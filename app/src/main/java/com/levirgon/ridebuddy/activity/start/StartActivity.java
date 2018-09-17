package com.levirgon.ridebuddy.activity.start;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.levirgon.ridebuddy.R;
import com.levirgon.ridebuddy.activity.book.BookRideActivity;
import com.levirgon.ridebuddy.adapter.SlideAdapter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;

public class StartActivity extends AppCompatActivity implements View.OnClickListener, IStartActivity {

    private static final int PHONE_AUTH = 111;
    private static final int GOOGLE_AUTH = 222;
    private static final int FACEBOOK_AUTH = 333;

    //=============================================================================================>

    private Button mSignInButton;
    private CircleIndicator mCircleIndicator;
    private ImageView mFacebookLoginButton;
    private ImageView mGogleLoginButton;

    private StartActivityListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListener = StartActivityPresenter.getInstance();
        if (mListener.isUserLoggedIn()) {
            startActivity(new Intent(this, BookRideActivity.class));
            finish();
        }


        setContentView(R.layout.activity_start);
        initializeViews();
        setupSlide();
    }

    private void setupSlide() {
        ViewPager viewPager = findViewById(R.id.app_intro_slider);
        SlideAdapter slideAdapter = new SlideAdapter(this);
        viewPager.setAdapter(slideAdapter);
        mCircleIndicator.setViewPager(viewPager);

    }

    private void initializeViews() {
        mSignInButton = findViewById(R.id.get_started_button);
        mSignInButton.setOnClickListener(this);
        mCircleIndicator = findViewById(R.id.slide_indicator);
        mFacebookLoginButton = findViewById(R.id.facebook_login);
        mFacebookLoginButton.setOnClickListener(this);
        mGogleLoginButton = findViewById(R.id.google_login);
        mGogleLoginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.get_started_button:
                attemptPhoneLogin(PHONE_AUTH);
                break;

        }

    }

    private static final int RC_SIGN_IN = 123;

    List<AuthUI.IdpConfig> googleAuthProvider = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());
    List<AuthUI.IdpConfig> phoneAuthProvider = Collections.singletonList(new AuthUI.IdpConfig.PhoneBuilder().build());
//    List<AuthUI.IdpConfig> fbAuthProvider = Collections.singletonList(new AuthUI.IdpConfig.FacebookBuilder().build());

    private void attemptPhoneLogin(int type) {

        switch (type) {
            case PHONE_AUTH:
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(phoneAuthProvider)
                                .build(),
                        RC_SIGN_IN);
                break;
            case GOOGLE_AUTH:
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(googleAuthProvider)
                                .build(),
                        RC_SIGN_IN);
                break;
            case FACEBOOK_AUTH:
                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();

                break;
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.successfull, Toast.LENGTH_SHORT).show();

                mListener.setUserLoggedInValue();
                startActivity(new Intent(this,BookRideActivity.class));


            } else {

                if (response != null) {
                    Toast.makeText(this, R.string.failed, Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, response.getError().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, R.string.please_continue, Toast.LENGTH_SHORT).show();
                }


                // ...
            }
        }
    }


}

