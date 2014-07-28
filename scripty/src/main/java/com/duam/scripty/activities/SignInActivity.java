package com.duam.scripty.activities;

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

import com.duam.scripty.R;
import com.duam.scripty.db.Device;
import com.duam.scripty.tasks.SendValidationTask;

import roboguice.activity.RoboActivity;
import roboguice.util.Ln;

import static com.duam.scripty.ScriptyConstants.PREF_DEVICE_CHECKED;
import static com.duam.scripty.ScriptyConstants.PREF_DEVICE_ID;
import static com.duam.scripty.ScriptyConstants.PREF_DEVICE_KEY;
import static com.duam.scripty.ScriptyConstants.PREF_USER_ID;
import static com.duam.scripty.ScriptyConstants.PREF_USER_EMAIL;


public class SignInActivity extends RoboActivity {
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        final EditText editEmail = (EditText) findViewById(R.id.editEmail);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SignInActivity.this);
        final SharedPreferences.Editor editor = prefs.edit();

        ((Button) findViewById(R.id.btnSignIn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editEmail.getText().toString();

                editor.putString(PREF_USER_EMAIL, email);
                editor.commit();

                new SendValidationTask(SignInActivity.this, email).execute();
            }
        });
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
