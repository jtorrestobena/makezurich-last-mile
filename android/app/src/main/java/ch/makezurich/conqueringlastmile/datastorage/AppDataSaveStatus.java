package ch.makezurich.conqueringlastmile.datastorage;

public interface AppDataSaveStatus {
    void onSaveComplete();
    void onException(Exception e);
}
