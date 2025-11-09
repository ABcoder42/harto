package com.example.harto;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.harto.custom_toast.CustomToast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.Map;

public class EditProfileMainActivity extends AppCompatActivity {

    private TextView backButton;
    private EditText nameField, emailField, phoneField;
    private Button updateButton;
    private CountryCodePicker countryCodePicker;
    private LinearLayout phoneLayout;

    private SessionManager sessionManager;
    private DatabaseReference userReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_main_profile);

        initViews();
        initSession();
        populateUserData();
        setupListeners();
    }

    private void initViews() {
        nameField = findViewById(R.id.edittxtnameReg);
        emailField = findViewById(R.id.edittxtEmailReg);
        phoneField = findViewById(R.id.edittxtPhoneReg);
        backButton = findViewById(R.id.backButton);
        updateButton = findViewById(R.id.btnUpdateXML);
        countryCodePicker = findViewById(R.id.countryCodePicker);
        phoneLayout = findViewById(R.id.linearLayout);
        userReference = FirebaseDatabase.getInstance().getReference("Users");
    }

    private void initSession() {
        sessionManager = new SessionManager(this);
    }

    private void populateUserData() {
        Map<String, String> userDetails = sessionManager.getUserDetails();

        String userName = userDetails.get(SessionManager.KEY_NAME);
        String userEmail = userDetails.get(SessionManager.KEY_EMAIL);
        String userPhone = userDetails.get(SessionManager.KEY_PHONE_NUMBER);

        if (userName != null) nameField.setText(userName);
        if (userEmail != null) emailField.setText(userEmail);

        String code = countryCodePicker.getSelectedCountryCode();
        if (userPhone != null && !userPhone.trim().isEmpty()) {
            phoneField.setText(userPhone.replace("+" + code, "").trim());
        }

        countryCodePicker.registerCarrierNumberEditText(phoneField);
    }

    private void setupListeners() {
        updateButton.setOnClickListener(v -> validateAndUpdate());
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void validateAndUpdate() {
        Map<String, String> userDetails = sessionManager.getUserDetails();

        String currentName = nameField.getText().toString().trim();
        String currentEmail = emailField.getText().toString().trim();
        String currentPhone = countryCodePicker.getFullNumberWithPlus();

        String oldName = userDetails.get(SessionManager.KEY_NAME);
        String oldEmail = userDetails.get(SessionManager.KEY_EMAIL);
        String oldPhone = userDetails.get(SessionManager.KEY_PHONE_NUMBER);
        String guardianName = userDetails.get(SessionManager.KEY_GUARDIAN_NAME);
        String guardianPhone = userDetails.get(SessionManager.KEY_GUARDIAN_PHONE);

        if (!isValidName(currentName)) return;
        if (!isValidEmail(currentEmail)) return;
        if (!isValidPhone(currentPhone)) return;

        if (!hasChanged(currentName, oldName, currentEmail, oldEmail, currentPhone, oldPhone)) {
            CustomToast.showError(this, "Information hasn't changed");
            return;
        }

        if (currentName.equals(guardianName)) {
            nameField.setBackgroundResource(R.drawable.edit_txt_bg_error);
            CustomToast.showError(this, "You can't use your guardian's name as your own");
            return;
        }

        if (currentPhone.equals(guardianPhone)) {
            phoneLayout.setBackgroundResource(R.drawable.edit_txt_bg_error);
            CustomToast.showError(this, "You can't use your guardian's phone number");
            return;
        }

        if (!currentPhone.equals(oldPhone)) {
            updatePhoneNumber(oldPhone, currentPhone, currentName, currentEmail);
        } else {
            updateUserInfo(oldPhone, currentName, currentEmail, currentPhone);
        }
    }

    private boolean isValidName(String name) {
        if (name.isEmpty() || !name.contains(" ")) {
            nameField.setBackgroundResource(R.drawable.edit_txt_bg_error);
            CustomToast.showError(this, "Enter your full name");
            nameField.requestFocus();
            return false;
        }
        nameField.setBackgroundResource(R.drawable.edit_txt_bg);
        return true;
    }

    private boolean isValidEmail(String email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setBackgroundResource(R.drawable.edit_txt_bg_error);
            CustomToast.showError(this, "Enter a valid email");
            emailField.requestFocus();
            return false;
        }
        emailField.setBackgroundResource(R.drawable.edit_txt_bg);
        return true;
    }

    private boolean isValidPhone(String phone) {
        if (!countryCodePicker.isValidFullNumber()) {
            phoneLayout.setBackgroundResource(R.drawable.edit_txt_bg_error);
            CustomToast.showError(this, "Enter a valid phone number");
            phoneField.requestFocus();
            return false;
        }
        phoneLayout.setBackgroundResource(R.drawable.edit_txt_bg);
        return true;
    }

    private boolean hasChanged(String newName, String oldName, String newEmail, String oldEmail, String newPhone, String oldPhone) {
        return !(newName.equals(oldName) && newEmail.equals(oldEmail) && newPhone.equals(oldPhone));
    }

    private void updatePhoneNumber(String oldPhone, String newPhone, String name, String email) {
        userReference.child(oldPhone).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                userReference.child(newPhone).setValue(snapshot.getValue()).addOnSuccessListener(unused -> {
                    updateUserFields(newPhone, name, email, newPhone);
                    userReference.child(oldPhone).removeValue();
                    sessionManager.updateUserInfo(name, newPhone, email);
                    CustomToast.showSuccess(this, "Phone number updated successfully!");
                    getOnBackPressedDispatcher().onBackPressed();
                });
            } else {
                CustomToast.showError(this, "Old user record not found!");
            }
        }).addOnFailureListener(e ->
                CustomToast.showError(this, "Error updating phone number: " + e.getMessage())
        );
    }

    private void updateUserInfo(String phone, String name, String email, String currentPhone) {
        updateUserFields(phone, name, email, currentPhone);
        sessionManager.updateUserInfo(name, currentPhone, email);
        CustomToast.showSuccess(this, "Information updated successfully!");
        getOnBackPressedDispatcher().onBackPressed();
    }

    private void updateUserFields(String phoneKey, String name, String email, String phone) {
        userReference.child(phoneKey).child("username").setValue(name);
        userReference.child(phoneKey).child("useremail").setValue(email);
        userReference.child(phoneKey).child("userphone").setValue(phone);
    }
}
