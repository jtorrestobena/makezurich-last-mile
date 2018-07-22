package ch.makezurich.ttnandroidapi.mqtt.api.data;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.io.Serializable;

import ch.makezurich.ttnandroidapi.common.AbstractTTNPacket;

/*
 * Copyright 2016 Fabio Tiriticco / Fabway
 * slightly modified by Jose Antonio Torres Tobena to accommodate the
 * new TTN format
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
 *
 * Created by fabiotiriticco on 5 June 2016.
 *
 */
public class Packet extends AbstractTTNPacket implements Serializable {

    private static final long serialVersionUID = 1L;

    /*
    Example incoming uplink packet:

    {
        "app_id":"1461658713846525",
        "dev_id":"01",
        "hardware_serial":"0004A30B001BAC1D",
        "port":1,
        "counter":0,
        "payload_raw":"AA==",
        "payload_fields":{
                            "altitude":0,
                            "fix":0,
                            "latitude":0,
                            "longitude":0},
        "metadata":{"
                        time":"2018-07-02T19:05:06.006550706Z"
                   }}
    */

    @SerializedName("app_id")
    String mAppId;

    @SerializedName("dev_id")
    String mDevId;

    @SerializedName("payload_raw")
    transient String mPayload;

    private byte[] payload;

    @SerializedName("port")
    int mPort;

    @SerializedName("counter")
    int mCounter;

    @SerializedName("hardware_serial")
    String mDevEUI;

    @SerializedName("metadata")
    Metadata mMetadata;

    @SerializedName("ttl")
    String mTTL;

    public String getApplicationId () {
        return mAppId;
    }

    public String getPayloadBase64() {
        if (mPayload == null) {
            mPayload = getPayloadBase64(payload);
        }

        return mPayload;
    }

    public int getPort() {
        return mPort;
    }

    public int getCounter() {
        return mCounter;
    }

    public String getDevId() {
        return mDevId;
    }

    public String getDevEUI() {
        return formatHexString(mDevEUI, SEP_WHITESPACE);
    }

    public Metadata getMetadata() {
        return mMetadata;
    }

    public String getTTL() {
        return mTTL;
    }

    @Override
    public String toString() {
        return new StringBuilder("Packet:")
                .append("\napp id: ").append(mAppId)
                .append("\ndev id: ").append(mDevId)
                .append("\ndeveui: ").append(mDevEUI)
                .append("\nport: ").append(mPort)
                .append("\ncounter: ").append(mCounter)
                .append("\npayload raw: ").append(mPayload)
                .append("\nmetadata: ").append(mMetadata)
                .toString();
    }

    @Override
    protected void parse() {
        payload = getPayloadBytes(mPayload);
    }

    @Override
    protected byte[] getPayload() {
        return payload;
    }

    @Override
    public DateTime getTimeStamp() {
        return getMetadata().mServerTime;
    }
}

