package com.duam.scripty;

import android.app.ActivityManager;
import android.content.Context;
import android.widget.EditText;

/**
 * Created by luispablo on 19/06/14.
 */
public class Utils {

    public static boolean isEmpty(EditText edit) {
        return edit.getText().length() == 0;
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
