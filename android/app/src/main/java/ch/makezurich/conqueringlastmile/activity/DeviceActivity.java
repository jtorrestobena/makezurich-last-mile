package ch.makezurich.conqueringlastmile.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ch.makezurich.conqueringlastmile.R;
import ch.makezurich.conqueringlastmile.TTNApplication;
import ch.makezurich.conqueringlastmile.datastorage.AppDataSaveStatus;
import ch.makezurich.conqueringlastmile.datastorage.DeviceProfile;
import ch.makezurich.conqueringlastmile.fragment.FrameFragment;
import ch.makezurich.ttnandroidapi.datastorage.api.Frame;

public class DeviceActivity extends PhotoActivity implements FrameFragment.OnFrameListFragmentInteractionListener {
    private static final String TAG = DeviceActivity.class.getSimpleName();

    private TTNApplication ttnApp;
    private String devId;
    private DeviceProfile deviceProfile;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView ivTabHeader;
    private TabLayout tabLayout;
    private Toolbar toolbar;

    public static final String EXTRA_DEVICE_ID = "EXTRA_DEVICE_ID";

    // Height and with for the device pictures
    private static final int DEVICE_PICTURE_WIDTH = 1280;
    private static final int DEVICE_PICTURE_HEIGHT = 720;
    private int currentItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        devId = intent.getStringExtra(EXTRA_DEVICE_ID);
        ttnApp = (TTNApplication) getApplication();
        deviceProfile = ttnApp.getDataStorage().getApplicationData().getProfile(devId);
        onConfigurationChanged(getResources().getConfiguration());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
                ? R.layout.activity_device : R.layout.activity_device_land);

        toolbar = (Toolbar) findViewById(R.id.htab_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            if (deviceProfile != null) {
                getSupportActionBar().setTitle(deviceProfile.getFriendlyName());
            } else {
                getSupportActionBar().setTitle(R.string.device);
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(FrameFragment.newInstance().setFrames(ttnApp.getFrames()), getString(R.string.frames));
        adapter.addFrag(FrameFragment.newInstance().setFrames(new ArrayList<Frame>()), getString(R.string.activations));
        adapter.addFrag(FrameFragment.newInstance().setFrames(new ArrayList<Frame>()), getString(R.string.locations));
        ViewPager viewPager = (ViewPager) findViewById(R.id.htab_viewpager);
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.htab_tabs);
        tabLayout.setupWithViewPager(viewPager);

        ivTabHeader = findViewById(R.id.htab_header);
        collapsingToolbarLayout = findViewById(R.id.htab_collapse_toolbar);

        viewPager.setCurrentItem(currentItem);

        try {
            Bitmap bitmap = deviceProfile.getPicture();

            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.things_uno);
            } else {
                ivTabHeader.setImageBitmap(bitmap);
            }
            setupPalette(bitmap);

        } catch (Exception e) {
            // if Bitmap fetch fails, fallback to primary colors
            Log.e(TAG, "Failed to create bitmap from background", e.fillInStackTrace());
            collapsingToolbarLayout.setContentScrimColor(
                    ContextCompat.getColor(this, R.color.colorPrimary)
            );
            collapsingToolbarLayout.setStatusBarScrimColor(
                    ContextCompat.getColor(this, R.color.colorPrimary)
            );
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentItem = tab.getPosition();
                Log.d(TAG, "onTabSelected: pos: " + currentItem);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupPalette(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @SuppressWarnings("ResourceType")
            @Override
            public void onGenerated(Palette palette) {

                int vibrantColor = palette.getVibrantColor(R.color.colorPrimary);
                int vibrantDarkColor = palette.getDarkVibrantColor(R.color.colorPrimaryDark);
                if (collapsingToolbarLayout != null) {
                    // We are on portrait mode and collapsing toolbar is in layout
                    collapsingToolbarLayout.setContentScrimColor(vibrantColor);
                    collapsingToolbarLayout.setStatusBarScrimColor(vibrantDarkColor);
                } else {
                    toolbar.setBackgroundColor(vibrantColor);
                    tabLayout.setBackgroundColor(vibrantColor);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_change_picture:
                dispatchTakePictureIntent(DEVICE_PICTURE_WIDTH, DEVICE_PICTURE_HEIGHT);
                return true;
            case R.id.action_change_name:
                showChangeNameDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showChangeNameDialog() {
        final EditText etName = new EditText(this);
        etName.setText(deviceProfile.getFriendlyName());
        AlertDialog.Builder changeName = new AlertDialog.Builder(this);
        changeName.setMessage(R.string.new_name);
        changeName.setTitle(R.string.change_name);

        changeName.setView(etName);

        changeName.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                deviceProfile.setFriendlyName(etName.getText().toString());
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(deviceProfile.getFriendlyName());
                }
                savedata();
            }
        });

        changeName.setNegativeButton(R.string.cancel, null);

        changeName.show();
    }

    private void savedata() {
        ttnApp.getDataStorage().getApplicationData().putProfile(devId, deviceProfile);
        ttnApp.getDataStorage().saveApplicationData(new AppDataSaveStatus() {
            @Override
            public void onSaveComplete() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DeviceActivity.this, R.string.data_saved, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onException(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DeviceActivity.this, R.string.data_save_error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public void onListFragmentInteraction(Frame frame) {
        Log.d(TAG, "Selected a frame from the list " + frame);
    }

    private static class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    protected void onPictureTaken(Bitmap picture) {
        deviceProfile.setPicture(picture);
        ivTabHeader.setImageBitmap(picture);
        setupPalette(picture);
        savedata();
    }
}
