package com.levirgon.ridebuddy.activity.start;

import com.levirgon.ridebuddy.Interface.DataManager;
import com.levirgon.ridebuddy.model.AppDataManager;

public class StartActivityPresenter implements StartActivityListener {

    private static StartActivityPresenter mInstance;
    private DataManager mDataManager;

    public StartActivityPresenter() {
        mDataManager = AppDataManager.getInstance();

    }

    public static synchronized StartActivityPresenter getInstance() {

        if (mInstance == null) {
            mInstance = new StartActivityPresenter();
        }
        return mInstance;
    }

    @Override
    public void setUserLoggedInValue() {
        mDataManager.setUserLoggedIn();
    }

    @Override
    public boolean isUserLoggedIn() {
        return mDataManager.isUserLoggedIn();
    }
}
