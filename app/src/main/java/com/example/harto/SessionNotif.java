package com.example.harto;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionNotif {

    private static final String PREF_NAME = "userSettings";
    private static final String KEY_ON_NOTIF = "OnNotif";

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public SessionNotif(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = preferences.edit();
    }

    public void enableNotification() {
        editor.putBoolean(KEY_ON_NOTIF, true).apply();
    }

    public boolean isNotificationEnabled() {
        return preferences.getBoolean(KEY_ON_NOTIF, false);
    }

    public void clearSession() {
        editor.clear().apply();
    }
}
