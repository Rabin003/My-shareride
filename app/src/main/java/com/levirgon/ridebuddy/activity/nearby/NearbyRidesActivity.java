package com.levirgon.ridebuddy.activity.nearby;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.levirgon.ridebuddy.R;
import com.levirgon.ridebuddy.adapter.NearbyRidesAdapter;
import com.levirgon.ridebuddy.entity.MyRide;

import java.util.ArrayList;

public class NearbyRidesActivity extends AppCompatActivity {

    private static final double RADIUS = 3; //km
    private FirebaseDatabase database;
    private DatabaseReference dataRef;
    private DatabaseReference pickupLocationRef;
    private DatabaseReference destinationLocationRef;
    private GeoFire mPickupSetter;
    private GeoFire mDestinationSetter;

    private LatLng destination;
    private LatLng pickup;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private NearbyRidesAdapter mAdapter;
    private ArrayList<MyRide> pickupList = new ArrayList<>();
    private ArrayList<MyRide> mRideList = new ArrayList<>();
    private static final String TAG = "NearbyRidesActivity";
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_rides);
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        Bundle extras = getIntent().getExtras();
        Intent incomingIntent = getIntent();

        if (extras != null) {
            destination = (LatLng) incomingIntent.getParcelableExtra("DESTINATION");
            Log.e(TAG, "onCreate: " + String.valueOf(destination));
            pickup = (LatLng) incomingIntent.getParcelableExtra("PICKUP");
            Log.e(TAG, "onCreate: " + String.valueOf(pickup));
        }

        Log.e(TAG, "onCreate: ");

        database = FirebaseDatabase.getInstance();

        dataRef = database.getReference().child("information");

        pickupLocationRef = database.getReference().child("gps").child("pickup_location");
        destinationLocationRef = database.getReference().child("gps").child("destination_location");

        mPickupSetter = new GeoFire(pickupLocationRef);
        mDestinationSetter = new GeoFire(destinationLocationRef);

        setupList();

        getNearbyRides();

    }

    private void getNearbyRides() {

        Log.e(TAG, "getNearbyRides: ");

        GeoQuery geoQuery = mPickupSetter.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), RADIUS);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                dataRef.child(key).child("ride_information").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        MyRide ride = dataSnapshot.getValue(MyRide.class);
                        pickupList.add(ride);

                        Log.e(TAG, "onDataChanged: Received pickup data " + pickupList.size());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                Log.e(TAG, "onGeoQueryReady: ");
                getRides2();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void getRides2() {
        GeoQuery geoQuery = mDestinationSetter.queryAtLocation(new GeoLocation(destination.latitude, destination.longitude), RADIUS);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                dataRef.child(key).child("ride_information").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        MyRide ride = dataSnapshot.getValue(MyRide.class);
                        //destinationList.add(ride);
                        Log.e(TAG, "onDataChanged: Received destination data ");

                        addToList(ride);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                Log.e(TAG, "onGeoQueryReady:2 ");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void addToList(MyRide ride) {

        Log.e(TAG, "addToList: ");


        for (int i = 0; i < pickupList.size(); i++) {

            {
                if (ride.getUid() != null && pickupList.get(i).getUid() != null)
                    if (pickupList.get(i).getUid().equalsIgnoreCase(ride.getUid()))
                        if (!ride.getUid().equalsIgnoreCase(mUser.getUid()))
                            mAdapter.addItems(ride);
            }


        }
    }

    private void setupList() {
        mRecyclerView = findViewById(R.id.nearby_rides_list);
        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new NearbyRidesAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void makeCall(String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
        startActivity(intent);
    }
}

