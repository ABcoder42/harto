package com.example.harto;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.harto.custom_toast.CustomToast;
import com.example.harto.hadlers.HalfCircleProgressBar;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.Map;


public class MainActivity extends AppCompatActivity implements DataClient.OnDataChangedListener {

    private static final int MAX_HEART_RATE = 220;
    private static final int SMS_PERMISSION_CODE = 100;
    private static final long TIP_UPDATE_INTERVAL = 900_000L;
    private static final long CONNECTION_CHECK_INTERVAL = 5_000L;
    private static final long ALERT_DIALOG_TIMEOUT = 60_000L;

    private TextView heartValue, condValue, condDesc, txtMax, txtMin, tipTitle, tipDesc;
    private ImageView heartIcon, menu;
    private Button btnEmergency;
    private HalfCircleProgressBar progressBar;

    private int currentHeartRate, maxHR = 0, minHR = 0;
    private boolean firstReading = true, isWatchConnected = false;
    private int currentTipIndex = 0;

    private final Handler tipHandler = new Handler();
    private final Handler connectionHandler = new Handler();
    private Runnable connectionChecker;

    private AlertDialog heartRateDialog;
    private CountDownTimer dialogTimer;

    private String guardianPhone, guardianName, userName, message = "";

    private final Tip[] tips = {
            new Tip("Take deep breaths", "Relaxation helps lower your heart rate naturally."),
            new Tip("Move a little every hour", "Even short walks keep your heart active."),
            new Tip("Stay hydrated", "Water helps your heart pump blood more easily."),
            new Tip("Eat heart-friendly foods", "Go for fruits, vegetables, and whole grains."),
            new Tip("Get enough sleep", "Rest helps your heart recover and stay strong."),
            new Tip("Smile often", "Positive emotions can calm your heartbeat."),
            new Tip("Avoid too much caffeine", "It can temporarily raise your heart rate."),
            new Tip("Check your heart rate daily", "Early awareness means early care."),
            new Tip("Enjoy the outdoors", "Fresh air and sunlight can reduce stress."),
            new Tip("Give your heart a break", "Slow down when you feel tired or stressed.")
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);

        setupInsets();
        initViews();
        loadSessionData();
        setupListeners();

        updateTip();
        tipHandler.postDelayed(this::updateTip, TIP_UPDATE_INTERVAL);

        startConnectionMonitoring();
        checkSmsPermission();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onResume() {
        super.onResume();

        if (!hasBluetoothPermission()) {
            requestBluetoothPermission();
            return;
        }

        Wearable.getDataClient(this).addListener(this);
        refreshConnectionStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getDataClient(this).removeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firstReading = true;
        maxHR = minHR = 0;
        if (connectionChecker != null) connectionHandler.removeCallbacks(connectionChecker);
        if (dialogTimer != null) dialogTimer.cancel();
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void initViews() {
        heartValue = findViewById(R.id.txt_heartrate);
        progressBar = findViewById(R.id.halfProgressBar);
        condValue = findViewById(R.id.text_cond_alert);
        condDesc = findViewById(R.id.text_cond_desc);
        heartIcon = findViewById(R.id.iconHeart);
        txtMax = findViewById(R.id.text_Highest_BPM);
        txtMin = findViewById(R.id.text_MINIMUM_BPM);
        menu = findViewById(R.id.iconMenu);
        tipTitle = findViewById(R.id.tipTitle);
        tipDesc = findViewById(R.id.tips_content);
        btnEmergency = findViewById(R.id.btnCallXML);
    }

    private void loadSessionData() {
        SessionManager session = new SessionManager(this);
        Map<String, String> user = session.getUserDetails();
        guardianPhone = user.get(SessionManager.KEY_GUARDIAN_PHONE);
        guardianName = user.get(SessionManager.KEY_GUARDIAN_NAME);
        userName = user.get(SessionManager.KEY_NAME);
    }

    private void setupListeners() {
        menu.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnEmergency.setOnClickListener(v -> {
            if (guardianPhone == null || guardianPhone.trim().isEmpty()) {
                DialogManager.showGuardianDialog(this);
            } else {
                sendEmergencyMessage(guardianPhone, message);
            }
        });
    }

    private boolean hasBluetoothPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestBluetoothPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
    }

    private void refreshConnectionStatus() {
        checkWatchConnection();
        DialogManager.showConnectionDialog(this, isWatchConnected);
    }

    private void startConnectionMonitoring() {
        if (connectionChecker != null) connectionHandler.removeCallbacks(connectionChecker);

        connectionChecker = () -> {
            checkWatchConnection();
            DialogManager.updateConnectionStatus(MainActivity.this, isWatchConnected);
            connectionHandler.postDelayed(connectionChecker, CONNECTION_CHECK_INTERVAL);
        };
        connectionHandler.post(connectionChecker);
    }

    private void checkWatchConnection() {
        Wearable.getNodeClient(this)
                .getConnectedNodes()
                .addOnSuccessListener(nodes -> isWatchConnected = !nodes.isEmpty());
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer events) {
        for (DataEvent event : events) {
            if (event.getType() != DataEvent.TYPE_CHANGED) continue;
            DataItem item = event.getDataItem();
            if (!"/ulheart".equals(item.getUri().getPath())) continue;

            DataMap map = DataMapItem.fromDataItem(item).getDataMap();
            String heartData = map.getString("HeartRate");
            if (heartData == null) return;

            currentHeartRate = Integer.parseInt(heartData);
            updateHeartDisplay(currentHeartRate);
        }
    }

