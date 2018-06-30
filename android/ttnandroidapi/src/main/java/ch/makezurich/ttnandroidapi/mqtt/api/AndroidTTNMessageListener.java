package ch.makezurich.ttnandroidapi.mqtt.api;

public interface AndroidTTNMessageListener {
    void onSuccess();
    void onError(Throwable _error);
}
