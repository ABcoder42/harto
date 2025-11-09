package com.example.harto.user_registration;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.harto.R;
import com.example.harto.custom_toast.CustomToast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.hbb20.CountryCodePicker;

public class SignupActivity extends AppCompatActivity {

    private EditText fullNameField, emailField, phoneField, passwordField, confirmPasswordField;
    private Button signupButton;
    private TextView backButton;
    private CountryCodePicker countryCodePicker;
    private LinearLayout phoneLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_signup);

        initViews();
        setupListeners();
        setupPasswordVisibility(passwordField);
        setupPasswordVisibility(confirmPasswordField);
    }

    private void initViews() {
        fullNameField = findViewById(R.id.edittxtnameReg);
        emailField = findViewById(R.id.edittxtEmailReg);
        phoneField = findViewById(R.id.edittxtPhoneReg);
        passwordField = findViewById(R.id.edittxtPassReg);
        confirmPasswordField = findViewById(R.id.edittxtComPassReg);
        signupButton = findViewById(R.id.btnSignupXML);
        backButton = findViewById(R.id.backButton);
        phoneLayout = findViewById(R.id.linearLayout);
        countryCodePicker = findViewById(R.id.countryCodePicker);
        countryCodePicker.registerCarrierNumberEditText(phoneField);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        signupButton.setOnClickListener(v -> validateAndProceed());
    }

    private void validateAndProceed() {
        String fullName = getText(fullNameField);
        String email = getText(emailField);
        String phone = countryCodePicker.getFullNumberWithPlus();
        String password = getText(passwordField);
        String confirmPassword = getText(confirmPasswordField);

        if (fullName.isEmpty() || !fullName.contains(" ")) {
            showFieldError(fullNameField, "Enter your full name");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showFieldError(emailField, "Enter a valid email");
            return;
        }

        if (!countryCodePicker.isValidFullNumber()) {
            showLayoutError(phoneLayout, "Enter a valid phone number");
            phoneField.requestFocus();
            return;
        }

        if (!isStrongPassword(password)) {
            showFieldError(passwordField, "Password must be 8+ chars with upper/lowercase and digit");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showFieldError(confirmPasswordField, "Passwords do not match");
            return;
        }

        checkIfUserExists(fullName, email, phone, password);
    }

    private String getText(EditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private void showFieldError(EditText field, String message) {
        field.setBackgroundResource(R.drawable.edit_txt_bg_error);
        CustomToast.showError(this, message);
        field.requestFocus();
    }

    private void showLayoutError(LinearLayout layout, String message) {
        layout.setBackgroundResource(R.drawable.edit_txt_bg_error);
        CustomToast.showError(this, message);
    }

    private void checkIfUserExists(String fullName, String email, String phone, String password) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        Query userQuery = usersRef.orderByChild("userphone").equalTo(phone);

        userQuery.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    phoneField.setError("Number is already used in another account.");
                    phoneField.requestFocus();
                } else {
                    proceedToPhoneVerification(fullName, email, phone, password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                CustomToast.showError(SignupActivity.this, "Database error: " + error.getMessage());
            }
        });
    }

    private void proceedToPhoneVerification(String fullName, String email, String phone, String password) {
        Intent intent = new Intent(SignupActivity.this, PhoneAuthActivity.class);
        intent.putExtra("Name", fullName);
        intent.putExtra("Phonenumber", phone);
        intent.putExtra("Email", email);
        intent.putExtra("Password", password);
        intent.putExtra("GuardianName", "");
        intent.putExtra("GuardianPhone", "");
        intent.putExtra("whatTodo", "CreateData");
        startActivity(intent);
    }

    private boolean isStrongPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*[0-9].*");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordVisibility(EditText passwordField) {
        Drawable eyeClosed = AppCompatResources.getDrawable(this, R.drawable.eye_crossed);
        Drawable eyeOpen = AppCompatResources.getDrawable(this, R.drawable.eye);
        Drawable startIcon = passwordField.getCompoundDrawablesRelative()[0];

        if (startIcon != null)
            startIcon.setBounds(0, 0, startIcon.getIntrinsicWidth(), startIcon.getIntrinsicHeight());
        if (eyeClosed != null)
            eyeClosed.setBounds(0, 0, eyeClosed.getIntrinsicWidth(), eyeClosed.getIntrinsicHeight());
        if (eyeOpen != null)
            eyeOpen.setBounds(0, 0, eyeOpen.getIntrinsicWidth(), eyeOpen.getIntrinsicHeight());

        passwordField.setCompoundDrawablesRelative(startIcon, null, eyeClosed, null);
        final boolean[] isPasswordVisible = {false};

        passwordField.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Drawable drawableEnd = passwordField.getCompoundDrawablesRelative()[DRAWABLE_RIGHT];
                if (drawableEnd != null) {
                    int touchX = (int) event.getX();
                    int width = passwordField.getWidth();
                    int paddingEnd = passwordField.getPaddingEnd();
                    int drawableWidth = drawableEnd.getBounds().width();

                    int drawableStart = width - paddingEnd - drawableWidth;
                    if (touchX >= drawableStart) {
                        isPasswordVisible[0] = !isPasswordVisible[0];
                        if (isPasswordVisible[0]) {
                            passwordField.setTransformationMethod(null);
                            passwordField.setCompoundDrawablesRelative(startIcon, null, eyeOpen, null);
                        } else {
                            passwordField.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
                            passwordField.setCompoundDrawablesRelative(startIcon, null, eyeClosed, null);
                        }
                        passwordField.setSelection(passwordField.length());
                        return true;
                    }
                }
            }
            return false;
        });
    }
}
