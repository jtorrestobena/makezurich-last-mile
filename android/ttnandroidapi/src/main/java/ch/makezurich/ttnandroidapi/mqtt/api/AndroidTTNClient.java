package ch.makezurich.ttnandroidapi.mqtt.api;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import net.danlew.android.joda.JodaTimeAndroid;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.joda.time.DateTime;

import java.lang.reflect.Modifier;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import ch.makezurich.ttnandroidapi.R;
import ch.makezurich.ttnandroidapi.common.DateTimeConverter;
import ch.makezurich.ttnandroidapi.common.TTNPacketTypeAdapterFactory;
import ch.makezurich.ttnandroidapi.mqtt.api.data.Packet;
import ch.makezurich.ttnandroidapi.mqtt.api.tls.SocketFactory;

/*
 * Copyright 2018 Jose Antonio Torres Tobena / bytecoders
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class AndroidTTNClient {
    private static final String TAG = "AndroidTTNClient";

    private static final String TTN_CLIENT_ID = "ch.makezuich.ttnandroidapi";

    public static final String ALL_DEVICES_FILTER = "+";

    //https://www.thethingsnetwork.org/docs/applications/mqtt/api.html
    private static final String BROKER_HOST = ".thethings.network";
    private static final String PROTOCOL_TCP = "tcp://";
    private static final String PROTOCOL_TLS = "ssl://";
    private static final int PORT = 1883;
    private static final int PORT_TLS = 8883;
    private final Gson mGson;

    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;
    private SocketFactory socketFactory;

    private final String subscriptionTopicUpMessages;
    private final String appId;
    private List<AndroidTTNListener> listeners = new ArrayList<>();

    private Class<? extends Packet> packetClass = Packet.class;

    public AndroidTTNClient(Context context, String appId, String appAccessKey, String handler, String filter, boolean enableTLS, final AndroidTTNListener listener) {
        this.appId = appId;
        // init joda date time
        JodaTimeAndroid.init(context);

        mGson = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .registerTypeAdapterFactory(new TTNPacketTypeAdapterFactory())
                .registerTypeAdapter(DateTime.class, new DateTimeConverter())
                .create();
        final String serverUri;
        if (enableTLS) {
            serverUri = PROTOCOL_TLS + handler + BROKER_HOST + ":" + PORT_TLS;
        } else {
            serverUri = PROTOCOL_TCP + handler + BROKER_HOST + ":" + PORT;
        }
        subscriptionTopicUpMessages = filter + "/devices/"+ filter +"/up";
        Log.d(TAG, "will connect to  " + serverUri + " subscribe " + subscriptionTopicUpMessages);
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, TTN_CLIENT_ID);
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        //mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(appId);
        mqttConnectOptions.setPassword(appAccessKey.toCharArray());
        if (enableTLS) try {
            SocketFactory.SocketFactoryOptions socketFactoryOptions = new SocketFactory.SocketFactoryOptions();
            socketFactoryOptions.withCaInputStream(context.getResources().openRawResource(R.raw.mqttca));
            socketFactory = new SocketFactory(socketFactoryOptions);
            mqttConnectOptions.setSocketFactory(socketFactory);
        } catch (Exception e) {
            Log.d(TAG, "Could not enable TLS: " + e.getMessage());
            e.printStackTrace();
            listener.onTLSError(e);
        }

        listeners.add(listener);

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    subscribeToTopic();
                }

                for (AndroidTTNListener l : listeners) {
                    l.onConnected(reconnect);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "The Connection was lost.");
                for (AndroidTTNListener l : listeners) {
                    l.onError(cause);
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String jsonStr = new String(message.getPayload());
                Log.d(TAG, "Incoming message: " + jsonStr);
                try {

                    Packet packet = mGson.fromJson(jsonStr, Packet.class);
                    for (AndroidTTNListener l : listeners) {
                        l.onPacket(packet);
                    }
                } catch (JsonSyntaxException e) {
                    for (AndroidTTNListener l : listeners) {
                        l.onError(e);
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    private void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(subscriptionTopicUpMessages, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Subscribed!");
                    for (AndroidTTNListener l : listeners) {
                        l.onConnected(false);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failed to subscribe");
                    for (AndroidTTNListener l : listeners) {
                        l.onError(exception);
                    }
                }
            });

            mqttAndroidClient.subscribe(subscriptionTopicUpMessages, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    String jsonStr = new String(message.getPayload());
                    Log.d(TAG, "Message arrived: " + jsonStr);
                    try {
                        Packet packet = mGson.fromJson(jsonStr, packetClass);
                        for (AndroidTTNListener l : listeners) {
                            l.onPacket(packet);
                        }
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                        for (AndroidTTNListener l : listeners) {
                            l.onError(e);
                        }
                    }
                }
            });

        } catch (MqttException ex){
            Log.d(TAG, "Exception whilst subscribing");
            ex.printStackTrace();
            for (AndroidTTNListener l : listeners) {
                l.onError(ex);
            }
        }
    }

    public void start() {

        try {
            Log.d(TAG, "Connecting");
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                    Log.d(TAG, "Connected: "+ listeners.size() + " listeners");
                    for (AndroidTTNListener l : listeners) {
                        l.onConnected(false);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failed to connect");
                    exception.printStackTrace();
                    if (exception.getCause() instanceof SSLHandshakeException) {
                        for (AndroidTTNListener l : listeners) l.onTLSError(exception);
                    } else {
                        for (AndroidTTNListener l : listeners) l.onError(exception);
                    }
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    public void stop() {
        mqttAndroidClient.unregisterResources();
        mqttAndroidClient.close();
    }

    public boolean isConnected() {
        return mqttAndroidClient.isConnected();
    }

    public void setPacketClass(Class<? extends Packet> packetClass) {
        this.packetClass = packetClass;
    }

    public void sendPayloadRaw(String device, byte[] payload, final AndroidTTNMessageListener listener) {
        final String messageJSON =
                "{\"payload_raw\": \""+ Base64.encodeToString(payload, Base64.NO_WRAP) +"\"}";

        final String deviceTopic = appId + "/devices/"+device+"/down";
        publishMessage(messageJSON, deviceTopic, listener);
    }

    private void publishMessage(String publishMessage, String publishTopic, final AndroidTTNMessageListener listener){
        Log.d(TAG, "Publish message: " + publishMessage + " topic: " + publishTopic);
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(publishMessage.getBytes());
            mqttAndroidClient.publish(publishTopic, message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Message Published");
                    listener.onSuccess();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Error sending message");
                    listener.onError(exception);
                }
            });
            if(!mqttAndroidClient.isConnected()){
                Log.d(TAG, mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            Log.d(TAG, "Error Publishing: " + e.getMessage());
            e.printStackTrace();
            listener.onError(e);
        }
    }

    public List<Certificate> getClientCertificates() {
        return socketFactory.getClientCertificates();
    }
}