    private void updateHeartDisplay(int bpm) {
        if (bpm <= 0) {
            heartValue.setText("00");
            condValue.setText("");
            condDesc.setText("");
            progressBar.setProgress(0);
            return;
        }

        progressBar.setProgress((int) ((bpm / (float) MAX_HEART_RATE) * 100));

        if (firstReading) {
            maxHR = minHR = bpm;
            firstReading = false;
        } else {
            maxHR = Math.max(maxHR, bpm);
            minHR = Math.min(minHR, bpm);
        }

        heartValue.setText(String.valueOf(bpm));
        txtMax.setText(String.valueOf(maxHR));
        txtMin.setText(String.valueOf(minHR));

        updateHeartRateStatus(bpm);
    }

    @SuppressLint("SetTextI18n")
    private void updateHeartRateStatus(int bpm) {
        HeartCondition[] conditions = {
                new HeartCondition(0, 39, "Critical Low", "Heart rate dangerously low. Seek medical help immediately.", R.drawable.alert_bg_critical_low, "Critical Low Heart Rate Warning"),
                new HeartCondition(40, 59, "Low", "Below normal. Monitor for fatigue or dizziness.", R.drawable.alert_bg_low, "Low Heart Rate Warning"),
                new HeartCondition(60, 100, "Normal", "Heart rate within healthy resting range.", R.drawable.alert_bg_normal, "Normal Heart Rate"),
                new HeartCondition(101, 120, "Elevated", "Slightly high due to activity, stress, or caffeine.", R.drawable.alert_bg_elevated, "Elevated Heart Rate Warning"),
                new HeartCondition(121, 160, "Active", "High from exercise; normal during activity.", R.drawable.alert_bg_active, "Active Heart Rate Warning"),
                new HeartCondition(161, 180, "Warning High", "Very high. Take caution if not exercising.", R.drawable.alert_bg_warning_high, "High Heart Rate Warning"),
                new HeartCondition(181, Integer.MAX_VALUE, "Critical High", "Extremely high. Seek medical attention immediately.", R.drawable.alert_bg_critical_low, "Critical High Heart Rate Warning")
        };

        for (HeartCondition c : conditions) {
            if (bpm >= c.min && bpm <= c.max) {
                condValue.setText(c.value);
                condDesc.setText(c.description);
                message = c.message;

                heartIcon.setBackgroundResource(c.bgRes);
                if (bpm < 60 || bpm > 100) handleAbnormalHeartRate(c);
                break;
            }
        }
    }

    private void handleAbnormalHeartRate(HeartCondition condition) {
        SessionNotif sessionNotif = new SessionNotif(this);
        if (!sessionNotif.isNotificationEnabled()) return;

        if (guardianPhone == null || guardianPhone.trim().isEmpty()) {
            DialogManager.showGuardianDialog(this);
        } else {
            showHeartRateDialog(R.drawable.heart_sad, "Are you Ok?",
                    condition.message, guardianPhone);
        }
    }

    private void showHeartRateDialog(int iconRes, String title, String message, String phone) {
        if (heartRateDialog != null && heartRateDialog.isShowing()) return;

        View view = getLayoutInflater().inflate(R.layout.dialog_heart_rate, null);
        ImageView icon = view.findViewById(R.id.dialogIcon);
        TextView titleView = view.findViewById(R.id.dialogTitle);
        TextView msgView = view.findViewById(R.id.dialogMessage);
        Button okButton = view.findViewById(R.id.dialogButton);

        icon.setImageResource(iconRes);
        titleView.setText(title);
        msgView.setText(message);


        heartRateDialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();
        heartRateDialog.show();

        if (dialogTimer != null) dialogTimer.cancel();

        dialogTimer = new CountDownTimer(ALERT_DIALOG_TIMEOUT, 1000) {
            @Override
            public void onTick(long ms) {
                okButton.setText("I am OK (" + (ms / 1000) + "s)");
            }

            @Override
            public void onFinish() {
                if (heartRateDialog != null && heartRateDialog.isShowing()) {
                    heartRateDialog.dismiss();
                    new SessionNotif(MainActivity.this).clearSession();
                    sendEmergencyMessage(phone, message);
                }
            }
        }.start();

        okButton.setOnClickListener(v -> {
            if (dialogTimer != null) dialogTimer.cancel();
            heartRateDialog.dismiss();
        });
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code != SMS_PERMISSION_CODE) return;

        if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            CustomToast.showSuccess(this, "SMS Permission Granted");
        } else {
            CustomToast.showError(this, "SMS Permission Denied");
        }
    }

    private void sendEmergencyMessage(String phone, String status) {
        try {
            SmsManager sms = SmsManager.getDefault();
            String text = "EMERGENCY ALERT!\n" +
                    "Hello Mr/Mrs " + guardianName + ",\n" +
                    "Your patient " + userName + " has reached " + status + " in their BPM monitor.";
            sms.sendTextMessage(phone, null, text, null, null);
            CustomToast.showSuccess(this, "Emergency message sent!");
        } catch (Exception e) {
            CustomToast.showError(this, "Failed to send emergency message.");
        }
    }

    private void updateTip() {
        Tip tip = tips[currentTipIndex];
        tipTitle.setText(tip.title);
        tipDesc.setText(tip.desc);
        currentTipIndex = (currentTipIndex + 1) % tips.length;
    }

    private static class HeartCondition {
        final int min, max, bgRes;
        final String value, description, message;

        HeartCondition(int min, int max, String value, String description, int bgRes, String message) {
            this.min = min;
            this.max = max;
            this.value = value;
            this.description = description;
            this.bgRes = bgRes;
            this.message = message;
        }
    }

    private static class Tip {
        final String title, desc;

        Tip(String title, String desc) {
            this.title = title;
            this.desc = desc;
        }
    }
}
