package com.duam.scripty;

import static com.duam.scripty.ScriptyConstants.PREF_USER_ID;
import static com.duam.scripty.ScriptyHelper.*;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import roboguice.activity.RoboListActivity;
import roboguice.util.Ln;

/**
 * Created by luispablo on 06/06/14.
 */
public class CommandsActivity extends RoboListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScriptyHelper helper = new ScriptyHelper(CommandsActivity.this);

        // Si no hay servers ofrecer la descarga.
        if (!helper.existsAnyServer()) {
            Ln.d("There're no servers...");
            offerServerDownload();
        }
        else {
            Ln.d("We have servers!");
            loadCommands(helper);
        }
    }

    private void loadCommands(ScriptyHelper helper) {
        Ln.d("Loading commands...");
        Server server = helper.selectAllServers().iterator().next();
        Cursor cursor = helper.getReadableDatabase().query(COMMANDS_TABLE_NAME, new String[]{ID, COMMAND}, null, null, null, null, null);
        Ln.d("And found "+ cursor.getCount() +" commands.");
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(CommandsActivity.this, android.R.layout.two_line_list_item, cursor, new String[]{ID, COMMAND}, new int[] {android.R.id.text1, android.R.id.text2}, 0);
        setListAdapter(adapter);
    }

    private void offerServerDownload() {
        Ln.d("Offering server download");
        AlertDialog.Builder builder = new AlertDialog.Builder(CommandsActivity.this);
        builder.setMessage(getString(R.string.ask_download_servers));
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // TODO: Do nothing for now...
            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // TODO: Fire server download
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CommandsActivity.this);
                new DownloadServersTask(CommandsActivity.this, prefs.getLong(PREF_USER_ID, -1)) {

                }.execute();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // TODO: Offer options to do with item.
    }
}
