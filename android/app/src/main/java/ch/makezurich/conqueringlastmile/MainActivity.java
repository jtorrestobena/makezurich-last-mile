package ch.makezurich.conqueringlastmile;

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
import ch.makezurich.ttnandroidapi.datastorage.api.Device;
import ch.makezurich.ttnandroidapi.datastorage.api.Frame;
import ch.makezurich.ttnandroidapi.datastorage.api.TTNDataStorageApi;
import ch.makezurich.ttnandroidapi.mqtt.api.AndroidTTNClient;
import ch.makezurich.ttnandroidapi.mqtt.api.AndroidTTNListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        DevicesFragment.OnListFragmentInteractionListener,
        FrameFragment.OnFrameListFragmentInteractionListener, AndroidTTNListener {

    private static final String TAG = "MainActivity";


    private String appId;
    private String appAccessKey;
    private String handler;

    private AndroidTTNClient mAndroidTTNClient;
    private TTNDataStorageApi mTTNDataStore;

    private List<Device> devices = new ArrayList<>();
    private List<Frame> frames = new ArrayList<>();

    private View mainView;
    private ImageView mainLogo;
    private boolean isConfigValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mainLogo = findViewById(R.id.main_logo);
        mainView = findViewById(R.id.fragment_container);

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
        mAndroidTTNClient = new AndroidTTNClient(this, appId, appAccessKey, handler, AndroidTTNClient.ALL_DEVICES_FILTER, this);
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

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_devices) {
            replaceFragment(DevicesFragment.newInstance().setDevices(devices));
        } else if (id == R.id.nav_frames) {
            replaceFragment(FrameFragment.newInstance().setFrames(frames));
        } else if (id == R.id.nav_mqtt) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_arduino) {

        } else if (id == R.id.nav_send) {

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
        if (key.equals("ttn_app_id") || key.equals("ttn_app_access_key") || key.equals("ttn_handler")) {
            isConfigValid = loadConfiguration(sharedPreferences);
            Log.d(TAG, "TTN account config changed, is valid " + isConfigValid);
            if (isConfigValid) {
                startClients();
                reloadDevices();
            }
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

        return true;
    }

    public void replaceFragment(Fragment fragment) {
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
    public void onConnected(boolean _reconnect) {
        showToast(_reconnect ? "Reconnected" : "Connected");
    }

    @Override
    public void onMessage(String _message) {
        showToast(_message);
    }
}
