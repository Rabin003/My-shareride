package com.levirgon.ridebuddy.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.levirgon.ridebuddy.R;
import com.levirgon.ridebuddy.broadcastrecievers.NetworkStateReceiver;

public class SmartLocationTrackService extends Service implements SmartLocationInterface {

    public static final String ACTION_LOCATION_BROADCAST = "LOCATION_BROADCAST";
    public static final String EXTRA_LATITUDE = "LATITUDE_EXTRA";
    public static final String EXTRA_LONGITUDE = "LONGITUDE_EXTRA";
    public static final String EXTRA_COMMAND = "COMMAND";
    public static final String LOCATION_UPDATE = "location updated";
    public static final String PERMISSION_ABSENT = "permission absent";
    public static final String LOCATION_DISABLED = "location disabled";
    public static final String LOCATION_ENABLED = "location enabled";
    public static final String INTERNET_ENABLED = "internet enabled";
    public static final String INTERNET_DISABLED = "internet disabled";

    private static long INTERVAL = 1000 * 5;
    private static float DISTANCE = 0;
    private static final long LOCATION_TIMEOUT = 40000;
    private static final long COUNT_INTERVAL = 1000;
    private LocationManager locationManager;
    private FusedLocationProviderClient mFusedLocationClient;
    private NetworkStateReceiver networkStateReceiver;
    private boolean isGPSTurnedOn;
    private final String CHANNEL_NAME = "TRACKING_SERVICE";
    private boolean isDemandingNetworkSwitch;
    private boolean isLocationFound;
    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    private static final String TAG = "LocationTrackingService";
    private static final int NOTIFICATION_ID = 111;

    public SmartLocationTrackService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            switch (action) {
                case ACTION_START_FOREGROUND_SERVICE:
                    startForegroundService();
                    Toast.makeText(getApplicationContext(), "Foreground service is started.", Toast.LENGTH_LONG).show();
                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    stopForegroundService();
                    Toast.makeText(getApplicationContext(), "Foreground service is stopped.", Toast.LENGTH_LONG).show();
                    break;

            }
        }
        return START_STICKY;
    }

    private void stopForegroundService() {
        Log.d(TAG, "Stop foreground service.");
        unregisterReceiver(networkStateReceiver);
        locationManager.removeUpdates(this);
        stopForeground(true);
        stopSelf();
    }

    private void startForegroundService() {


        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        String CHANNEL_ID = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CHANNEL_ID = createNotificationChannel();
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle("Your service has started.");
        bigTextStyle.bigText("Phina");
        builder.setStyle(bigTextStyle);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.drawable.ic_car);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setFullScreenIntent(pendingIntent, true);
        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);

        setupGPSandNetworkListener();
        getLastLocation();
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel() {

        String channel_id = "TRACKER";
        String channel_name = TAG;
        NotificationChannel channel = new NotificationChannel(channel_id, channel_name, NotificationManager.IMPORTANCE_HIGH);
        channel.setLockscreenVisibility(MODE_PRIVATE);
        channel.setLightColor(Color.CYAN);
        NotificationManager service = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        service.createNotificationChannel(channel);
        return channel_id;
    }

    //======================================================

    @Override
    public void onLocationChanged(Location location) {
        if (location != null)
            updateLocationEvent(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        isGPSTurnedOn = true;
        getLastLocation();
        locationServiceEnabledEvent();
    }

    @Override
    public void onProviderDisabled(String s) {
        isGPSTurnedOn = false;

        locationServiceDisabledEvent();
    }

    @Override
    public void networkAvailable() {
        if (isDemandingNetworkSwitch && !isLocationFound) {
            startLocationService();
        }
        internetServiceEnabledEvent();
    }

    @Override
    public void networkUnavailable() {
        internetServiceDisabledEvent();
    }

    //======================================================

    private void getLastLocation() {
        Log.e(TAG, "getLastLocation: ");

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            permissionAbsentEvent();

            return;
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            updateLocationEvent(location);
                        }


                    }
                });

        startLocationService();
    }

    private void switchToGPSMode() {

        Log.e(TAG, "switchToGPSMode: ");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            permissionAbsentEvent();

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                INTERVAL,
                DISTANCE, this);
    }

    private void startLocationService() {
        Log.e(TAG, "startLocationService: ");

        if (!isLocationServiceAvailable())
            return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            permissionAbsentEvent();

            return;
        }

        if (isDemandingNetworkSwitch) {
            switchToNetWorkMode();
        } else {
            switchToGPSMode();
            new CountDownTimer(LOCATION_TIMEOUT, COUNT_INTERVAL) {

                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    if (!isNetworkAvailable()) {
                        isDemandingNetworkSwitch = true;
                    } else {
                        switchToNetWorkMode();
                    }
                }
            }.start();
        }


    }

    private void switchToNetWorkMode() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                INTERVAL,
                DISTANCE, this);

        Log.e(TAG, "switchToNetWorkMode: ");
    }

    private boolean isNetworkAvailable() {
        Log.e(TAG, "isNetworkAvailable: ");

        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager != null ? manager.getActiveNetworkInfo() : null;
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }

    private boolean isLocationServiceAvailable() {
        Log.e(TAG, "isLocationServiceAvailable: ");

        if (isGPSTurnedOn)
            return true;
        return false;
    }

    private void setupGPSandNetworkListener() {


        Log.e(TAG, "setupGPSandNetworkListener: ");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            permissionAbsentEvent();

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, this);

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }


    private void locationServiceDisabledEvent() {

        Log.e(TAG, "locationServiceDisabledEvent: ");

        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_COMMAND, LOCATION_DISABLED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void locationServiceEnabledEvent() {
        Log.e(TAG, "locationServiceEnabledEvent: ");
        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_COMMAND, LOCATION_ENABLED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void internetServiceEnabledEvent() {
        Log.e(TAG, "internetServiceEnabledEvent: ");
        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_COMMAND, INTERNET_ENABLED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void internetServiceDisabledEvent() {
        Log.e(TAG, "internetServiceDisabledEvent: ");
        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_COMMAND, INTERNET_DISABLED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void permissionAbsentEvent() {
        Log.e(TAG, "permissionAbsentEvent: ");
        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_COMMAND, PERMISSION_ABSENT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void updateLocationEvent(Location location) {
        Log.e(TAG, "updateLocationEvent: " + location.getLongitude() + " " + location.getLatitude());
        isLocationFound = true;
        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LATITUDE, String.valueOf(location.getLatitude()));
        intent.putExtra(EXTRA_LONGITUDE, String.valueOf(location.getLongitude()));
        intent.putExtra(EXTRA_COMMAND, LOCATION_UPDATE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
