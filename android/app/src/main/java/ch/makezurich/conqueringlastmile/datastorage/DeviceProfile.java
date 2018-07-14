package ch.makezurich.conqueringlastmile.datastorage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class DeviceProfile implements Serializable {
    private String id;
    private String friendlyName;
    private transient Bitmap picture;
    private byte[] pictureData;

    public DeviceProfile(String devId) {
        this.id = devId;
        this.friendlyName = devId;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public String getId() {
        return id;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        picture.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        pictureData = stream.toByteArray();
    }

    public Bitmap getPicture() {
        if (picture == null && pictureData != null) {
            picture = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
        }

        return picture;
    }
}
