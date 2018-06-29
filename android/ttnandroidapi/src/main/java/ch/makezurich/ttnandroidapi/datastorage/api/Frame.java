package ch.makezurich.ttnandroidapi.datastorage.api;

import android.util.Base64;

import java.io.Serializable;
import java.math.BigInteger;

public class Frame implements Serializable {
    private String device_id;
    private String raw;
    private String time;
    private byte[] payload;
    private String hex;
    private String hexString;

    public String getDeviceId() {
        return device_id;
    }

    public String getRaw() {
        return raw;
    }

    public String getTimeStamp() {
        return time;
    }

    void parse() {
        if (raw != null) {
            payload = Base64.decode(raw, Base64.DEFAULT);
            // Assume 51 bytes
            hexString = String.format("%040x", new BigInteger(1, payload));
        }
    }

    public String getHexString() {
        return hexString;
    }
}
