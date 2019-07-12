package com.sorter.putrialutfi.sorter.Session;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPrefs {

    SharedPreferences mSharedPreferences;

    public static SharedPrefs instance = null;

    public static SharedPrefs getInstance() {

        if (instance == null) {
            synchronized (SharedPrefs.class) {
                instance = new SharedPrefs();
            }
        }
        return instance;
    }
    public void isLogedIn(Context context, Boolean isLoggedin) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("IS_LOGIN", isLoggedin);
        editor.commit();
    }

    public boolean getLogedIn(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return mSharedPreferences.getBoolean("IS_LOGIN", false);
    }
}
