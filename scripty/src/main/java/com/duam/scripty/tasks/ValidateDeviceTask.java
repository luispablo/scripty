package com.duam.scripty.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.duam.scripty.R;
import com.duam.scripty.ScriptyConstants;
import com.duam.scripty.ScriptyException;
import com.duam.scripty.Utils;

import roboguice.inject.InjectResource;
import roboguice.util.RoboAsyncTask;

/**
 * Created by luispablo on 05/09/14.
 */
public class ValidateDeviceTask extends RoboAsyncTask<Void> {
    private long deviceId;
    private String key;

    @InjectResource(R.string.wrong_device_id_key) String wrongDeviceIdKey;

    public ValidateDeviceTask(Context context, long deviceId, String key) {
        super(context);
        this.deviceId = deviceId;
        this.key = key;
    }

    @Override
    public Void call() throws Exception {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        long prefDeviceId = prefs.getLong(ScriptyConstants.PREF_DEVICE_ID, -1);
        String prefKey = prefs.getString(ScriptyConstants.PREF_DEVICE_KEY, "");

        if (deviceId == prefDeviceId && key.equals(prefKey)) {
            Utils.scriptyService().validateDevice(deviceId, key);
        } else {
            throw new ScriptyException(wrongDeviceIdKey);
        }

        return null;
    }
}
