package com.duam.scripty.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;

import com.duam.scripty.R;
import com.duam.scripty.ScriptyConstants;

import roboguice.activity.RoboPreferenceActivity;

import static com.duam.scripty.ScriptyConstants.DEFAULT_TIMEOUT_SECONDS;
import static com.duam.scripty.ScriptyConstants.PREF_APP_VERSION;
import static com.duam.scripty.ScriptyConstants.PREF_TIMEOUT_SECOND;
import static com.duam.scripty.ScriptyConstants.PREF_USER_EMAIL;

public class SettingsActivity extends RoboPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(PREF_APP_VERSION, ScriptyConstants.APP_VERSION);
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String email = prefs.getString(PREF_USER_EMAIL, "-");
        EditTextPreference userEmail = (EditTextPreference) findPreference(PREF_USER_EMAIL);
        userEmail.setSummary(email);

        String commandTimeout = prefs.getString(PREF_TIMEOUT_SECOND, String.valueOf(DEFAULT_TIMEOUT_SECONDS));
        EditTextPreference editCmdTimeout = (EditTextPreference) findPreference(PREF_TIMEOUT_SECOND);
        editCmdTimeout.setText(commandTimeout);

        String appVersion = prefs.getString(PREF_APP_VERSION, ScriptyConstants.APP_VERSION);
        EditTextPreference editAppVersion = (EditTextPreference) findPreference(PREF_APP_VERSION);
        editAppVersion.setSummary(appVersion);
    }
}
