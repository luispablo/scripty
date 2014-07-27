package com.duam.scripty.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.duam.scripty.R;
import com.duam.scripty.db.ScriptyHelper;

import roboguice.inject.InjectResource;
import roboguice.util.RoboAsyncTask;

/**
 * Created by luispablo on 27/07/14.
 */
public class LogoutTask extends RoboAsyncTask<Void> {

    protected ProgressDialog dialog;

    @InjectResource(R.string.logout) String logout;
    @InjectResource(R.string.logging_out) String loggingOut;

    public LogoutTask(Context context) {
        super(context);

        dialog = new ProgressDialog(context);
        dialog.setTitle(logout);
        dialog.setMessage(loggingOut);
    }

    @Override
    public Void call() throws Exception {
        ScriptyHelper helper = new ScriptyHelper(getContext());
        helper.deleteAllDBRecords();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();

        editor.clear();
        editor.commit();

        return null;
    }
}
