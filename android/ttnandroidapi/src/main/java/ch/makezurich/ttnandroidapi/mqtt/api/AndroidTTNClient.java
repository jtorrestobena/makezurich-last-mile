package ch.makezurich.ttnandroidapi.mqtt.api;

import android.content.Context;
import android.util.Log;

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

import java.util.ArrayList;
import java.util.List;

public class AndroidTTNClient {
    private static final String TAG = "AndroidTTNClient";

    private static final String TTN_CLIENT_ID = "ch.makezuich.ttnandroidapi";

    public static final String ALL_DEVICES_FILTER = "+";

    //https://www.thethingsnetwork.org/docs/applications/mqtt/api.html
    private static final String BROKER_HOST = ".thethings.network";
    private static final String PROTOCOL_TCP = "tcp://";
    private static final int PORT = 1883;

    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;

    private String subscriptionTopicUpMessages;
    private List<AndroidTTNListener> listeners = new ArrayList<>();

    public AndroidTTNClient(Context context, String appId, String appAccessKey, String handler, String filter, final AndroidTTNListener listener) {
        final String serverUri = PROTOCOL_TCP + handler + BROKER_HOST + ":" + PORT;
        subscriptionTopicUpMessages = filter + "/devices/"+ filter +"/up";
        Log.d(TAG, "will connect to  " + serverUri + " subscribe " + subscriptionTopicUpMessages);
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, TTN_CLIENT_ID);
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        //mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(appId);
        mqttConnectOptions.setPassword(appAccessKey.toCharArray());

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
                Log.d(TAG, "Incoming message: " + new String(message.getPayload()));
                for (AndroidTTNListener l : listeners) {
                    l.onMessage(new String(message.getPayload()));
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
                    Log.d(TAG, "Message: " + topic + " : " + new String(message.getPayload()));
                    for (AndroidTTNListener l : listeners) {
                        l.onMessage(new String(message.getPayload()));
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
                    for (AndroidTTNListener l : listeners) {
                        l.onConnected(false);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failed to connect");
                    exception.printStackTrace();
                    for (AndroidTTNListener l : listeners) {
                        l.onError(exception);
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

    /*
    public void publishMessage(){

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(publishMessage.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
            showToast("Message Published");
            if(!mqttAndroidClient.isConnected()){
                showToast(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }
*/
}
