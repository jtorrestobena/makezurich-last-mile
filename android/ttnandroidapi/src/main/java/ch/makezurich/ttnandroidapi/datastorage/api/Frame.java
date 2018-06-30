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
            hexString = formatHexString();
        }
    }

    public String getHexString() {
        return hexString;
    }

    protected String formatHexString() {
        return insertPeriodically((String.format("%040x", new BigInteger(1, payload))).toUpperCase(), ":", 2);
    }

    private static String insertPeriodically(
            String text, String insert, int period)
    {
        StringBuilder builder = new StringBuilder(
                text.length() + insert.length() * (text.length()/period)+1);

        int index = 0;
        String prefix = "";
        while (index < text.length())
        {
            // Don't put the insert in the very first iteration.
            // This is easier than appending it *after* each substring
            builder.append(prefix);
            prefix = insert;
            builder.append(text.substring(index,
                    Math.min(index + period, text.length())));
            index += period;
        }
        return builder.toString();
    }
}
