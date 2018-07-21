package ch.makezurich.conqueringlastmile.util;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileInputOutput {
    public interface FileIOWriteCallback {
        void onSaveComplete();
        void onException(Exception ex);
    }

    public interface FileIOReadCallback {
        void onReadComplete(Object object);
        void onException(Exception ex);
    }

    public static void write(final Object object, final File file, @NonNull final FileIOWriteCallback callback) {
        new Thread() {
            @Override
            public void run() {
                ObjectOutputStream oos = null;
                FileOutputStream fout = null;
                try{
                    fout = new FileOutputStream(file);
                    oos = new ObjectOutputStream(fout);
                    oos.writeObject(object);
                    callback.onSaveComplete();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    callback.onException(ex);
                } finally {
                    if(oos != null) try {
                        oos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        callback.onException(ioe);
                    }
                }
            }
        }.start();
    }

    public static void read(final File file, @NonNull final FileIOReadCallback callback) {
        new Thread() {
            @Override
            public void run() {
                ObjectInputStream objectinputstream = null;
                try {
                    FileInputStream streamIn = new FileInputStream(file);
                    objectinputstream = new ObjectInputStream(streamIn);
                    callback.onReadComplete(objectinputstream.readObject());
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onException(e);
                } finally {
                    if(objectinputstream != null) try {
                        objectinputstream.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        callback.onException(ioe);
                    }
                }
            }
        }.start();
    }
}
