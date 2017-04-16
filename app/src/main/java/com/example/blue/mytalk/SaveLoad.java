package com.example.blue.iamceo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by blue on 04/03/2017.
 */

public class SaveLoadPreferences {
    private Context context;
    public static final String TONG_TIEN = "TT";
    public static final String TONG_NGAY = "TN";
    public static final String TONG_NGUOI = "TNG";
    public static final String TONG_LV = "TL";
    public static final String NHAN_SU_NGUOI = "NSN";
    public static final String NHAN_SU_LV = "NSL";
    public static final String KY_THUAT_NGUOI = "KTN";
    public static final String KY_THUAT_LV = "KTL";
    public static final String NGHIEN_CUU_NGUOI = "NCN";
    public static final String NGHIEN_CUU_LV = "NCL";
    public static final String TEN_CONG_TY = "TCT";

    public SaveLoadPreferences(Context context) {
        this.context = context;
    }

    public void seveBoolean(String key, boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean loadBoolean(String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key, true);

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
