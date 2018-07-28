package ch.makezurich.conqueringlastmile;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.makezurich.conqueringlastmile.activity.MainActivity;
import ch.makezurich.conqueringlastmile.datastorage.DataStorage;
import ch.makezurich.conqueringlastmile.datastorage.DeviceProfile;
import ch.makezurich.conqueringlastmile.packetclass.DemoPayloadPacket;
import ch.makezurich.conqueringlastmile.util.ConnectionSettings;
import ch.makezurich.conqueringlastmile.util.DeviceRequestCallback;
import ch.makezurich.conqueringlastmile.util.FileInputOutput;
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
    private List<DeviceProfile> devicesProfiles = new ArrayList<>();
    private List<Frame> frames = new ArrayList<>();
    private List<Packet> sessionPackets = new ArrayList<>();

    private boolean isConfigValid;
    private boolean showNotifications;
    private String notificationRingtone;
    private boolean notificationVibrate;
    private boolean debugEnabled;

    private List<TTNSessionListener> listeners = new ArrayList<>();
    private DataStorage dataStorage;

    private static final String CHANNEL_ID = "defaultTTNChannel";
    private int notificationId = 0;

    private File sessionsFolder;

    public interface TTNSessionListener extends AndroidTTNListener {
        void onSessionRefresh(List<Packet> sessionPackets);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        dataStorage = new DataStorage(this, preferences);
        sessionsFolder = new File(getFilesDir(), "sessions");

        isConfigValid = loadConfiguration(preferences);
        if (isConfigValid) {
            startClients();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "notifications_new_message":
                showNotifications = sharedPreferences.getBoolean(key, true);
                if (showNotifications) {
                    createNotificationChannel();
                }
                return;
            case "notifications_new_message_ringtone":
                notificationRingtone = sharedPreferences.getString(key, null);
                return;
            case "notifications_new_message_vibrate":
                notificationVibrate = sharedPreferences.getBoolean(key, true);
                return;
            case "enable_debug":
                debugEnabled = sharedPreferences.getBoolean(key, false);
                return;
        }
        // For all other cases revalidate the configuration entirely and try to reload clients

        isConfigValid = loadConfiguration(sharedPreferences);
        if (isConfigValid) {
            startClients();
            reloadDevices(null);
        }
    }

    private boolean loadConfiguration(SharedPreferences preferences) {
        // Check if the app id and secret key are empty or have whitespaces
        appId = preferences.getString("ttn_app_id", null);
        Log.d(TAG, "App id set to " + appId);
        if (appId == null || appId.isEmpty() || appId.contains(" ")) return false;

        appAccessKey = preferences.getString("ttn_app_access_key", null);
        Log.d(TAG, "appAccessKey id set to " + appAccessKey);
        if (appAccessKey == null || appAccessKey.isEmpty() || appAccessKey.contains(" ")) return false;

        handler = preferences.getString("ttn_handler", "eu");
        tlsEnabled = preferences.getBoolean("enable_tls", true);

        dataAPIEnabled = preferences.getBoolean("enable_data_api", false);
        Log.d(TAG, handler + " connection with TLS: " + tlsEnabled + " data api " + dataAPIEnabled);

        showNotifications = preferences.getBoolean("notifications_new_message", true);

        if (showNotifications) {
            createNotificationChannel();
        }

        notificationRingtone = preferences.getString("notifications_new_message_ringtone", null);

        notificationVibrate = preferences.getBoolean("notifications_new_message_vibrate", true);

        debugEnabled = preferences.getBoolean("enable_debug", false);

        return true;
    }

    public List<Frame> getNewFrames(String device) {
        List<Frame> newFrames = new ArrayList<>();
        if (dataAPIEnabled) {
            try {
                newFrames = mTTNDataStore.getAllFrames(device);
            } catch (TTNDataStorageApi.TTNDataException e) {
                e.printStackTrace();
            }
        }
        return newFrames;
    }

    public List<Frame> getNewFrames() {
        if (dataAPIEnabled) {
            try {
                frames = mTTNDataStore.getAllFrames();
            } catch (TTNDataStorageApi.TTNDataException e) {
                e.printStackTrace();
            }
        }
        return frames;
    }

    private void startClients() {
        if (mAndroidTTNClient != null)
            mAndroidTTNClient.stop();

        mAndroidTTNClient = new AndroidTTNClient(this, appId, appAccessKey, handler, AndroidTTNClient.ALL_DEVICES_FILTER, tlsEnabled, this);
        mAndroidTTNClient.setPacketClass(DemoPayloadPacket.class);
        mAndroidTTNClient.start();
        if (dataAPIEnabled) {
            mTTNDataStore = new TTNDataStorageApi(this, appId, appAccessKey);
        }
    }

    public void reloadDevices(final DeviceRequestCallback drc) {
        if (dataAPIEnabled) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        devices = mTTNDataStore.getDevices();
                        // Process profiles
                        devicesProfiles = new ArrayList<>();
                        for (Device d : devices) {
                            devicesProfiles.add(dataStorage.getApplicationData().getProfile(d.getName()));
                        }

                        getNewFrames();
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
        } else if (drc != null) {
            drc.onDevicesLoaded();
        }
    }

    public boolean isConfigValid() {
        return isConfigValid;
    }

    public void sendPayloadRaw(String device, byte[] payload, final AndroidTTNMessageListener listener) {
        mAndroidTTNClient.sendPayloadRaw(device, payload, listener);
    }

    public void stopClients() {
        if (mAndroidTTNClient != null) {
            mAndroidTTNClient.stop();
        }
    }

    public List<Device> getDevices() {
        return devices;
    }
    public List<DeviceProfile> getProfiles() {
        return devicesProfiles;
    }

    public List<Frame> getFrames() {
        return frames;
    }

    public List<Frame> getFrames(String device) {
        final List<Frame> deviceFrames = new ArrayList<>();
        for (Frame f : frames) {
            if (f.getDeviceId().equals(device)) {
                deviceFrames.add(f);
            }
        }

        return deviceFrames;
    }



    public List<Packet> getSessionPackets() {
        return sessionPackets;
    }

    public void addListener(TTNSessionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(TTNSessionListener listener) {
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
        Log.d(TAG, "Connected: " + listeners.size() + " listening to app");
        for (AndroidTTNListener l : listeners) l.onConnected(_reconnect);
    }

    @Override
    public void onPacket(Packet _message) {
        sessionPackets.add(_message);

        if (showNotifications) {
            showNotification(_message);
        }

        for (AndroidTTNListener l : listeners) l.onPacket(_message);
    }

    public ConnectionSettings getConnectionSettings() {
        return new ConnectionSettings(tlsEnabled, mAndroidTTNClient);
    }

    public DataStorage getDataStorage() {
        return dataStorage;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(Packet _msg) {
        Intent intent = new Intent(this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(MainActivity.EXTRA_PACKET_VALUE, _msg);
        intent.putExtra(MainActivity.EXTRA_PACKET, bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setColor(getResources().getColor(R.color.colorPrimaryDark))
                .setContentTitle(getString(R.string.message_from, _msg.getDevId()))
                .setContentText(_msg.getPayloadBase64())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(_msg.getPayloadBase64()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (notificationVibrate) {
            notiBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        }

        if (notificationRingtone != null && !notificationRingtone.isEmpty()) {
            notiBuilder.setSound(Uri.parse(notificationRingtone));
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, notiBuilder.build());
        notificationId++;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public boolean saveSession(String name, boolean overwrite) {
        final File sessionFile = new File(sessionsFolder, name);
        if (sessionFile.exists() && !overwrite) {
            return false;
        }

        if (!sessionsFolder.exists()) {
            sessionsFolder.mkdirs();
        }
        saveSessionImpl(sessionFile);
        return true;
    }

    private void saveSessionImpl(File sessFile) {
        FileInputOutput.write(sessionPackets, sessFile, new FileInputOutput.FileIOWriteCallback() {
            @Override
            public void onSaveComplete() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TTNApplication.this, R.string.session_saved, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onException(Exception ex) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TTNApplication.this, R.string.session_save_failed, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void loadSession(String session) {
        File sessionFile = new File(sessionsFolder, session);
        if (sessionFile.exists()) {
            FileInputOutput.read(sessionFile, new FileInputOutput.FileIOReadCallback<List<Packet>>() {
                @Override
                public void onReadComplete(List<Packet> object) {
                    sessionPackets = object;
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            for (TTNSessionListener l : listeners) l.onSessionRefresh(sessionPackets);
                            Toast.makeText(TTNApplication.this, R.string.session_loaded, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onException(final Exception ex) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(TTNApplication.this, getString(R.string.could_not_load_session) + ex, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        } else {
            Toast.makeText(this, R.string.session_not_exist, Toast.LENGTH_LONG).show();
        }
    }

    public String[] getSavedSessions() {
        if (!sessionsFolder.exists()) {
            return null;
        }

        return sessionsFolder.list();
    }

    private void runOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public boolean isConnectedToTTN() {
        return mAndroidTTNClient != null && mAndroidTTNClient.isConnected();
    }
}
