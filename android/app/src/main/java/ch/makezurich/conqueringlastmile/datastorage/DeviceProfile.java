package ch.makezurich.conqueringlastmile.datastorage;

import android.graphics.Bitmap;

import java.io.Serializable;

public class DeviceProfile implements Serializable {
    private String id;
    private String friendlyName;
    private Bitmap picture;

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
}
