package ch.makezurich.ttnandroidapi.common;

import android.util.Base64;

import java.math.BigInteger;

public abstract class AbstractTTNPacket {
    protected abstract void parse();

    // Put a colon separator
    protected static final String SEP_COLON = ":";
    protected static final String SEP_WHITESPACE = " ";

    protected byte[] getPayloadBytes(String base64) {
        if (base64 == null) return null;

        return Base64.decode(base64, Base64.DEFAULT);
    }

    protected String formatHexString(byte[] payload, String insert) {
        if (payload == null) return null;

        return insertPeriodically((String.format("%02X", new BigInteger(1, payload))).toUpperCase(), insert, 2);
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
