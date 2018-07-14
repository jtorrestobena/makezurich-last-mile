package ch.makezurich.conqueringlastmile.datastorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;

public class DataStorage {

    private static final String TAG = "DataStorage";

    private static final String KEY_STORE = "KEY_STORE";
    private static final String transformation = "AES/ECB/PKCS5Padding";
    private static final int KEY_LEN = 16;
    private static String key;
    private static ApplicationData applicationData = new ApplicationData();

    private static final String dataFileName = "ttnappdata.db";
    private Context context;

    public DataStorage(Context context, SharedPreferences preferences) {
        this.context = context;
        key = preferences.getString(KEY_STORE, null);
        if (key == null) {
            key = new RandomString(KEY_LEN).nextString();
            preferences.edit().putString(KEY_STORE, key).apply();
        } else {
            loadApplicationData();
            if (applicationData == null) {
                Log.d(TAG, "Application data is empty");
            }
        }
    }

    private void loadApplicationData() {
        try {
            applicationData = (ApplicationData) decrypt(new FileInputStream(new File(context.getFilesDir(), dataFileName)));
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public void saveApplicationData(final AppDataSaveStatus status) {
        new Thread() {
            @Override
            public void run() {
                try {
                    encrypt(applicationData, new FileOutputStream(new File(context.getFilesDir(), dataFileName)));
                } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
                    e.printStackTrace();
                    status.onException(e);
                }

                status.onSaveComplete();
            }
        }.start();
    }

    private static void encrypt(Serializable object, OutputStream ostream) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        try {
            // Length is 16 byte
            SecretKeySpec sks = new SecretKeySpec(key.getBytes(), transformation);

            // Create cipher
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, sks);
            SealedObject sealedObject = new SealedObject(object, cipher);

            // Wrap the output stream
            CipherOutputStream cos = new CipherOutputStream(ostream, cipher);
            ObjectOutputStream outputStream = new ObjectOutputStream(cos);
            outputStream.writeObject(sealedObject);
            outputStream.close();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    private static Object decrypt(InputStream istream) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        SecretKeySpec sks = new SecretKeySpec(key.getBytes(), transformation);
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, sks);

        CipherInputStream cipherInputStream = new CipherInputStream(istream, cipher);
        ObjectInputStream inputStream = new ObjectInputStream(cipherInputStream);
        SealedObject sealedObject;
        try {
            sealedObject = (SealedObject) inputStream.readObject();
            return sealedObject.getObject(cipher);
        } catch (ClassNotFoundException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ApplicationData getApplicationData() {
        return applicationData;
    }
}
