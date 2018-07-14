package ch.makezurich.conqueringlastmile;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ch.makezurich.conqueringlastmile.util.ConnectionSettings;
import ch.makezurich.conqueringlastmile.util.DeviceRequestCallback;
import ch.makezurich.ttnandroidapi.datastorage.api.Device;
import ch.makezurich.ttnandroidapi.datastorage.api.Frame;
import ch.makezurich.ttnandroidapi.datastorage.api.TTNDataStorageApi;
import ch.makezurich.ttnandroidapi.mqtt.api.AndroidTTNClient;
import ch.makezurich.ttnandroidapi.mqtt.api.AndroidTTNListener;
import ch.makezurich.ttnandroidapi.mqtt.api.AndroidTTNMessageListener;
import ch.makezurich.ttnandroidapi.mqtt.api.data.Packet;

public class TTNApplication extends Application implements SharedPreferences.OnSharedPreferenceChangeListener, AndroidTTNListener {

    private static final String TAG = "TTNApplication";
    private String appId;
    private String appAccessKey;
    private String handler;
    private boolean tlsEnabled;
    private boolean dataAPIEnabled;

    private AndroidTTNClient mAndroidTTNClient;
    private TTNDataStorageApi mTTNDataStore;

    private List<Device> devices = new ArrayList<>();
    private List<Frame> frames = new ArrayList<>();

    private boolean isConfigValid;

    private List<AndroidTTNListener> listeners = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);

        isConfigValid = loadConfiguration(preferences);
        if (isConfigValid) {
            startClients();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        isConfigValid = loadConfiguration(sharedPreferences);
        if (isConfigValid) {
            startClients();
            reloadDevices(null);
        }
    }

    private boolean loadConfiguration(SharedPreferences preferences) {
        appId = preferences.getString("ttn_app_id", null);
        Log.d(TAG, "App id set to " + appId);
        if (appId == null) return false;

        appAccessKey = preferences.getString("ttn_app_access_key", null);
        Log.d(TAG, "appAccessKey id set to " + appAccessKey);
        if (appAccessKey == null) return false;

        handler = preferences.getString("ttn_handler", "eu");
        tlsEnabled = preferences.getBoolean("enable_tls", true);

        dataAPIEnabled = preferences.getBoolean("enable_data_api", false);
        Log.d(TAG, handler + " connection with TLS: " + tlsEnabled + " data api " + dataAPIEnabled);

        return true;
    }

    public List<Frame> getNewFrames() {
        try {
            frames = mTTNDataStore.getAllFrames("7d");
        } catch (TTNDataStorageApi.TTNDataException e) {
            e.printStackTrace();
        }
        return frames;
    }

    private void startClients() {
        if (mAndroidTTNClient != null)
            mAndroidTTNClient.stop();

        mAndroidTTNClient = new AndroidTTNClient(getApplicationContext(), appId, appAccessKey, handler, AndroidTTNClient.ALL_DEVICES_FILTER, tlsEnabled, this);
        mAndroidTTNClient.start();
        mTTNDataStore = new TTNDataStorageApi(appId, appAccessKey);
    }

    public void reloadDevices(final DeviceRequestCallback drc) {
        new Thread() {
            @Override
            public void run() {
                try {
                    devices = mTTNDataStore.getDevices();
                    // Frames from last 7 days
                    frames = mTTNDataStore.getAllFrames("7d");

                    if (drc != null) {
                        drc.onDevicesLoaded();
                    }
                } catch (TTNDataStorageApi.TTNDataException ttnExc) {
                    if (drc != null) {
                        drc.onTTNException(ttnExc);
                    }
                }

            }
        }.start();
    }

    public boolean isConfigValid() {
        return isConfigValid;
    }

    public void sendPayloadRaw(String device, byte[] payload, final AndroidTTNMessageListener listener) {
        mAndroidTTNClient.sendPayloadRaw(device, payload, listener);
    }

    public void stopClients() {
        mAndroidTTNClient.stop();
    }

    public List<Device> getDevices() {
        return devices;
    }

    public List<Frame> getFrames() {
        return frames;
    }

    public void addListener(AndroidTTNListener listener) {
        listeners.add(listener);
    }

    public void removeListener(AndroidTTNListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onError(Throwable _error) {
        for (AndroidTTNListener l : listeners) l.onError(_error);
    }

    @Override
    public void onTLSError(Throwable _error) {
        for (AndroidTTNListener l : listeners) l.onTLSError(_error);
    }

    @Override
    public void onConnected(boolean _reconnect) {
        for (AndroidTTNListener l : listeners) l.onConnected(_reconnect);
    }

    @Override
    public void onPacket(Packet _message) {
        for (AndroidTTNListener l : listeners) l.onPacket(_message);
    }

    public ConnectionSettings getConnectionSettings() {
        return new ConnectionSettings(tlsEnabled, mAndroidTTNClient);
    }
}
