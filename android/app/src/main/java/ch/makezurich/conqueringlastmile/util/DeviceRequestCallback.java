package ch.makezurich.conqueringlastmile.util;

import ch.makezurich.ttnandroidapi.datastorage.api.TTNDataStorageApi;

public interface DeviceRequestCallback {
    void onDevicesLoaded();
    void onTTNException(TTNDataStorageApi.TTNDataException ttnExc);
}
