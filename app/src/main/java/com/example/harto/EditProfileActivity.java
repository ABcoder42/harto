package com.example.harto;

import android.content.Intent;
import android.os.Bundle;
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

public class EditProfileActivity extends AppCompatActivity {

    private TextView backButton;
    private EditText guardianNameEditText, guardianPhoneEditText;
    private Button updateButton;
    private CountryCodePicker countryCodePicker;
    private LinearLayout phoneLayout;

    private SessionManager sessionManager;
    private DatabaseReference userReference;

    private String userPhone, userName, guardianName, guardianPhone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        initSession();
        populateFields();
        setupListeners();
    }

    private void initViews() {
        guardianNameEditText = findViewById(R.id.edittxtnameReg);
        guardianPhoneEditText = findViewById(R.id.edittxtPhoneReg);
        backButton = findViewById(R.id.backButton);
        updateButton = findViewById(R.id.btnUpdateXML);
        countryCodePicker = findViewById(R.id.countryCodePicker);
        phoneLayout = findViewById(R.id.linearLayout);

        countryCodePicker.registerCarrierNumberEditText(guardianPhoneEditText);
    }

    private void initSession() {
        sessionManager = new SessionManager(this);
        userReference = FirebaseDatabase.getInstance().getReference("Users");

        Map<String, String> userDetails = sessionManager.getUserDetails();
        userPhone = userDetails.get(SessionManager.KEY_PHONE_NUMBER);
        userName = userDetails.get(SessionManager.KEY_NAME);
        guardianName = userDetails.get(SessionManager.KEY_GUARDIAN_NAME);
        guardianPhone = userDetails.get(SessionManager.KEY_GUARDIAN_PHONE);
    }

    private void populateFields() {
        if (guardianName != null) guardianNameEditText.setText(guardianName);

        String code = countryCodePicker.getSelectedCountryCode();
        if (guardianPhone != null && !guardianPhone.trim().isEmpty()) {
            String localNumber = guardianPhone.replace("+" + code, "").trim();
            guardianPhoneEditText.setText(localNumber);
        }
    }

    private void setupListeners() {
        updateButton.setOnClickListener(v -> validateAndUpdate());
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void validateAndUpdate() {
        String newGuardianName = guardianNameEditText.getText().toString().trim();
        String newGuardianPhone = countryCodePicker.getFullNumberWithPlus();

        if (!isValidGuardianName(newGuardianName)) return;
        if (!isValidPhoneNumber(newGuardianPhone)) return;

        if (isDuplicateWithUser(newGuardianName, newGuardianPhone)) return;

        boolean nameChanged = guardianName == null || !guardianName.equals(newGuardianName);
        boolean phoneChanged = guardianPhone == null || !guardianPhone.equals(newGuardianPhone);

        if (!nameChanged && !phoneChanged) {
            CustomToast.showError(this, "Information hasn't changed");
            return;
        }

        // Save updates
        updateGuardianInfo(newGuardianName, newGuardianPhone);
    }

    private boolean isValidGuardianName(String name) {
        if (name.isEmpty() || !name.contains(" ")) {
            guardianNameEditText.setBackgroundResource(R.drawable.edit_txt_bg_error);
            CustomToast.showError(this, "Enter full name");
            guardianNameEditText.requestFocus();
            return false;
        }
        guardianNameEditText.setBackgroundResource(R.drawable.edit_txt_bg);
        return true;
    }

    private boolean isValidPhoneNumber(String phone) {
        if (!countryCodePicker.isValidFullNumber()) {
            phoneLayout.setBackgroundResource(R.drawable.edit_txt_bg_error);
            CustomToast.showError(this, "Enter a valid phone number");
            guardianPhoneEditText.requestFocus();
            return false;
        }
        phoneLayout.setBackgroundResource(R.drawable.edit_txt_bg);
        return true;
    }

    private boolean isDuplicateWithUser(String name, String phone) {
        if (name.equals(userName)) {
            guardianNameEditText.setBackgroundResource(R.drawable.edit_txt_bg_error);
            CustomToast.showError(this, "You can't use your own name as Guardian name");
            return true;
        }

        if (phone.equals(userPhone)) {
            phoneLayout.setBackgroundResource(R.drawable.edit_txt_bg_error);
            CustomToast.showError(this, "You can't use your own phone number as Guardian phone");
            return true;
        }

        guardianNameEditText.setBackgroundResource(R.drawable.edit_txt_bg);
        phoneLayout.setBackgroundResource(R.drawable.edit_txt_bg);
        return false;
    }

    private void updateGuardianInfo(String name, String phone) {
        userReference.child(userPhone).child("guarname").setValue(name);
        userReference.child(userPhone).child("guarphone").setValue(phone);

        sessionManager.updateGuardianInfo(name, phone);

        CustomToast.showSuccess(this, "Guardian info saved successfully!");
        navigateToMain();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
