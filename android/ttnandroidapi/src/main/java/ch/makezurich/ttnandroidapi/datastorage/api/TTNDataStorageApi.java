package ch.makezurich.ttnandroidapi.datastorage.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import ch.makezurich.ttnandroidapi.common.DateTimeConverter;
import ch.makezurich.ttnandroidapi.common.TTNPacketTypeAdapterFactory;

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
public class TTNDataStorageApi {

    private static final String TAG = "TTNDataStorageApi";

    private String authKey;
    private String apiUrl;
    private Gson mGson;

    public static class TTNDataException extends Exception {
        private int code = 0;

        public TTNDataException(String message) {
            super(message);
        }

        public TTNDataException(String message, Throwable cause) {
            super(message, cause);
        }

        public TTNDataException(String message, int code) {
            super(message);
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }
    }

    public TTNDataStorageApi(Context context, String appId, String authKey) {
        // init joda date time
        JodaTimeAndroid.init(context);

        mGson = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .registerTypeAdapter(DateTime.class, new DateTimeConverter())
                .registerTypeAdapterFactory(new TTNPacketTypeAdapterFactory())
                .create();

        this.apiUrl = "https://"+ appId +".data.thethingsnetwork.org/api/v2/";
        this.authKey = authKey;
    }

    public List<Device> getDevices() throws TTNDataException {
        List<Device> devices = new ArrayList<>();
        try {
            String jsonStr= executeQuery("devices");
            if (jsonStr != null) {
                String[] devstr = mGson.fromJson(jsonStr, String[].class);
                if (devstr != null) {
                    for (String d : devstr) {
                        devices.add(new Device(d));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new TTNDataException("IOException", e);
        }
        return devices;
    }

    private String executeQuery(String method) throws IOException, TTNDataException {
        final String url = apiUrl + method;
        Log.d(TAG, "Fetching URL: " + url);
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "key " + authKey);
        connection.setReadTimeout(15000);
        connection.setConnectTimeout(15000);

        int code = connection.getResponseCode();
        Log.d(TAG, "Got reply code: " + code);

        if (code == 200) {
            final String reply = getString(connection.getInputStream());
            Log.d(TAG, "Got reply: " + reply);
            return reply;
        }

        throw new TTNDataException("HTTP Error", code);
    }

    private static String getString(InputStream inputStream) {
        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        return  s.hasNext() ? s.next() : "";
    }

    public List<Frame> getAllFrames(String since) throws TTNDataException {
        try {
            String jsonStr= executeQuery("query?last=" + since);
            if (jsonStr != null) {
                Frame[] frames = mGson.fromJson(jsonStr, Frame[].class);
                if (frames != null) {
                    return new ArrayList<>(Arrays.asList(frames));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new TTNDataException("IOException", e);
        }

        return new ArrayList<>();
    }
}
