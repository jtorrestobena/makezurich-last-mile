package ch.makezurich.ttnandroidapi.common;

import android.util.Base64;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigInteger;
import java.util.Locale;

public abstract class AbstractTTNPacket {
    // Put a colon separator
    protected static final String SEP_COLON = ":";
    protected static final String SEP_WHITESPACE = " ";
    private transient String hexString;

    protected abstract void parse();

    protected abstract byte[] getPayload();

    protected byte[] getPayloadBytes(String base64) {
        if (base64 == null) return null;

        return Base64.decode(base64, Base64.NO_WRAP);
    }

    protected String getPayloadBase64(byte[] payload) {
        if (payload == null) return null;

        return Base64.encodeToString(payload, Base64.NO_WRAP);
    }

    protected String formatHexString(byte[] payload, String insert) {
        if (payload == null) return null;

        return formatHexString(String.format("%02X", new BigInteger(1, payload)), insert);
    }

    protected String formatHexString(String hexStr, String insert) {
        if (hexStr == null) return null;

        return insertPeriodically(hexStr.toUpperCase(), insert, 2);
    }

    public String getPayloadHexString() {
        if (hexString == null) {
            hexString = formatHexString(getPayload(), SEP_COLON);
        }

        return hexString;
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

    public abstract DateTime getTimeStamp();

    public final String getTimestampString() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss.SSS")
                .withLocale(Locale.getDefault());
        return formatter.print( getTimeStamp() );
    }
}
