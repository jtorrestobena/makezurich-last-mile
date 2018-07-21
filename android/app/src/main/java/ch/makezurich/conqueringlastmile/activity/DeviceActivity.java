package ch.makezurich.conqueringlastmile.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
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
import android.view.View;
import android.view.animation.DecelerateInterpolator;
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

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration;

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

        final ImageView expandedImage = findViewById(R.id.expanded_image);
        ivTabHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomImageFromThumb(view, expandedImage);
            }
        });

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        try {
            Bitmap bitmap = deviceProfile.getPicture();

            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.things_uno);
            } else {
                ivTabHeader.setImageBitmap(bitmap);
                expandedImage.setImageBitmap(bitmap);
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

    private void zoomImageFromThumb(final View thumbView, final ImageView expandedImageView) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.htab_maincontent)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }
}
