package com.inventory.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.inventory.R;
import com.inventory.constants.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SplashActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference(Constants.Profile);

        new Handler(Looper.getMainLooper()).postDelayed(this::fetchProfile, 1000);
    }

    // Fetch profile whether user already logged in or not.
    // If yes switch to Home screen else switch to Login screen
    private void fetchProfile() {
        databaseReference.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                switchActivity(LoginActivity.class);
            } else {
                //noinspection ConstantConditions
                if (task.getResult().exists()) {
                    String login = "" + task.getResult().child(Constants.Login).getValue();
                    if (login.equals(Constants.True)) {
                        switchActivity(MainActivity.class);
                    } else {
                        switchActivity(LoginActivity.class);
                    }
                } else {
                    switchActivity(LoginActivity.class);
                }
            }
        });
    }

    @SuppressWarnings("rawtypes")
    private void switchActivity(Class mClass) {
        startActivity(new Intent(SplashActivity.this, mClass));
        finish();
    }
}
