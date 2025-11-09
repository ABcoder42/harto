package com.example.harto.reset_password;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.harto.R;
import com.example.harto.custom_toast.CustomToast;
import com.example.harto.user_registration.PhoneAuthActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

public class CheckNumActivity extends AppCompatActivity {

    private EditText phoneNumberEditText;
    private Button nextButton;
    private TextView backButton;
    private CountryCodePicker countryCodePicker;
    private LinearLayout phoneLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_num_activity);

        initViews();
        setupListeners();
    }

    private void initViews() {
        phoneNumberEditText = findViewById(R.id.edittxtPhonelog);
        nextButton = findViewById(R.id.btnCheckXML);
        backButton = findViewById(R.id.backButton);
        countryCodePicker = findViewById(R.id.countryCodePicker);
        phoneLayout = findViewById(R.id.linearLayout);

        countryCodePicker.registerCarrierNumberEditText(phoneNumberEditText);
    }

    private void setupListeners() {
        nextButton.setOnClickListener(v -> validateNumber());
        backButton.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
            finish();
        });
    }

    private void validateNumber() {
        String userPhone = countryCodePicker.getFullNumberWithPlus();

        if (!countryCodePicker.isValidFullNumber()) {
            showPhoneError("Enter a valid phone number");
            return;
        }

        phoneLayout.setBackgroundResource(R.drawable.edit_txt_bg);
        checkUserExistence(userPhone);
    }

    private void showPhoneError(String message) {
        phoneLayout.setBackgroundResource(R.drawable.edit_txt_bg_error);
        CustomToast.showError(this, message);
        phoneNumberEditText.requestFocus();
    }

    private void checkUserExistence(String userPhone) {
        Query checkUserQuery = FirebaseDatabase.getInstance()
                .getReference("Users")
                .orderByChild("userphone")
                .equalTo(userPhone);

        checkUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    navigateToPhoneAuth(userPhone);
                } else {
                    showPhoneError("User doesn't exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                CustomToast.showError(CheckNumActivity.this, "Database error: " + error.getMessage());
            }
        });
    }

    private void navigateToPhoneAuth(String userPhone) {
        Intent intent = new Intent(this, PhoneAuthActivity.class);
        intent.putExtra("Phonenumber", userPhone);
        intent.putExtra("whatTodo", "UpdateData");
        startActivity(intent);
    }
}
