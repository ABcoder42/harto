package com.example.harto.user_registration;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.harto.R;
import com.example.harto.custom_toast.CustomToast;
import com.example.harto.reset_password.NewPassActivity;
import com.example.harto.user;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity {

    private static final long RESEND_TIMEOUT_MS = 60_000;

    private TextView backButton, tvResend;
    private EditText etCode;
    private Button btnVerify;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    private String name, email, phone, password, guardianName, guardianPhone, actionType;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        initViews();
        mAuth = FirebaseAuth.getInstance();
        retrieveIntentData();
        setupListeners();

        sendVerificationCode(phone, null);
        setupResendClickable();
        startResendCooldown();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    private void initViews() {
        tvResend = findViewById(R.id.tvResend);
        etCode = findViewById(R.id.etCode);
        btnVerify = findViewById(R.id.btnVerifyXML);
        progressBar = findViewById(R.id.progress);
        backButton = findViewById(R.id.backButton);
    }

    private void retrieveIntentData() {
        Intent intent = getIntent();
        name = intent.getStringExtra("Name");
        email = intent.getStringExtra("Email");
        phone = intent.getStringExtra("Phonenumber");
        password = intent.getStringExtra("Password");
        guardianName = intent.getStringExtra("GuardianName");
        guardianPhone = intent.getStringExtra("GuardianPhone");
        actionType = intent.getStringExtra("whatTodo");

        if (phone == null) phone = "+63XXXXXXXXXX";
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        btnVerify.setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            if (code.length() < 6) {
                CustomToast.showError(this, "Enter a valid 6-digit code");
            } else {
                verifyCode(code);
            }
        });
    }

    private void sendVerificationCode(String phone, PhoneAuthProvider.ForceResendingToken token) {
        progressBar.setVisibility(View.VISIBLE);

        PhoneAuthOptions.Builder optionsBuilder = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks);

        if (token != null) {
            optionsBuilder.setForceResendingToken(token);
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build());
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    progressBar.setVisibility(View.GONE);
                    String smsCode = credential.getSmsCode();
                    if (smsCode != null) etCode.setText(smsCode);
                    signInWithCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    progressBar.setVisibility(View.GONE);
                    CustomToast.showError(PhoneAuthActivity.this, "Verification failed: " + e.getMessage());
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    progressBar.setVisibility(View.GONE);
                    PhoneAuthActivity.this.verificationId = verificationId;
                    resendToken = token;
                    CustomToast.showSuccess(PhoneAuthActivity.this, "Code sent successfully!");
                }
            };

    private void verifyCode(String code) {
        if (verificationId == null) {
            CustomToast.showError(this, "Invalid or expired verification ID. Please resend the code.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        if ("UpdateData".equalsIgnoreCase(actionType)) {
                            goToUpdatePassword();
                        } else {
                            saveNewUserData();
                        }
                    } else {
                        CustomToast.showError(this, "Verification failed. Please try again.");
                    }
                });
    }

    private void setupResendClickable() {
        String fullText = "Didn't receive code? Resend";
        SpannableString spannable = new SpannableString(fullText);
        int start = fullText.indexOf("Resend");

        if (start >= 0) {
            ClickableSpan resendSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    if (countDownTimer != null) return; // still cooling down
                    FirebaseAuth.getInstance().signOut();
                    sendVerificationCode(phone, resendToken);
                    startResendCooldown();
                }
            };

            spannable.setSpan(resendSpan, start, start + "Resend".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvResend.setText(spannable);
            tvResend.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            tvResend.setText(fullText);
        }
    }

    private void startResendCooldown() {
        tvResend.setEnabled(false);
        tvResend.setAlpha(0.6f);

        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(RESEND_TIMEOUT_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvResend.setText("Resend code in " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                tvResend.setEnabled(true);
                tvResend.setAlpha(1f);
                setupResendClickable();
                countDownTimer = null;
            }
        }.start();
    }

    private void saveNewUserData() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        user newUser = new user(name, email, phone, password, guardianName, guardianPhone);

        reference.child(phone)
                .setValue(newUser)
                .addOnSuccessListener(a -> CustomToast.showSuccess(this, "Account created successfully!"))
                .addOnFailureListener(e -> CustomToast.showError(this, "Failed to save user: " + e.getMessage()));

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void goToUpdatePassword() {
        Intent intent = new Intent(this, NewPassActivity.class);
        intent.putExtra("Phonenumber", phone);
        startActivity(intent);
    }
}
