package com.duam.scripty.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.duam.scripty.FullDBSyncCommand;
import com.duam.scripty.R;
import com.duam.scripty.Utils;

import roboguice.util.Ln;

import static com.duam.scripty.ScriptyConstants.PREF_USER_ID;

/**
 * Created by luispablo on 09/08/14.
 */
public class FullDBSyncService extends IntentService {
    private static final String TAG = FullDBSyncService.class.getName();

    public FullDBSyncService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Ln.d("-- Service starting...");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            new FullDBSyncCommand(getApplicationContext(), prefs.getLong(PREF_USER_ID, -1)).execute();
        } catch (Exception e) {
            Utils.simpleNotify(this, getString(R.string.sync_db), e.getMessage());
            Ln.e(e);
        }
    }
}
