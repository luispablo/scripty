package com.duam.scripty;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import roboguice.activity.RoboActivity;
import roboguice.util.Ln;


public class SignInActivity extends RoboActivity {

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
                    protected void onFinally() {
                        if (dialog.isShowing()) dialog.dismiss();
                    }
                }.execute();
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
