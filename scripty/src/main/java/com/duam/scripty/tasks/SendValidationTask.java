package com.duam.scripty.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.duam.scripty.activities.ValidationPendingActiviy;
import com.duam.scripty.db.Device;
import com.duam.scripty.ScriptyService;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONException;

import java.io.IOException;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;
import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;

import static com.duam.scripty.ScriptyConstants.PREF_DEVICE_CHECKED;
import static com.duam.scripty.ScriptyConstants.PREF_DEVICE_ID;
import static com.duam.scripty.ScriptyConstants.PREF_DEVICE_KEY;
import static com.duam.scripty.ScriptyConstants.PREF_USER_ID;
import static com.duam.scripty.ScriptyConstants.SCRIPTY_SERVER_URL;

/**
 * Created by luispablo on 11/05/14.
 */
public class SendValidationTask extends RoboAsyncTask<Device> {

    protected ProgressDialog dialog;

    protected String email;

    public SendValidationTask(Context context, String email) {
        super(context);
        this.dialog = new ProgressDialog(context);
        this.email = email;
    }

    @Override
    protected void onPreExecute() throws Exception {
        dialog.setMessage("Sending validation token to your e-mail...");
        dialog.show();
    }

    @Override
    protected void onException(Exception e) {
        Toast.makeText(getContext(), "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        Ln.e(e);
    }

    @Override
    protected void onFinally() {
        if (dialog.isShowing()) dialog.dismiss();
    }

    @Override
    public Device call() throws Exception {
        String userId = findUserId(email);
        Device device = null;

        if (userId == null || userId.trim().length() == 0) userId = createUser(email);

        if (userId == null || userId.trim().length() == 0) {
            throw new RuntimeException("Something's wrong... Cannot find nor create the user.");
        } else {
            device = createDevice(userId);
            Ln.d("Created device: "+ device.describe());
        }

        return device;
    }

    @Override
    protected void onSuccess(Device device) throws Exception {
        super.onSuccess(device);

        Ln.d("Storing device with id "+ device.getId() +" and key "+ device.getKey());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong(PREF_DEVICE_ID, device.getId());
        editor.putString(PREF_DEVICE_KEY, device.getKey());
        editor.putBoolean(PREF_DEVICE_CHECKED, false);
        editor.putLong(PREF_USER_ID, device.getUserId());
        editor.commit();

        Toast.makeText(getContext(), "Validation mail sent. Please check your inbox to start using Scripty!", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(getContext(), ValidationPendingActiviy.class);
        getContext().startActivity(intent);
    }

    private Device createDevice(String userId) throws IOException {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setErrorHandler(new ErrorHandler() {
                    @Override
                    public Throwable handleError(RetrofitError cause) {
                        Ln.e(cause);
                        return cause;
                    }
                })
                .setEndpoint(SCRIPTY_SERVER_URL).setConverter(new GsonConverter(gson)).build();
        ScriptyService service = restAdapter.create(ScriptyService.class);

        return service.createDevice(userId, userId);
    }

    private String createUser(String email) throws IOException, JSONException {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(SCRIPTY_SERVER_URL).build();
        ScriptyService service = restAdapter.create(ScriptyService.class);
        LinkedTreeMap response = service.createUser(email);

        if (response != null) {
            return String.valueOf(((Double) response.get("id")).intValue());
        } else {
            return null;
        }
    }

    private String findUserId(String email) throws IOException, JSONException {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(SCRIPTY_SERVER_URL).build();
        ScriptyService service = restAdapter.create(ScriptyService.class);
        LinkedTreeMap response = service.findUserByEmail(email);

        if (response != null) {
            return String.valueOf(((Double) response.get("id")).intValue());
        } else {
            return null;
        }
    }
}
