package com.example.harto.user_registration;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.harto.DialogManager;
import com.example.harto.MainActivity;
import com.example.harto.R;
import com.example.harto.SessionManager;
import com.example.harto.SessionNotif;
import com.example.harto.custom_toast.CustomToast;
import com.example.harto.reset_password.CheckNumActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;


public class LoginActivity extends AppCompatActivity {

    private EditText editPhone, editPassword;
    private TextView txtForgotPass, txtCreateAcc;
    private Button btnLogin;
    private CountryCodePicker ccp;
    private LinearLayout phoneLayout;
    private final Handler connectionHandler = new Handler();
    private boolean isWifiConnected = false;
    private Runnable connectionChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);

        initViews();
        setupListeners();
    }

    private void initViews() {
        editPhone = findViewById(R.id.edittxtPhonelog);
        editPassword = findViewById(R.id.edittxtPasslog);
        ccp = findViewById(R.id.countryCodePicker);
        ccp.registerCarrierNumberEditText(editPhone);

        btnLogin = findViewById(R.id.btnLoginXML);
        txtCreateAcc = findViewById(R.id.createacc);
        phoneLayout = findViewById(R.id.linearLayout);
        txtForgotPass = findViewById(R.id.forgetpass);

        setupPasswordVisibilityToggle(editPassword);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> validateUser());
        txtCreateAcc.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        txtForgotPass.setOnClickListener(v -> startActivity(new Intent(this, CheckNumActivity.class)));
    }

    private void validateUser() {
        final String userPhone = ccp.getFullNumberWithPlus();
        final String userPass = editPassword.getText().toString().trim();

        if (!ccp.isValidFullNumber()) {
            showFieldError(phoneLayout, "Enter a valid phone number");
            editPhone.requestFocus();
            return;
        }

        phoneLayout.setBackgroundResource(R.drawable.edit_txt_bg);

        Query userQuery = FirebaseDatabase.getInstance()
                .getReference("Users")
                .orderByChild("userphone")
                .equalTo(userPhone);

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    showFieldError(phoneLayout, "User doesn't exist. Check phone number");
                    return;
                }

                DataSnapshot userSnapshot = snapshot.child(userPhone);
                String systemPassword = userSnapshot.child("userpassword").getValue(String.class);

                if (systemPassword == null || !systemPassword.equals(userPass)) {
                    showFieldError(editPassword, "Password doesn't match!");
                    return;
                }

                // ✅ Retrieve user data
                String name = userSnapshot.child("username").getValue(String.class);
                String email = userSnapshot.child("useremail").getValue(String.class);
                String phone = userSnapshot.child("userphone").getValue(String.class);
                String password = userSnapshot.child("userpassword").getValue(String.class);
                String guardian = userSnapshot.child("guarname").getValue(String.class);
                String guardianPhone = userSnapshot.child("guarphone").getValue(String.class);

                // ✅ Store session
                SessionManager sessionManager = new SessionManager(LoginActivity.this);
                sessionManager.createLoginSession(name, email, password, phone, guardian, guardianPhone);

                new SessionNotif(LoginActivity.this).enableNotification();

                CustomToast.showSuccess(LoginActivity.this, "Login successfully!");
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                CustomToast.showError(LoginActivity.this, "Error: " + error.getMessage());
            }
        });
    }

    private void showFieldError(View field, String message) {
        field.setBackgroundResource(R.drawable.edit_txt_bg_error);
        CustomToast.showError(this, message);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordVisibilityToggle(EditText passwordField) {
        Drawable eye = AppCompatResources.getDrawable(this, R.drawable.eye_crossed);
        Drawable eyeOff = AppCompatResources.getDrawable(this, R.drawable.eye);
        Drawable startIcon = passwordField.getCompoundDrawablesRelative()[0];

        if (startIcon != null)
            startIcon.setBounds(0, 0, startIcon.getIntrinsicWidth(), startIcon.getIntrinsicHeight());
        if (eye != null)
            eye.setBounds(0, 0, eye.getIntrinsicWidth(), eye.getIntrinsicHeight());
        if (eyeOff != null)
            eyeOff.setBounds(0, 0, eyeOff.getIntrinsicWidth(), eyeOff.getIntrinsicHeight());

        passwordField.setCompoundDrawablesRelative(startIcon, null, eye, null);
        final boolean[] isVisible = {false};

        passwordField.setOnTouchListener((v, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) return false;

            Drawable drawableEnd = passwordField.getCompoundDrawablesRelative()[2];
            if (drawableEnd == null) return false;

            int touchX = (int) event.getX();
            int drawableStart = passwordField.getWidth() - passwordField.getPaddingEnd() - drawableEnd.getBounds().width();

            if (touchX >= drawableStart) {
                isVisible[0] = !isVisible[0];
                passwordField.setTransformationMethod(isVisible[0] ? null : new PasswordTransformationMethod());
                passwordField.setCompoundDrawablesRelative(startIcon, null, isVisible[0] ? eyeOff : eye, null);
                passwordField.setSelection(passwordField.getText().length());
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        DialogManager.showWifiDialog(this);
    }


}
