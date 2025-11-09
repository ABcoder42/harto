package com.example.harto;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.harto.user_registration.LoginActivity;
import com.example.harto.user_registration.PhoneAuthActivity;

import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private LinearLayout editProfile, editGuardian, editNotif, editPass, editHelp, editContact;
    private TextView userName, userEmail, backButton;
    private Button btnLogout;
    private Switch notifSwitch;

    private SessionManager sessionManager;
    private SessionNotif sessionNotif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        initSessions();
        loadUserDetails();
        setupListeners();
        setupNotifSwitch();
    }

    private void initViews() {
        editProfile = findViewById(R.id.profile_info);
        editGuardian = findViewById(R.id.guardian_info);
        editPass = findViewById(R.id.password_info);
        editNotif = findViewById(R.id.notif_info);
        editHelp = findViewById(R.id.help_info);
        editContact = findViewById(R.id.contactus_info);

        userName = findViewById(R.id.pname);
        userEmail = findViewById(R.id.pemail);
        backButton = findViewById(R.id.backButton);
        btnLogout = findViewById(R.id.btnLogoutXML);
        notifSwitch = findViewById(R.id.notif_swtch);
    }

    private void initSessions() {
        sessionManager = new SessionManager(this);
        sessionNotif = new SessionNotif(this);
    }

    private void loadUserDetails() {
        Map<String, String> user = sessionManager.getUserDetails();
        userName.setText(user.get(SessionManager.KEY_NAME));
        userEmail.setText(user.get(SessionManager.KEY_EMAIL));
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> logoutUser());
        editProfile.setOnClickListener(v -> openActivity(EditProfileMainActivity.class));
        editGuardian.setOnClickListener(v -> openActivity(EditProfileActivity.class));
        editPass.setOnClickListener(v -> openPasswordVerification());
        editNotif.setOnClickListener(v -> {});
        editHelp.setOnClickListener(v -> {});
        editContact.setOnClickListener(v -> {});
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupNotifSwitch() {
        notifSwitch.setChecked(sessionNotif.isNotificationEnabled());

        notifSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                sessionNotif.enableNotification();
            } else {
                sessionNotif.clearSession();
            }
        });
    }

    private void openPasswordVerification() {
        String phone = sessionManager.getUserDetails().get(SessionManager.KEY_PHONE_NUMBER);
        Intent intent = new Intent(this, PhoneAuthActivity.class);
        intent.putExtra("Phonenumber", phone);
        intent.putExtra("whatTodo", "UpdateData");
        startActivity(intent);
    }

    private void openActivity(Class<?> targetActivity) {
        startActivity(new Intent(this, targetActivity));
    }

    private void logoutUser() {
        sessionManager.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
