package ch.makezurich.conqueringlastmile;
/*
 * Copyright 2018 Jose Antonio Torres Tobena / bytecoders
 * slightly modified by Jose Antonio Torres Tobena
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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ch.makezurich.conqueringlastmile.fragment.DevicesFragment;
import ch.makezurich.conqueringlastmile.fragment.FrameFragment;
import ch.makezurich.conqueringlastmile.fragment.SendPayloadFragment;
import ch.makezurich.ttnandroidapi.common.StringUtil;
import ch.makezurich.ttnandroidapi.datastorage.api.Device;
import ch.makezurich.ttnandroidapi.datastorage.api.Frame;
import ch.makezurich.ttnandroidapi.datastorage.api.TTNDataStorageApi;
import ch.makezurich.ttnandroidapi.mqtt.api.AndroidTTNClient;
import ch.makezurich.ttnandroidapi.mqtt.api.AndroidTTNListener;
import ch.makezurich.ttnandroidapi.mqtt.api.AndroidTTNMessageListener;
import ch.makezurich.ttnandroidapi.mqtt.api.data.Packet;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        DevicesFragment.OnListFragmentInteractionListener,
        FrameFragment.OnFrameListFragmentInteractionListener, AndroidTTNListener {

    private static final String TAG = "MainActivity";


    private String appId;
    private String appAccessKey;
    private String handler;
    private boolean tlsEnabled;
    private boolean dataAPIEnabled;

    private AndroidTTNClient mAndroidTTNClient;
    private TTNDataStorageApi mTTNDataStore;

    private List<Device> devices = new ArrayList<>();
    private List<Frame> frames = new ArrayList<>();

    private FloatingActionButton fab;
    private ImageView mainLogo;
    private boolean isConfigValid;

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        setActionButtonSendMail();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mainLogo = findViewById(R.id.main_logo);

        doMainAnimation();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        isConfigValid = loadConfiguration(preferences);
        Log.d(TAG, "Config valid " + isConfigValid);
        if (isConfigValid) {
            startClients();
        } else {
            Log.d(TAG, "Client is not configured");
        }
    }

    private void setActionButtonSendMail() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Send me an e-mail", Snackbar.LENGTH_LONG)
                        .setAction("MAIL", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("message/rfc822");
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "josepantoni.torres@gmail.com" });
                                intent.putExtra(Intent.EXTRA_SUBJECT, "The Things Network App");
                                intent.putExtra(Intent.EXTRA_TEXT, "I would like to know more about the app sample or the Android TTN SDK.");

                                startActivity(Intent.createChooser(intent, "Send Email"));
                            }
                        }).show();
            }
        });
    }

    private void setActionButtonSendPayload() {
        fab.setVisibility(View.VISIBLE);
        fab.setImageResource(R.drawable.ic_baseline_send_24px);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentFragment instanceof SendPayloadFragment) {
                    final SendPayloadFragment sendPayloadFragment = (SendPayloadFragment) (SendPayloadFragment) currentFragment;
                    final String payloadHex = sendPayloadFragment.getPayloadHex();
                    final String device = sendPayloadFragment.getSelectedDevice();
                    Log.d(TAG, "Send hex payload: " + payloadHex + " to device " + device);
                    if (device == null || device.isEmpty()) {
                        Toast.makeText(MainActivity.this, R.string.no_device_selected, Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (payloadHex != null && !payloadHex.isEmpty()) {
                        mAndroidTTNClient.sendPayloadRaw(device, StringUtil.hexStringToByteArray(payloadHex), new AndroidTTNMessageListener() {
                            @Override
                            public void onSuccess() {
                                sendPayloadFragment.setSendProgress(false);
                                Toast.makeText(MainActivity.this, R.string.payload_send_success, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError(Throwable _error) {
                                sendPayloadFragment.setSendProgress(false);
                                Toast.makeText(MainActivity.this, R.string.payload_send_error, Toast.LENGTH_LONG).show();
                            }
                        });
                        sendPayloadFragment.setSendProgress(true);
                    } else {
                        Toast.makeText(MainActivity.this, R.string.payload_empty_msg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(TAG, "Fragment is not send payload");
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAndroidTTNClient.stop();
    }

    private void doMainAnimation() {
        Animation hyperspaceJump = AnimationUtils.loadAnimation(this, R.anim.hyperspace_jump);
        mainLogo.startAnimation(hyperspaceJump);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start
        if (isConfigValid) {
            reloadDevices();
        }
    }

    private void startClients() {
        if (mAndroidTTNClient != null)
            mAndroidTTNClient.stop();

        mAndroidTTNClient = new AndroidTTNClient(this, appId, appAccessKey, handler, AndroidTTNClient.ALL_DEVICES_FILTER, tlsEnabled, this);
        mAndroidTTNClient.start();
        mTTNDataStore = new TTNDataStorageApi(appId, appAccessKey);
    }

    private void reloadDevices() {
        new Thread() {
            @Override
            public void run() {
                devices = mTTNDataStore.getDevices();
                // Frames from last 7 days
                frames = mTTNDataStore.getAllFrames("7d");

                final StringBuilder welComeStr = new StringBuilder("You have:\n")
                        .append(devices.size()).append(" devices\n")
                        .append(frames.size()).append(" frames\n");
                Log.d(TAG, welComeStr.toString());
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragmentContainer.setText(welComeStr.toString());
                    }
                });*/
            }
        }.start();
    }



    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mainLogo.setVisibility(View.GONE);

        setTitle(item.getTitle());

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_devices) {
            fab.setVisibility(View.GONE);
            replaceFragment(DevicesFragment.newInstance().setDevices(devices));
        } else if (id == R.id.nav_frames) {
            fab.setVisibility(View.GONE);
            replaceFragment(FrameFragment.newInstance().setFrames(frames));
        } else if (id == R.id.nav_mqtt) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_arduino) {

        } else if (id == R.id.nav_send) {
            setActionButtonSendPayload();
            replaceFragment(SendPayloadFragment.newInstance().setDevices(devices));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showToast(final String toastMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        isConfigValid = loadConfiguration(sharedPreferences);
        if (isConfigValid) {
            startClients();
            reloadDevices();
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

    public void replaceFragment(Fragment fragment) {
        currentFragment = fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }

    public List<Frame> getNewFrames() {
        frames = mTTNDataStore.getAllFrames("7d");
        return frames;
    }

    @Override
    public void onListFragmentInteraction(Device device) {
        Log.d(TAG, "Clicked on device " + device.getName());
    }

    @Override
    public void onListFragmentInteraction(Frame frame) {
        Log.d(TAG, "Clicked on frame with raw data " + frame.getRaw());
    }

    @Override
    public void onError(Throwable _error) {
        showToast("Error " + _error);
    }

    @Override
    public void onTLSError(Throwable _error) {
        // Show an error indicating that TLS failed
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.tls_error_title)
                .setIcon(R.drawable.ic_baseline_lock_open_24px)
                .setMessage(R.string.tls_error_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Disable tls
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                        editor.putBoolean("enable_tls", false).apply();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    @Override
    public void onConnected(boolean _reconnect) {
        showToast(_reconnect ? "Reconnected" : "Connected");
    }

    @Override
    public void onPacket(Packet _message) {

        showToast(_message.toString());
    }
}
