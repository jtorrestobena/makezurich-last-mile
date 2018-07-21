package ch.makezurich.conqueringlastmile.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import ch.makezurich.conqueringlastmile.R;

public abstract class PhotoActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String PHOTO_PATH = "PHOTO_PATH";
    private String mCurrentPhotoPath;
    private int pictureWidth;
    private int pictureHeight;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(PHOTO_PATH, mCurrentPhotoPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PHOTO_PATH)) {
                mCurrentPhotoPath = savedInstanceState.getString(PHOTO_PATH);
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
    }


    protected void dispatchTakePictureIntent(int pictureWidth, int pictureHeight) {
        this.pictureWidth = pictureWidth;
        this.pictureHeight = pictureHeight;

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
        Bitmap picture = null;
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) try {
            picture = resizePicture();
            picture = checkOrientation(picture);
            if (picture != null) {
                onPictureTaken(picture);
            }
            new File(mCurrentPhotoPath).delete();
        } catch (Exception e) {
            if (picture == null) {
                Toast.makeText(this, R.string.get_picture_failed, Toast.LENGTH_LONG).show();
            }
            e.printStackTrace();
        }
    }

    private Bitmap checkOrientation(Bitmap bitmap) {
        try {
            ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            Bitmap rotatedBitmap = null;
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }

            return rotatedBitmap;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return bitmap;
    }

    private Bitmap resizePicture() {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/pictureWidth, photoH/pictureHeight);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    protected abstract void onPictureTaken(Bitmap picture);
}
