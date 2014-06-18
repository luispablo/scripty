package com.duam.scripty;

import static com.duam.scripty.ScriptyConstants.PREF_DEVICE_ID;
import static com.duam.scripty.ScriptyConstants.PREF_DEVICE_KEY;
import static com.duam.scripty.ScriptyConstants.PREF_DEVICE_CHECKED;
import static com.duam.scripty.ScriptyConstants.PREF_USER_ID;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class SignInActivity extends Activity {
    private static final String TAG = SignInActivity.class.getName();

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
                    protected void onPreExecute() {
                        dialog.setMessage("Sending validation token to your e-mail...");
                        dialog.show();
                    }
                    @Override
                    protected void onException(Exception e) {
                        Toast.makeText(SignInActivity.this, "ERROR: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error sending validation", e);
                    }

                    @Override
                    protected void onPostExecute(Device device) {
                        super.onPostExecute(device);

                        if (device != null) {
                            Log.d(TAG, "Storing device with id "+ device.getId() +" and key "+ device.getKey());

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SignInActivity.this);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putLong(PREF_DEVICE_ID, device.getId());
                            editor.putString(PREF_DEVICE_KEY, device.getKey());
                            editor.putBoolean(PREF_DEVICE_CHECKED, false);
                            editor.putLong(PREF_USER_ID, device.getUserId());
                            editor.commit();

                            Toast.makeText(SignInActivity.this, "Validation mail sent. Please check your inbox to start using Scripty!", Toast.LENGTH_LONG).show();
                        }
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SignInActivity.this);

        if (prefs.contains(PREF_DEVICE_CHECKED)) {
            Log.d(TAG, "The deviceChecked pref exists");
            if (prefs.getBoolean(PREF_DEVICE_CHECKED, false)) {
                Log.d(TAG, "Already checked. Redirecting!");
                startActivity(new Intent(SignInActivity.this, MainActivity.class));
            } else {
                Log.d(TAG, "Not checked yet. Calling server to check...");
                long deviceId = prefs.getLong(PREF_DEVICE_ID, -1);

                new CheckValidationTask(deviceId) {
                    @Override
                    protected void onPostExecute(Boolean checked) {
                        super.onPostExecute(checked);

                        Log.d(TAG, "Server said: "+ checked);
                        if (checked) {
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                        }
                    }
                }.execute();
            }
        } else {
            Log.d(TAG, "The deviceChecked pref does not exist.");
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
