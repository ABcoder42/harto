package com.example.harto.custom_toast;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.harto.R;

import android.graphics.Color;

public class CustomToast {

    public static void show(Context context, String title, String message, int iconRes, int barColor, int backgroundColor) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast_container, null);


        View coloredBar = layout.findViewById(R.id.coloredBar);
        LinearLayout toastContainer = layout.findViewById(R.id.toastContainer);
        ImageView icon = layout.findViewById(R.id.imgIcon);
        TextView txtTitle = layout.findViewById(R.id.toastTitle);
        TextView txtMessage = layout.findViewById(R.id.toastMessage);


        coloredBar.setBackgroundColor(barColor);
        toastContainer.setBackgroundColor(backgroundColor);
        icon.setImageResource(iconRes);
        icon.setColorFilter(barColor); // ✅ dynamically change icon color
        txtTitle.setText(title);
        txtMessage.setText(message);

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 150);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }


    public static void showError(Context context, String message) {
        show(context, "Error", message,
                R.drawable.ic_error, Color.parseColor("#FF3B5C"), Color.WHITE);
    }


    public static void showSuccess(Context context, String message) {
        show(context, "Success", message,
                R.drawable.ic_success, Color.parseColor("#4CAF50"), Color.WHITE);
    }


    public static void showWarning(Context context, String message) {
        show(context, "Warning", message,
                R.drawable.ic_warning, Color.parseColor("#FFC107"), Color.WHITE);
    }


    public static void showInfo(Context context, String message) {
        show(context, "Info", message,
                R.drawable.ic_info, Color.parseColor("#4178E1"), Color.WHITE);
    }


}
