package com.levirgon.ridebuddy.Interface;

public interface LoginCallbacksListener {

    void onSuccess();

    void onError(int errorCode, String errorMessage);

}
