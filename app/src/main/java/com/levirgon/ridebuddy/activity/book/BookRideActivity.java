package com.levirgon.ridebuddy.activity.book;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.levirgon.ridebuddy.R;
import com.levirgon.ridebuddy.activity.nearby.NearbyRidesActivity;
import com.levirgon.ridebuddy.broadcastrecievers.NetworkStateReceiver;
import com.levirgon.ridebuddy.entity.MyRide;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookRideActivity extends FragmentActivity
        implements OnMapReadyCallback, LocationListener, NetworkStateReceiver.NetworkStateReceiverListener {

    private static final long INTERVAL = 1000;
    private static final float DISTANCE = 1;
    private GoogleMap mMap;
    private static final int ACCESS_LOCATION_REQUEST = 101;
    private static final String TAG = "BookRideActivity";
    private AlertDialog mNetWorkDialog;
    private AlertDialog mGpsDialog;
    private ProgressDialog mLocationProgressDialog;
    private Geocoder mGeocoder;
    private GoogleMap.OnCameraIdleListener onCameraIdleListener;
    private Location mLocation;
    static boolean isActive = false;
    private FirebaseDatabase database;
    private DatabaseReference dataRef;
    private DatabaseReference pickupLocationRef;
    private DatabaseReference destinationLocationRef;
    private GeoFire mPickupSetter;
    private GeoFire mDestinationSetter;
    private FirebaseUser mUser;

    @BindView(R.id.text_pickup_point)
    TextView pickupTextView;
    @BindView(R.id.text_destination)
    TextView destinationTextView;
    @BindView(R.id.radio_pickup)
    RadioButton pickupRadioButton;
    @BindView(R.id.radio_destination)
    RadioButton destinationRadioButton;
    @BindView(R.id.input_name)
    EditText nameInput;
    @BindView(R.id.input_phone_number)
    EditText phoneInput;
    @BindView(R.id.info_input_card)
    CardView infoCard;
    @BindView(R.id.book_button)
    Button bookButton;


    private LatLng pickupLocationLatLng;
    private LatLng destinationLatLng;
    private NetworkStateReceiver networkStateReceiver;
    private LocationManager locationManager;
    private String UID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_ride);
        ButterKnife.bind(this);
        initializeViews();
        setupMAP();
        startLocationProvider();


    }

    private void initializeViews() {
        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRideInformation();
            }
        });
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        UID = mUser.getUid();
        database = FirebaseDatabase.getInstance();

        dataRef = database.getReference().child("information").child(UID).child("ride_information");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        pickupLocationRef = database.getReference().child("gps").child("pickup_location");
        destinationLocationRef = database.getReference().child("gps").child("destination_location");

        mPickupSetter = new GeoFire(pickupLocationRef);
        mDestinationSetter = new GeoFire(destinationLocationRef);

        buildNetworkDialog();
        buildGpsDialog();
        buildProgressDialog();
    }

    private void getRideInformation() {

        infoCard.setVisibility(View.VISIBLE);

        String name = nameInput.getText().toString();
        String phone = phoneInput.getText().toString();

        if (name.trim().isEmpty() || phone.trim().isEmpty()) {
            showToast("Please fill up all fields");
        } else {
            postMyRide(name, phone);
            infoCard.setVisibility(View.GONE);
        }

    }

    private void buildProgressDialog() {
        mLocationProgressDialog = new ProgressDialog(this);
        mLocationProgressDialog.setMessage("Getting your location");
        mLocationProgressDialog.setCancelable(false);
    }

    private void buildGpsDialog() {
        mGpsDialog = new AlertDialog.Builder(this).create();
        mGpsDialog.setTitle("Enable Location");
        mGpsDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
        mGpsDialog.setButton(DialogInterface.BUTTON_POSITIVE, "SETTING", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        mGpsDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "DISMISS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        mGpsDialog.setCancelable(false);
    }

    private void buildNetworkDialog() {
        mNetWorkDialog = new AlertDialog.Builder(this).create();
        mNetWorkDialog.setTitle("Turn on Internet connection");
        mNetWorkDialog.setMessage("Your internet setting is not enabled. Please enabled it in settings menu.");
        mNetWorkDialog.setButton(DialogInterface.BUTTON_POSITIVE, "SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
            }
        });
        mNetWorkDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "DISMISS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        mNetWorkDialog.setCancelable(false);
    }


    private void setupMAP() {
        Log.e(TAG, "setupMAP: ");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mGeocoder = new Geocoder(this);
        configureCameraIdle();
    }

    private void configureCameraIdle() {
        Log.e(TAG, "configureCameraIdle: ");
        onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                //  showToast("Camera Idle");

                LatLng latLng = mMap.getCameraPosition().target;

                Geocoder geocoder = new Geocoder(BookRideActivity.this);

                try {
                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        String locality = addressList.get(0).getAddressLine(0);
                        String country = addressList.get(0).getCountryName();
                        if (!locality.isEmpty() && !country.isEmpty())
                            setSelectedLocation(locality, latLng);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
    }

    private void setSelectedLocation(String locality, LatLng latLng) {
        if (pickupRadioButton.isChecked()) {
            pickupTextView.setText(locality);
            pickupLocationLatLng = latLng;
        } else if (destinationRadioButton.isChecked()) {
            destinationTextView.setText(locality);
            destinationLatLng = latLng;
        }

    }

    private void updateLocation(Location location) {
        Log.e(TAG, "updateLocation: ");

        if (mNetWorkDialog != null)
            mNetWorkDialog.dismiss();

        if (location != null) {
            mLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraIdleListener(onCameraIdleListener);
    }

    private void startLocationProvider() {


        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_LOCATION_REQUEST);

            return;
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVAL, DISTANCE, BookRideActivity.this);
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(BookRideActivity.this);
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.e(TAG, "onRequestPermissionsResult: ");
        switch (requestCode) {
            case ACCESS_LOCATION_REQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast("Permission granted");
                    startLocationProvider();

                } else {
                    showToast("Cannot work without permission");
                }
                return;


        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void postMyRide(String name, String phone) {
        if ((destinationLatLng == null) || (pickupLocationLatLng == null)) {
            showToast("Please select your start location and destination correctly");
            return;
        }

        dataRef.setValue(new MyRide(name, phone, destinationTextView.getText().toString(),
                pickupTextView.getText().toString(), UID));

        mPickupSetter.setLocation(UID, new GeoLocation(pickupLocationLatLng.latitude,
                pickupLocationLatLng.longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    showToast(error.getCode() + " // " + error.getMessage());
                }
            }
        });

        mDestinationSetter.setLocation(UID, new GeoLocation(destinationLatLng.latitude,
                destinationLatLng.longitude), new GeoFire.CompletionListener() {

            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    showToast(error.getCode() + " // " + error.getMessage());
                } else {
                    showToast("Your ride has been registered");
                    showNearbyRides();
                }
            }
        });

    }

    private void showNearbyRides() {

        Intent intent = new Intent(this, NearbyRidesActivity.class);
        intent.putExtra("DESTINATION", destinationLatLng);
        intent.putExtra("PICKUP", pickupLocationLatLng);
        startActivity(intent);


    }

    @Override
    public void onLocationChanged(Location location) {
        locationManager.removeUpdates(this);
        updateLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkStateReceiver);
    }

    @Override
    public void onProviderEnabled(String s) {
        mGpsDialog.dismiss();
        showToast("Getting your location");
    }

    @Override
    public void onProviderDisabled(String s) {
        mGpsDialog.show();
    }

    @Override
    public void networkAvailable() {
        mNetWorkDialog.dismiss();
        Log.e(TAG, "networkAvailable: ");
    }

    @Override
    public void networkUnavailable() {
        mNetWorkDialog.show();
        Log.e(TAG, "networkUnavailable: ");
    }
}
