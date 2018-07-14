package ch.makezurich.conqueringlastmile.datastorage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ApplicationData implements Serializable {
    private Map<String, DeviceProfile> deviceProfileMap = new HashMap<>();

    public DeviceProfile getProfile(String id) {
        DeviceProfile dp = deviceProfileMap.get(id);
        if (dp == null) {
            dp = new DeviceProfile(id);
        }
        return dp;
    }

    public void putProfile(String id, DeviceProfile profile) {
        deviceProfileMap.put(id, profile);
    }
}
