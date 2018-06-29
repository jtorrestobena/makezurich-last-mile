package ch.makezurich.ttnandroidapi.datastorage.api;

public class Device {
    private String devName;
    public Device(String devName) {
        this.devName = devName;
    }

    public String getName() {
        return devName;
    }
}
