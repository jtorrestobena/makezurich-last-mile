package ch.makezurich.conqueringlastmile.packetclass;

import java.io.Serializable;

import ch.makezurich.ttnandroidapi.mqtt.api.data.Packet;

/**
 * Always implement Serislizable if you plan to pass the object between notifications, activities ...
 * Also useful when data must be saved to disk
 *
 * Remember to set the packet class in the AndroidMQTTClient
  */
public class DemoPayloadPacket extends Packet implements Serializable {
    /*
    {"payload_fields":
            {"altitude":0,
            "fix":0,
            "latitude":0,
            "longitude":0,
            "satellites":18}*/

    public static class PayloadFields implements Serializable {
        private Double altitude;
        private Double fix;
        private Double latitude;
        private Double longitude;
        private Integer satellites;

        public double getAltitude() { return altitude; }
        public double getFix() { return fix; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public int getSatellites() { return satellites; }
    }

    private PayloadFields payload_fields;

    public PayloadFields getPayloadFields() {
        return payload_fields;
    }
}
