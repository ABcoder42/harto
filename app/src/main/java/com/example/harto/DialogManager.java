package com.example.harto;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import com.example.harto.custom_toast.CustomToast;

public class DialogManager {

    private static AlertDialog currentDialog;
    private static boolean isGuardianDialogActive = false;

    private DialogManager() {

    }

    public static boolean isDialogShowing() {
        return currentDialog != null && currentDialog.isShowing();
    }

    public static void dismissCurrentDialog() {
        if (isDialogShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }
    }


    private static void showFixDialog(Activity activity, String title, String message, int imageRes, Runnable onButtonClick) {
        if (isDialogShowing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final var view = activity.getLayoutInflater().inflate(R.layout.dialog_universal, null);
        builder.setView(view);

        ImageView image = view.findViewById(R.id.dialogImage);
        TextView titleView = view.findViewById(R.id.dialogTitle);
        TextView messageView = view.findViewById(R.id.dialogMessage);
        Button actionButton = view.findViewById(R.id.dialogButton);

        image.setImageResource(imageRes);
        titleView.setText(title);
        messageView.setText(message);
        actionButton.setText("Fix Connection");

        actionButton.setOnClickListener(v -> {
            dismissCurrentDialog();
            new Handler(Looper.getMainLooper()).postDelayed(onButtonClick, 300);
        });

        currentDialog = builder.create();
        currentDialog.setCancelable(false);
        currentDialog.show();
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        } else {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI;
        }
    }

    private static boolean isBluetoothEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static void showConnectionDialog(Activity activity, boolean isWatchConnected) {
        boolean wifiConnected = isWifiConnected(activity);
        boolean bluetoothEnabled = isBluetoothEnabled();

        if (!wifiConnected) {
            showFixDialog(
                    activity,
                    "Wi-Fi is Off",
                    "Please enable Wi-Fi to maintain a stable connection.",
                    R.drawable.wifi,
                    () -> activity.startActivity(new Intent(Settings.Panel.ACTION_WIFI))
            );
            return;
        }

        if (!bluetoothEnabled) {
            showFixDialog(
                    activity,
                    "Bluetooth is Off",
                    "Please turn on Bluetooth for device communication.",
                    R.drawable.bluetooth,
                    () -> {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        activity.startActivityForResult(enableBtIntent, 1001);
                    }
            );
            return;
        }

        if (!isWatchConnected) {
            showFixDialog(
                    activity,
                    "Watch Not Connected",
                    "Your Wear OS watch seems disconnected. Please reconnect it via the Wear OS app.",
                    R.drawable.watch,
                    () -> openWearOSApp(activity)
            );
        } else {
            dismissCurrentDialog();
        }
    }

    private static void openWearOSApp(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(
                "com.google.android.wearable.app",
                "com.google.android.clockwork.companion.MainActivity"
        );
        try {
            activity.startActivity(intent);
        } catch (Exception e) {
            CustomToast.showWarning(activity, "Please open the Wear OS app manually.");
        }
    }

    public static void showWifiDialog(Activity activity) {
        if (!isWifiConnected(activity)) {
            showFixDialog(
                    activity,
                    "Wi-Fi is Off",
                    "Please enable Wi-Fi to maintain a stable connection.",
                    R.drawable.wifi,
                    () -> activity.startActivity(new Intent(Settings.Panel.ACTION_WIFI))
            );
        }
    }

    public static void updateConnectionStatus(Activity activity, boolean isConnected) {
        if (isGuardianDialogActive && isDialogShowing()) return;

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        showConnectionDialog(activity, isConnected);
    }

    public static void showGuardianDialog(Activity activity) {
        if (isDialogShowing() && isGuardianDialogActive) return;

        isGuardianDialogActive = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final var view = activity.getLayoutInflater().inflate(R.layout.dialog_universal, null);
        builder.setView(view);

        ImageView image = view.findViewById(R.id.dialogImage);
        TextView title = view.findViewById(R.id.dialogTitle);
        TextView message = view.findViewById(R.id.dialogMessage);
        Button button = view.findViewById(R.id.dialogButton);

        image.setImageResource(R.drawable.warning_png);
        title.setText("NO GUARDIAN ADDED");
        message.setText("Please add a Guardian to contact in emergency.");
        button.setText("Add Guardian (60s)");

        button.setOnClickListener(v -> {
            dismissCurrentDialog();
            isGuardianDialogActive = false;
            activity.startActivity(new Intent(activity, EditProfileActivity.class));
        });

        currentDialog = builder.create();
        currentDialog.setCancelable(false);
        currentDialog.setCanceledOnTouchOutside(false);
        currentDialog.setOnDismissListener(dialog -> isGuardianDialogActive = false);
        currentDialog.show();

        // 🔹 Countdown timer for auto-dismiss (60s)
        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (button != null) {
                    int seconds = (int) (millisUntilFinished / 1000);
                    button.setText("Add Guardian (" + seconds + "s)");
                }
            }

            @Override
            public void onFinish() {
                dismissCurrentDialog();
                isGuardianDialogActive = false;
            }
        }.start();
    }
}
