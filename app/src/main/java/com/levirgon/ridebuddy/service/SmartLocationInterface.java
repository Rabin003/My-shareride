package com.levirgon.ridebuddy.service;

import android.location.LocationListener;

import com.levirgon.ridebuddy.broadcastrecievers.NetworkStateReceiver;

public interface SmartLocationInterface extends LocationListener , NetworkStateReceiver.NetworkStateReceiverListener {
}
