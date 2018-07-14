package ch.makezurich.ttnandroidapi.datastorage.api;

import android.util.Base64;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Locale;
/*
 * Copyright 2018 Jose Antonio Torres Tobena / bytecoders
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Frame implements Serializable {
    private String device_id;
    private String raw;
    private DateTime time;
    private byte[] payload;
    private String hex;
    private String hexString;

    public String getDeviceId() {
        return device_id;
    }

    public String getRaw() {
        return raw;
    }

    public DateTime getTimeStamp() {
        return time;
    }

    public String getTimestampString() {
        DateTimeFormatter formatter = DateTimeFormat.forStyle("LL")
                .withLocale(Locale.getDefault());
        return formatter.print( time );
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
        return insertPeriodically((String.format("%02X", new BigInteger(1, payload))).toUpperCase(), ":", 2);
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
