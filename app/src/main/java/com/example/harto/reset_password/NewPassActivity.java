package com.example.harto.reset_password;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.harto.R;
import com.example.harto.custom_toast.CustomToast;
import com.example.harto.user_registration.LoginActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewPassActivity extends AppCompatActivity {

    private EditText editPassword, editConfirmPassword;
    private Button btnConfirm;
    private TextView btnBack;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pass);

        initViews();
        setupListeners();
    }

    private void initViews() {
        phoneNumber = getIntent().getStringExtra("Phonenumber");

        editPassword = findViewById(R.id.edittxtPassReg);
        editConfirmPassword = findViewById(R.id.edittxtComPassReg);
        btnConfirm = findViewById(R.id.btnConfirmXML);
        btnBack = findViewById(R.id.backButton);

        setupPasswordVisibilityToggle(editPassword);
        setupPasswordVisibilityToggle(editConfirmPassword);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        btnConfirm.setOnClickListener(v -> validateAndSavePassword());
    }

    private void validateAndSavePassword() {
        String newPassword = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        if (!isStrongPassword(newPassword)) {
            showError(editPassword, "Password must be at least 8 characters with upper/lowercase letters and digits.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError(editConfirmPassword, "Passwords do not match.");
            return;
        }

        // ✅ Update password in Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(phoneNumber)
                .child("userpassword");

        userRef.setValue(newPassword)
                .addOnSuccessListener(unused -> {
                    CustomToast.showSuccess(this, "Password changed successfully!");
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        CustomToast.showError(this, "Failed to update password: " + e.getMessage()));
    }

    private boolean isStrongPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*[0-9].*");
    }

    private void showError(EditText field, String message) {
        field.setBackgroundResource(R.drawable.edit_txt_bg_error);
        CustomToast.showError(this, message);
        field.requestFocus();
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

        final boolean[] isPasswordVisible = {false};

        passwordField.setOnTouchListener((v, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) return false;

            Drawable drawableEnd = passwordField.getCompoundDrawablesRelative()[2];
            if (drawableEnd == null) return false;

            int touchX = (int) event.getX();
            int drawableStart = passwordField.getWidth() - passwordField.getPaddingEnd() - drawableEnd.getBounds().width();

            if (touchX >= drawableStart) {
                isPasswordVisible[0] = !isPasswordVisible[0];

                passwordField.setTransformationMethod(isPasswordVisible[0]
                        ? null
                        : new android.text.method.PasswordTransformationMethod());

                passwordField.setCompoundDrawablesRelative(startIcon, null,
                        isPasswordVisible[0] ? eyeOff : eye, null);

                passwordField.setSelection(passwordField.getText().length());
                return true;
            }
            return false;
        });
    }
}
