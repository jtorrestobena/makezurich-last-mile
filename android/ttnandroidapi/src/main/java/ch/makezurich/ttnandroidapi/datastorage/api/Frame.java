package ch.makezurich.ttnandroidapi.datastorage.api;

import org.joda.time.DateTime;

import java.io.Serializable;

import ch.makezurich.ttnandroidapi.common.AbstractTTNPacket;
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

public class Frame extends AbstractTTNPacket implements Serializable {
    private String device_id;
    private transient String raw;
    private DateTime time;
    private byte[] payload;

    public String getDeviceId() {
        return device_id;
    }

    public String getRaw() {
        return raw;
    }

    @Override
    public DateTime getTimeStamp() {
        return time;
    }

    @Override
    protected void parse() {
        payload = getPayloadBytes(raw);
    }

    @Override
    protected byte[] getPayload() {
        return payload;
    }
}
