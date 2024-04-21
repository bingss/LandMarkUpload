package com.example.LandMarkUpload.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class EncryptedSharedHelper {


    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;


    public EncryptedSharedHelper(Context context,String FileName) throws GeneralSecurityException, IOException {

        MasterKey masterKey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        sharedPreferences = EncryptedSharedPreferences.create(
                context,
                FileName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
        editor = sharedPreferences.edit();
    }

    public String getString(String key)
    {
        return sharedPreferences.getString(key,"");
    }

    public void putString(String key,String value)
    {
        editor.putString(key,value);
    }
    public void remove(String key)
    {
        editor.remove(key);
    }
    public void apply()
    {
        editor.apply();
    }


}
