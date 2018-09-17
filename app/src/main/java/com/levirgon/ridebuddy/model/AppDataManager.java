package com.levirgon.ridebuddy.model;

import com.levirgon.ridebuddy.Interface.DataManager;

public class AppDataManager implements DataManager {

    private static AppDataManager mInstance;
    private PreferenceHelper mPreferenceHelper;

    private AppDataManager() {
        mPreferenceHelper = AppPreferenceHelper.getInstance();
    }

    public static synchronized DataManager getInstance() {

        if (mInstance == null) {
            mInstance = new AppDataManager();
        }

        return mInstance;
    }


    @Override
    public boolean isUserLoggedIn() {
        return mPreferenceHelper.isUserLoggedIn();
    }

    @Override
    public void setUserLoggedIn() {
        mPreferenceHelper.setUserLoggedIn();
    }


}
