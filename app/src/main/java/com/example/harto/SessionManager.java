package com.example.harto;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {

    private static final String PREF_NAME = "userLoginSession";
    private static final String KEY_IS_LOGGED_IN = "IsLoggedIn";
    private static final String KEY_IS_NOTIF_ON = "IsNotificationOn";

    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PHONE_NUMBER = "phone";
    public static final String KEY_GUARDIAN_NAME = "guardian_name";
    public static final String KEY_GUARDIAN_PHONE = "guardian_phone";

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = preferences.edit();
    }

    public void createLoginSession(String name, String email, String password,
                                   String phone, String guardianName, String guardianPhone) {

        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putBoolean(KEY_IS_NOTIF_ON, true);

        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_PHONE_NUMBER, phone);
        editor.putString(KEY_GUARDIAN_NAME, guardianName);
        editor.putString(KEY_GUARDIAN_PHONE, guardianPhone);

        editor.apply();
    }

    public void updateGuardianInfo(String guardianName, String guardianPhone) {
        editor.putString(KEY_GUARDIAN_NAME, guardianName);
        editor.putString(KEY_GUARDIAN_PHONE, guardianPhone);
        editor.apply();
    }

    public void updateUserInfo(String name, String phone, String email) {
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_PHONE_NUMBER, phone);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public Map<String, String> getUserDetails() {
        Map<String, String> userData = new HashMap<>();
        userData.put(KEY_NAME, preferences.getString(KEY_NAME, null));
        userData.put(KEY_EMAIL, preferences.getString(KEY_EMAIL, null));
        userData.put(KEY_PASSWORD, preferences.getString(KEY_PASSWORD, null));
        userData.put(KEY_PHONE_NUMBER, preferences.getString(KEY_PHONE_NUMBER, null));
        userData.put(KEY_GUARDIAN_NAME, preferences.getString(KEY_GUARDIAN_NAME, null));
        userData.put(KEY_GUARDIAN_PHONE, preferences.getString(KEY_GUARDIAN_PHONE, null));
        return userData;
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.clear().apply();
    }
}
