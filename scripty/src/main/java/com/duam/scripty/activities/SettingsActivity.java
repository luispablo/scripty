package com.duam.scripty.activities;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;

import com.duam.scripty.R;

import roboguice.activity.RoboPreferenceActivity;

import static com.duam.scripty.ScriptyConstants.PREF_USER_EMAIL;

public class SettingsActivity extends RoboPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String email = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_USER_EMAIL, "-");
        EditTextPreference userEmail = (EditTextPreference) findPreference(PREF_USER_EMAIL);
        userEmail.setSummary(email);
    }
}
