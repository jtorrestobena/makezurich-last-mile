package ch.makezurich.ttnandroidapi.mqtt.api;

public interface AndroidTTNListener {
    void onError(Throwable _error);
    void onConnected(boolean _reconnect);
    void onMessage(String _message);
}
