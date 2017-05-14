package com.example.blue.mytalk;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by blue on 04/03/2017.
 */

public class SaveLoad {
    private Context context;
    public static final String NAME = "name";
    public static final String OLD = "old";
    public static final String SEX = "sex";
    public static final String LANGUAGE = "language";
    public static final String CHECKPASS = "check";
    public static final String PASS = "pass";
    public static final String IS_CONNECT = "isconnect";
    public static final String ID_CONNECT = "idconnect";
    public static final String CBOY = "boy";
    public static final String CGIRL = "girl";
    public static final String CLESBIAN = "lesbian";
    public static final String CGAY = "gay";
    public static final String COLD = "cold";
    public static final String TO_OLD = "Told";
    public static final String FROM_COLD = "Fold";
    public static final String C_NAME = "cname";
    public static final String ONLINE = "online";
    public static final String UID = "uid";
    public static final String LOVE = "love";

    public static final int BOY = 1;
    public static final int GIRL = 0;
    public static final int LESBIAN = 2;
    public static final int GAY = 3;

    public SaveLoad(Context context) {
        this.context = context;
    }

    public void seveBoolean(String key, boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean loadBoolean(String key, boolean df) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key, df);

    }

    public void saveInteger(String key, int value) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int loadInteger(String key, int df) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(key, df);
    }

    public void saveString(String key, String value) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String loadString(String key, String df) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, df);
    }
}
