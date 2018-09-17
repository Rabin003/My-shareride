package com.levirgon.ridebuddy.application;

import android.app.Application;

public class App extends Application {

    private static App SApp;

    public synchronized static App getApp() {
        return SApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (SApp == null)
            SApp = this;

    }

}
