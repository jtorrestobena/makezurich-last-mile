package ch.makezurich.conqueringlastmile.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.makezurich.conqueringlastmile.R;
import ch.makezurich.conqueringlastmile.TTNApplication;
import ch.makezurich.conqueringlastmile.datastorage.AppDataSaveStatus;
import ch.makezurich.conqueringlastmile.datastorage.DeviceProfile;
import ch.makezurich.conqueringlastmile.fragment.FrameFragment;
import ch.makezurich.ttnandroidapi.datastorage.api.Frame;

public class DeviceActivity extends AppCompatActivity implements FrameFragment.OnFrameListFragmentInteractionListener {
    private static final String TAG = DeviceActivity.class.getSimpleName();

    private TTNApplication ttnApp;
    private String devId;
    private DeviceProfile deviceProfile;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView ivTabHeader;

    public static final String EXTRA_DEVICE_ID = "EXTRA_DEVICE_ID";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath;

    // Height and with for the device pictures
    private static final int DEVICE_PICTURE_WIDTH = 1280;
    private static final int DEVICE_PICTURE_HEIGHT = 720;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        ttnApp = (TTNApplication) getApplication();

        final Intent intent = getIntent();
        devId = intent.getStringExtra(EXTRA_DEVICE_ID);
        deviceProfile = ttnApp.getDataStorage().getApplicationData().getProfile(devId);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.htab_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            if (deviceProfile != null) {
                getSupportActionBar().setTitle(deviceProfile.getFriendlyName());
            } else {
                getSupportActionBar().setTitle(R.string.device);
            }
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.htab_viewpager);
        setupViewPager(viewPager);


        TabLayout tabLayout = (TabLayout) findViewById(R.id.htab_tabs);
        tabLayout.setupWithViewPager(viewPager);

        ivTabHeader = findViewById(R.id.htab_header);
        collapsingToolbarLayout = findViewById(R.id.htab_collapse_toolbar);

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
            Log.e(TAG, "onCreate: failed to create bitmap from background", e.fillInStackTrace());
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

                viewPager.setCurrentItem(tab.getPosition());
                Log.d(TAG, "onTabSelected: pos: " + tab.getPosition());

                switch (tab.getPosition()) {
                    case 0:
                        // TODO: Set Correct View
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void setupPalette(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @SuppressWarnings("ResourceType")
            @Override
            public void onGenerated(Palette palette) {

                int vibrantColor = palette.getVibrantColor(R.color.colorPrimary);
                int vibrantDarkColor = palette.getDarkVibrantColor(R.color.colorPrimaryDark);
                collapsingToolbarLayout.setContentScrimColor(vibrantColor);
                collapsingToolbarLayout.setStatusBarScrimColor(vibrantDarkColor);
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(FrameFragment.newInstance().setFrames(ttnApp.getFrames()), getString(R.string.frames));
        adapter.addFrag(FrameFragment.newInstance().setFrames(new ArrayList<Frame>()), getString(R.string.activations));
        adapter.addFrag(FrameFragment.newInstance().setFrames(new ArrayList<Frame>()), getString(R.string.locations));
        viewPager.setAdapter(adapter);
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
                dispatchTakePictureIntent();
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

    private static class ViewPagerAdapter extends FragmentPagerAdapter {
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

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "ch.makezurich.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "tmp";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            final Bitmap picture = adjustPicture(DEVICE_PICTURE_WIDTH, DEVICE_PICTURE_HEIGHT);
            deviceProfile.setPicture(picture);
            ivTabHeader.setImageBitmap(picture);
            setupPalette(picture);
            savedata();
            new File(mCurrentPhotoPath).delete();
        }
    }

    private Bitmap adjustPicture(int targetW, int targetH) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
    }
}
