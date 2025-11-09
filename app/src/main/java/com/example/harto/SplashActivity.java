package com.example.harto;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.harto.user_registration.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);
        Class<?> nextActivity = sessionManager.isLoggedIn()
                ? MainActivity.class
                : LoginActivity.class;


        startActivity(new Intent(this, nextActivity));
        overridePendingTransition(0, 0);
        finish();
    }
}
