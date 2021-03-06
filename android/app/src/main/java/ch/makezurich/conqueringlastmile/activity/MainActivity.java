package ch.makezurich.conqueringlastmile.activity;
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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
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
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import ch.makezurich.conqueringlastmile.R;
import ch.makezurich.conqueringlastmile.TTNApplication;
import ch.makezurich.conqueringlastmile.datastorage.DeviceProfile;
import ch.makezurich.conqueringlastmile.fragment.DashboardFragment;
import ch.makezurich.conqueringlastmile.fragment.DeviceLocationFragment;
import ch.makezurich.conqueringlastmile.fragment.DevicesFragment;
import ch.makezurich.conqueringlastmile.fragment.FrameFragment;
import ch.makezurich.conqueringlastmile.fragment.PacketFragment;
import ch.makezurich.conqueringlastmile.fragment.SendPayloadFragment;
import ch.makezurich.conqueringlastmile.fragment.ToolsFragment;
import ch.makezurich.conqueringlastmile.fragment.intro.IntroFragment;
import ch.makezurich.conqueringlastmile.util.DeviceRequestCallback;
import ch.makezurich.ttnandroidapi.common.StringUtil;
import ch.makezurich.ttnandroidapi.datastorage.api.Frame;
import ch.makezurich.ttnandroidapi.datastorage.api.TTNDataStorageApi;
import ch.makezurich.ttnandroidapi.mqtt.api.AndroidTTNMessageListener;
import ch.makezurich.ttnandroidapi.mqtt.api.data.Packet;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SendPayloadFragment.OnSendPayloadRequest,
        DevicesFragment.OnListFragmentInteractionListener,
        PacketFragment.OnPacketFragmentSelectionListener,
        FrameFragment.OnFrameListFragmentInteractionListener,
        DashboardFragment.OnDashboardSelectionListener, TTNApplication.TTNSessionListener {

    private static final String TAG = "MainActivity";
    public static final String EXTRA_PACKET = "EXTRA_PACKET";
    public static final String EXTRA_PACKET_VALUE = "EXTRA_PACKET_VALUE";

    private TTNApplication ttnApp;

    private NavigationView navigationView;
    private ConstraintLayout welcomeLayout;
    private ImageView connectingImageView;
    private ImageView fetchDataImageView;

    private FragmentManager fragmentManager;

    private IntroFragment introFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ttnApp = (TTNApplication) getApplication();
        ttnApp.addListener(this);

        FloatingActionButton fab = findViewById(R.id.fab_mail);
        setActionButtonSendMail(fab);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        fragmentManager = getSupportFragmentManager();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        welcomeLayout = findViewById(R.id.welcome_layout);

        connectingImageView = findViewById(R.id.connecting_iv);
        fetchDataImageView = findViewById(R.id.fetching_data_iv);

        if (ttnApp.isConfigValid()) {
            setupConnectingViewsAnimation();
        } else {
            welcomeLayout.setVisibility(View.GONE);
            introFragment = IntroFragment.newInstance();
            replaceFragment(introFragment);
        }
        Log.d(TAG, "Config valid " + ttnApp.isConfigValid());
    }

    private void setActionButtonSendMail(final FloatingActionButton fab) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.send_me_mail, Snackbar.LENGTH_LONG)
                        .setAction(R.string.mail_action, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("message/rfc822");
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.email_address) });
                                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
                                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_text));

                                startActivity(Intent.createChooser(intent, getString(R.string.mail_chooser_type)));
                            }
                        }).show();
            }
        });
    }

    @Override
    public void onPayloadRequest(@NonNull String device, @NonNull String payloadHex, @NonNull final SendPayloadFragment sendPayloadFragment) {
        ttnApp.sendPayloadRaw(device, StringUtil.hexStringToByteArray(payloadHex), new AndroidTTNMessageListener() {
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
    }

    @Override
    public void onResume() {
        super.onResume();
        final Bundle bundleExtra = getIntent().getBundleExtra(EXTRA_PACKET);
        if (bundleExtra != null) {
            final Object o = bundleExtra.get(EXTRA_PACKET_VALUE);
            if (o != null && o instanceof Packet) {
                startPacketDetailActivity((Packet) o);
            }
        }

        // Start
        if (ttnApp.isConfigValid()) {
            ttnApp.reloadDevices(new DeviceRequestCallback() {
                @Override
                public void onDevicesLoaded() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fetchDataImageView.clearAnimation();
                            fetchDataImageView.setImageResource(R.drawable.ic_baseline_done_24px);
                        }
                    });
                }

                @Override
                public void onTTNException(TTNDataStorageApi.TTNDataException ttnExc) {
                    showErrorIcon(fetchDataImageView, R.id.fetch_data_layout);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ttnApp.stopClients();
        ttnApp.removeListener(this);
    }

    private void setupConnectingViewsAnimation() {
        if (ttnApp.isConnectedToTTN()) {
            connectingImageView.setImageResource(R.drawable.ic_baseline_done_24px);
        } else {
            connectingImageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate));
        }
        fetchDataImageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            if (fragmentManager.getBackStackEntryCount() == 0) {
                Log.d(TAG, "End of back stack, showing main view");
                welcomeLayout.setVisibility(View.VISIBLE);
                setTitle(R.string.app_name);
            }
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
        } else if (id == R.id.action_load_session) {
            loadSavedSession();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        welcomeLayout.setVisibility(View.GONE);

        if (ttnApp.isConfigValid()) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();
            final String title = item.getTitle().toString();

        switch (id) {
            case R.id.nav_dashboard:
                replaceFragment(DashboardFragment.newInstance(ttnApp.getDevices().size(), ttnApp.getFrames().size()).withIdTitle(title, id));
                break;
            case R.id.nav_devices:
                replaceFragment(DevicesFragment.newInstance().withIdTitle(title, id));
                break;
            case R.id.nav_frames:
                replaceFragment(FrameFragment.newInstance().setFrames(ttnApp.getFrames()).withIdTitle(title, id));
                break;
            case R.id.nav_mqtt:
                replaceFragment(PacketFragment.newInstance().withIdTitle(title, id));
                break;
            case R.id.nav_manage:
                replaceFragment(ToolsFragment.newInstance().withIdTitle(title, id));
                break;
            case R.id.nav_arduino:

                    break;
            case R.id.nav_gps_tracker:
                replaceFragment(DeviceLocationFragment.newInstance(null, null));
                break;
            case R.id.nav_send:
                replaceFragment(SendPayloadFragment.newInstance().setDevices(ttnApp.getDevices()).withIdTitle(title, id));
                break;
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showToast(final String toastMessage) {
        if (ttnApp.isDebugEnabled()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }

    @Override
    public void onListFragmentInteraction(DeviceProfile device) {
        Log.d(TAG, "Clicked on device " + device.getId());
        startActivity(new Intent(this, DeviceActivity.class).putExtra(DeviceActivity.EXTRA_DEVICE_ID, device.getId()));
    }

    @Override
    public void onListFragmentInteraction(Frame frame) {
        Log.d(TAG, "Clicked on frame with raw data " + frame.getRaw());
    }

    @Override
    public void onError(Throwable _error) {
        showToast("Error " + _error);
        showErrorIcon(connectingImageView, R.id.connect_layout);
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
        if (introFragment == null) {
            connectingImageView.clearAnimation();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    introFragment.setConnectionSuccessful();
                }
            });
        }
        connectingImageView.setImageResource(R.drawable.ic_baseline_done_24px);
    }

    @Override
    public void onPacket(Packet _message) {
        showToast(_message.toString());
    }

    private void showErrorIcon(final ImageView iv, @IdRes final int layoutId) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            // Vibrate for 500 milliseconds, the duration of the animation
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            }else {
                v.vibrate(500);
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iv.clearAnimation();
                iv.setImageResource(R.drawable.ic_baseline_close_24px);
                findViewById(layoutId).startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.error));
            }
        });
    }

    @Override
    public void onDashboardSelection(int navItem) {
        setSelectedItem(navItem);
        onNavigationItemSelected(navigationView.getMenu().findItem(navItem));
    }

    public void setSelectedItem(int navItem) {
        navigationView.setCheckedItem(navItem);
    }

    @Override
    public void onPacketSelected(Packet packet) {
        startPacketDetailActivity(packet);
    }

    private void startPacketDetailActivity(Packet packet) {
        Log.d(TAG, "Selected packet for inspection " + packet);
        // TODO start fragment for showing packet details
    }

    private void loadSavedSession() {
        final String[] savedSessions = ttnApp.getSavedSessions();
        if (savedSessions == null || savedSessions.length == 0) {
            Toast.makeText(this, "No saved sessions available", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.ic_baseline_restore_24px);
        builderSingle.setTitle(R.string.select_session_load);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, savedSessions);

        builderSingle.setNegativeButton(android.R.string.cancel, null);

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ttnApp.loadSession(arrayAdapter.getItem(which));
            }
        });
        builderSingle.show();
    }

    @Override
    public void onSessionRefresh(List<Packet> sessionPackets) {}
}
