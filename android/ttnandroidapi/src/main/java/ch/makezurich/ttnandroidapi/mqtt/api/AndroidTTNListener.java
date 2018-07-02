package ch.makezurich.ttnandroidapi.mqtt.api;

import ch.makezurich.ttnandroidapi.mqtt.api.data.Packet;

public interface AndroidTTNListener {
    void onError(Throwable _error);
    void onTLSError(Throwable _error);
    void onConnected(boolean _reconnect);
    void onPacket(Packet _message);
}
