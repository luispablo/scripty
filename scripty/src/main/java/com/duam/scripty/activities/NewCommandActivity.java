package com.duam.scripty.activities;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.duam.scripty.ScriptyException;
import com.duam.scripty.db.Command;
import com.duam.scripty.R;
import com.duam.scripty.db.ScriptyHelper;
import com.duam.scripty.Utils;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

import static com.duam.scripty.db.ScriptyHelper.SERVER_ID;
import static com.duam.scripty.db.ScriptyHelper.COMMAND;

public class NewCommandActivity extends RoboActivity {
    public static final int COMMAND_SAVED = 1;
    public static final int ACTION_CANCELED = 2;

    @InjectView(R.id.editDescription) EditText editDescription;
    @InjectView(R.id.editCommand) EditText editCommand;
    @InjectView(R.id.btnCommandCancel) Button btnCommandCancel;
    @InjectView(R.id.btnCommandOK) Button btnCommandOK;

    @InjectResource(R.string.field_required) String fieldRequired;
    @InjectResource(R.string.command_saved) String commandSaved;

    private long serverId;
    private long commandId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_command);

        serverId = getIntent().getLongExtra(SERVER_ID, -1);
        Ln.d("Command for server " + serverId);

        // If new_command given, keep its values for editing.
        initializeValues((Command) getIntent().getSerializableExtra(COMMAND));

        btnCommandCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(ACTION_CANCELED);
                finish();
            }
        });
        btnCommandOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (saveCommand()) {
                        setResult(COMMAND_SAVED);
                        finish();
                    }
                } catch (ScriptyException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
                    Ln.e(e);
                }
            }
        });
    }

    private void initializeValues(Command command) {
        if (command != null) {
            Ln.d("Received new_command with id "+ command.get_id());
            commandId = command.get_id();
            serverId = command.getServerId();
            editCommand.setText(command.getCommand());
            editDescription.setText(command.getDescription());
        }
    }

    private boolean saveCommand() throws ScriptyException {
        boolean saved = false;

        if (fieldsValid()) {
            ScriptyHelper helper = ScriptyHelper.getInstance(this);

            Ln.d("Saving new_command with id "+ commandId);
            Command command = new Command();

            if (commandId > 0) command = helper.retrieveCommand(commandId);

            command.setDescription(editDescription.getText().toString());
            command.setCommand(editCommand.getText().toString());
            command.setServerId(serverId);
            command.set_id(commandId);

            if (command.get_id() == -1) {
                helper.insert(command);
            } else {
                helper.update(command);
            }

            saved = true;
            Toast.makeText(this, commandSaved, Toast.LENGTH_LONG).show();
        }

        return saved;
    }

    private boolean fieldsValid() {
        boolean isValid = true;

        if (Utils.isEmpty(editDescription)) {
            editDescription.setError(fieldRequired);
            isValid = false;
        }
        if (Utils.isEmpty(editCommand)) {
            editCommand.setError(fieldRequired);
            isValid = false;
        }

        return isValid;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_command, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                                    // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
