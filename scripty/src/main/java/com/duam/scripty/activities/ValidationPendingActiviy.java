package com.duam.scripty.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.duam.scripty.R;
import com.duam.scripty.ScriptyConstants;
import com.duam.scripty.tasks.CheckValidationTask;
import com.duam.scripty.tasks.LogoutTask;
import com.duam.scripty.tasks.SendValidationTask;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectPreference;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

import static com.duam.scripty.ScriptyConstants.PREF_DEVICE_CHECKED;
import static com.duam.scripty.ScriptyConstants.PREF_DEVICE_ID;
import static com.duam.scripty.ScriptyConstants.PREF_USER_EMAIL;

public class ValidationPendingActiviy extends RoboActivity {

    @InjectView(R.id.txtEmailSent) TextView txtEmailSent;

    @InjectResource(R.string.validation_sent) String validationSent;
    @InjectResource(R.string.not_validated) String notValidated;

    @InjectView(R.id.btnCheckValidation) Button btnCheckValidation;
    @InjectView(R.id.btnResendMail) Button btnResendMail;
    @InjectView(R.id.btnUseOtherMail) Button btnUserOtherMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validation_pending);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String email = prefs.getString(PREF_USER_EMAIL, "-");

        Ln.d("Formatting "+ validationSent + " with email "+ email);

        txtEmailSent.setText(String.format(validationSent, email));

        btnCheckValidation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkValidation();
            }
        });
        btnResendMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendMail();
            }
        });
        btnUserOtherMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userOtherMail();
            }
        });
    }

    private void checkValidation() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long deviceId = prefs.getLong(PREF_DEVICE_ID, -1);

        new CheckValidationTask(this, deviceId) {
            @Override
            protected void onPreExecute() throws Exception {
                dialog.show();
            }
            @Override
            protected void onSuccess(Boolean checked) throws Exception {
                super.onSuccess(checked);

                Ln.d("Server said: "+ checked);
                if (checked) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(PREF_DEVICE_CHECKED, true);
                    editor.commit();

                    Ln.d("Checked. Now loading servers");
                    Intent intent = new Intent(ValidationPendingActiviy.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(ValidationPendingActiviy.this, notValidated, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            protected void onFinally() throws RuntimeException {
                dialog.dismiss();
            }
        }.execute();
    }

    private void resendMail() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ValidationPendingActiviy.this);
        String email = prefs.getString(PREF_USER_EMAIL, "-");
        new SendValidationTask(ValidationPendingActiviy.this, email).execute();
    }

    private void userOtherMail() {
        new LogoutTask(this) {
            @Override
            protected void onPreExecute() throws Exception {
                dialog.show();
            }
            @Override
            protected void onSuccess(Void aVoid) throws Exception {
                Intent intent = new Intent(ValidationPendingActiviy.this, SignInActivity.class);
                startActivity(intent);
            }
            @Override
            protected void onFinally() throws RuntimeException {
                dialog.dismiss();
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.validation_pending_activiy, menu);
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
