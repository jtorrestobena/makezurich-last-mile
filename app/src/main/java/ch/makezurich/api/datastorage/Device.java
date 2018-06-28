package ch.makezurich.api.datastorage;

public class Device {
    private String devName;
    public Device(String devName) {
        this.devName = devName;
    }

    public String getName() {
        return devName;
    }
}
