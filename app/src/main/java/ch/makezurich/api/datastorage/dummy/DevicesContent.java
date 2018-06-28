package ch.makezurich.api.datastorage.dummy;

import java.util.ArrayList;
import java.util.List;

import ch.makezurich.api.datastorage.Device;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 */
public class DevicesContent {

    /**
     * An array of Device items.
     */
    private List<Device> deviceList = new ArrayList<Device>();

    public DevicesContent(List<Device> deviceList) {
        this.deviceList = deviceList;
    }


    public void addItem(Device item) {
        deviceList.add(item);
    }
}
