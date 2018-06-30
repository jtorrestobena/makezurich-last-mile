package ch.makezurich.ttnandroidapi.datastorage.api;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class TTNDataStorageApi {

    private static final String TAG = "TTNDataStorageApi";

    private String authKey;
    private String apiUrl;

    public TTNDataStorageApi(String appId, String authKey) {
        this.apiUrl = "https://"+ appId +".data.thethingsnetwork.org/api/v2/";
        this.authKey = authKey;
    }

    public List<Device> getDevices() {
        List<Device> devices = new ArrayList<>();
        try {
            String jsonStr= executeQuery("devices");
            if (jsonStr != null) {
                Gson gson = new Gson();
                String[] devstr = gson.fromJson(jsonStr, String[].class);
                if (devstr != null) {
                    for (String d : devstr) {
                        devices.add(new Device(d));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return devices;
    }

    private String executeQuery(String method) throws IOException {
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

        return null;
    }

    private static String getString(InputStream inputStream) {
        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        return  s.hasNext() ? s.next() : "";
    }

    public List<Frame> getAllFrames(String since) {
        try {
            String jsonStr= executeQuery("query?last=" + since);
            if (jsonStr != null) {
                Gson gson = new Gson();
                Frame[] frames = gson.fromJson(jsonStr, Frame[].class);
                if (frames != null) {
                    final ArrayList<Frame> frames1ist= new ArrayList<>(Arrays.asList(frames));
                    for (Frame f: frames1ist) {
                        f.parse();
                    }
                    return frames1ist;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<Frame>();
    }
}