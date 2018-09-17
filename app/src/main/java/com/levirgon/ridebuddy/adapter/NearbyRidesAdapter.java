package com.levirgon.ridebuddy.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.levirgon.ridebuddy.R;
import com.levirgon.ridebuddy.activity.nearby.NearbyRidesActivity;
import com.levirgon.ridebuddy.entity.MyRide;

import java.util.ArrayList;
import java.util.List;

public class NearbyRidesAdapter extends RecyclerView.Adapter {

    private List<MyRide> items;
    private Context mContext;

    public NearbyRidesAdapter(Context context) {
        mContext = context;
        this.items = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View viewItem = inflater.inflate(R.layout.my_ride_item, parent, false);
        viewHolder = new MyRideVH(viewItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((MyRideVH) holder).bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void addItems(List<MyRide> newItems) {

        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void addItems(MyRide item){
        items.add(item);
        notifyDataSetChanged();
    }

    private class MyRideVH extends RecyclerView.ViewHolder {

        TextView name;
        TextView to;
        TextView from;
        Button callButton;

        public MyRideVH(View v) {
            super(v);

            name = v.findViewById(R.id.mri_name);
            from = v.findViewById(R.id.mri_from);
            to = v.findViewById(R.id.mri_to);
            callButton = v.findViewById(R.id.mri_call);
        }

        public void bind(final MyRide ride) {
            name.setText(ride.getName());
            to.setText(ride.getDestination());
            from.setText(ride.getPickup());
            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((NearbyRidesActivity) mContext).makeCall(ride.getPhone());
                }
            });
        }
    }
}
