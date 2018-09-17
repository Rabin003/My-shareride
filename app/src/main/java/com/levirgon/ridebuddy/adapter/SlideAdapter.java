package com.levirgon.ridebuddy.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.levirgon.ridebuddy.R;

public class SlideAdapter extends PagerAdapter {

    private Context mContext;
    private LayoutInflater mInflater;


    public SlideAdapter(Context context) {
        mContext = context;
    }

    private int[] list_images = {
            R.drawable.image_3,
            R.drawable.image_2,
            R.drawable.image_1
    };

    private String[] list_titles = {
            "Share happiness",
            "Connect and stay synced",
            "Stay safe"
    };

    private String[] list_sub_titles = {
            "Share cost, time, and many more...",
            "Contact with your partner and also track his arrival using the app",
            "Know who you are travelling with, the in app video chat might be a good option"
    };


    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == (ConstraintLayout) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.intro_slide, container, false);
        TextView title = view.findViewById(R.id.slide_title);
        TextView subTitle = view.findViewById(R.id.slide_sub_title);
        ImageView image = view.findViewById(R.id.slide_image);

        title.setText(list_titles[position]);
        subTitle.setText(list_sub_titles[position]);
        image.setImageResource(list_images[position]);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout) object);
    }
}
