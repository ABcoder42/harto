package com.example.harto;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;


public class MainWatchActivity extends AppCompatActivity
        implements SensorEventListener, DataClient.OnDataChangedListener {

    private static final int REQUEST_BODY_SENSOR_PERMISSION = 1001;

    private TextView heartRateTextView;
    private ToggleButton toggleButton;
    private SensorManager sensorManager;
    private Sensor heartRateSensor;
    private boolean isTracking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_watch);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initViews();
        initSensorManager();
        setupToggleButton();
        requestBodySensorPermissionIfNeeded();
    }

    private void initViews() {
        heartRateTextView = findViewById(R.id.txtHeartrate);
        toggleButton = findViewById(R.id.toglbtn);
    }

    private void initSensorManager() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        heartRateSensor = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) : null;
    }
    private void setupToggleButton() {
        toggleButton.setOnClickListener(v -> {
            if (toggleButton.isChecked()) {
                startHeartRateMonitoring();
            } else {
                stopHeartRateMonitoring();
            }
        });
    }

    private void requestBodySensorPermissionIfNeeded() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.BODY_SENSORS},
                    REQUEST_BODY_SENSOR_PERMISSION
            );
        }
    }

    private void startHeartRateMonitoring() {
        if (heartRateSensor != null) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
            isTracking = true;
            heartRateTextView.setText("Measuring...");
        } else {
            heartRateTextView.setText("Heart rate sensor not available");
        }
    }

    private void stopHeartRateMonitoring() {
        sendHeartRateData("00");
        sensorManager.unregisterListener(this);
        heartRateTextView.setText("");
        isTracking = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isTracking && event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            int heartRate = Math.round(event.values[0]);
            heartRateTextView.setText(heartRate + " BPM");
            sendHeartRateData(String.valueOf(heartRate));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTracking) sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTracking && heartRateSensor != null) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    private void sendHeartRateData(@NonNull String heartRate) {
        try {
            DataClient dataClient = Wearable.getDataClient(this);
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/ulheart");
            putDataMapRequest.getDataMap().putString("HeartRate", heartRate);

            PutDataRequest request = putDataMapRequest.asPutDataRequest();
            request.setUrgent();
            Task<DataItem> putDataTask = dataClient.putDataItem(request);


        } catch (Exception e) {
            Toast.makeText(this, "Error sending data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEvents) {
        dataEvents.release();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BODY_SENSOR_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Body sensor permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Body sensor permission denied", Toast.LENGTH_SHORT).show();
                toggleButton.setChecked(false);
            }
        }
    }
}
