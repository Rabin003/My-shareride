package com.levirgon.ridebuddy.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.levirgon.ridebuddy.application.App;

public class AppPreferenceHelper implements PreferenceHelper {

    private static final String KEY_USER_LOGGED_ID = "com.levirgon.ridebuddy.model.logged_in";

    private static Context sContext;
    private static AppPreferenceHelper mInstance;
    private static final String SHARED_PREF_NAME = "com.noushad.locationoffline.utils";

    private AppPreferenceHelper() {
        sContext = App.getApp().getApplicationContext();
    }

    public static synchronized AppPreferenceHelper getInstance() {
        if (mInstance == null) {
            mInstance = new AppPreferenceHelper();
        }
        return mInstance;
    }

    public boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = sContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.contains(KEY_USER_LOGGED_ID);
    }

    public void setUserLoggedIn() {
        SharedPreferences sharedPreferences = sContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_LOGGED_ID, KEY_USER_LOGGED_ID);
        editor.apply();
    }


}
