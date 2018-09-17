package com.levirgon.ridebuddy.entity;

public class MyRide {
    private  String mName;
    private  String mPhone;
    private  String mDestination;
    private  String mPickup;
    private String mUid;

    public void setName(String name) {
        mName = name;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public void setDestination(String destination) {
        mDestination = destination;
    }

    public void setPickup(String pickup) {
        mPickup = pickup;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public MyRide(String name, String phone, String destination, String pickup, String uid) {
        mName = name;
        mPhone = phone;
        mDestination = destination;
        mPickup = pickup;
        mUid = uid;
    }

    public MyRide() {
    }

    public String getName() {
        return mName;
    }

    public String getPhone() {
        return mPhone;
    }

    public String getDestination() {
        return mDestination;
    }

    public String getPickup() {
        return mPickup;
    }
}
