package com.duam.scripty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import roboguice.activity.RoboActivity;
import roboguice.util.Ln;

import static com.duam.scripty.ScriptyConstants.PREF_DEVICE_CHECKED;
import static com.duam.scripty.ScriptyConstants.PREF_DEVICE_ID;
import static com.duam.scripty.ScriptyConstants.PREF_DEVICE_KEY;
import static com.duam.scripty.ScriptyConstants.PREF_USER_ID;


public class SignInActivity extends RoboActivity {
    @Override
    protected void onResume() {
        super.onResume();

        checkValidation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        final EditText editEmail = (EditText) findViewById(R.id.editEmail);

        ((Button) findViewById(R.id.btnSignIn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editEmail.getText().toString();

                new SendValidationTask(SignInActivity.this, email) {
                    @Override
                    protected void onPreExecute() throws Exception {
                        dialog.setMessage("Sending validation token to your e-mail...");
                        dialog.show();
                    }
                    @Override
                    protected void onException(Exception e) {
                        Toast.makeText(SignInActivity.this, "ERROR: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                        Ln.e(e);
                    }

                    @Override
                    protected void onSuccess(Device device) throws Exception {
                        super.onSuccess(device);

                        Ln.d("Storing device with id "+ device.getId() +" and key "+ device.getKey());

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SignInActivity.this);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putLong(PREF_DEVICE_ID, device.getId());
                        editor.putString(PREF_DEVICE_KEY, device.getKey());
                        editor.putBoolean(PREF_DEVICE_CHECKED, false);
                        editor.putLong(PREF_USER_ID, device.getUserId());
                        editor.commit();

                        Toast.makeText(SignInActivity.this, "Validation mail sent. Please check your inbox to start using Scripty!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    protected void onFinally() {
                        if (dialog.isShowing()) dialog.dismiss();
                    }
                }.execute();
            }
        });
    }

    private void checkValidation() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SignInActivity.this);

        if (prefs.contains(PREF_DEVICE_CHECKED)) {
            Ln.d("The deviceChecked pref exists");
            if (prefs.getBoolean(PREF_DEVICE_CHECKED, false)) {
                Ln.d("Already checked. Redirecting!");
                startActivity(new Intent(SignInActivity.this, MainActivity.class));
            } else {
                Ln.d("Not checked yet. Calling server to check...");
                long deviceId = prefs.getLong(PREF_DEVICE_ID, -1);

                new CheckValidationTask(SignInActivity.this, deviceId) {
                    @Override
                    protected void onSuccess(Boolean checked) throws Exception {
                        super.onSuccess(checked);

                        Ln.d("Server said: "+ checked);
                        if (checked) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean(PREF_DEVICE_CHECKED, true);
                            editor.commit();

                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                        }
                    }
                }.execute();
            }
        } else {
            Ln.d("The deviceChecked pref does not exist.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sign_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
