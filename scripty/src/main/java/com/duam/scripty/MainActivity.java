package com.duam.scripty;

import static com.duam.scripty.ScriptyConstants.PREF_USER_ID;
import static com.duam.scripty.ScriptyHelper.*;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import roboguice.activity.RoboActivity;
import roboguice.activity.RoboListActivity;
import roboguice.util.Ln;

/**
 * Created by luispablo on 06/06/14.
 */
public class MainActivity extends RoboActivity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        loadServers();
    }

    private void loadServers() {
        ScriptyHelper helper = new ScriptyHelper(MainActivity.this);

        // Si no hay servers ofrecer la descarga.
        if (!helper.existsAnyServer()) {
            Ln.d("There're no servers...");
            offerServerDownload();
        }
        else {
            Ln.d("We have servers!");
            fillDrawer(helper);
        }
    }

    private void fillDrawer(ScriptyHelper helper) {
        Cursor cursor = helper.getReadableDatabase().query(SERVERS_TABLE_NAME, new String[]{DESCRIPTION}, null, null, null, null, null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(MainActivity.this, android.R.layout.simple_list_item_1, cursor, new String[]{DESCRIPTION}, new int[] {android.R.id.text1}, 0);
        mDrawerList.setAdapter(adapter);
    }

//    private void loadCommands(ScriptyHelper helper) {
//        Ln.d("Loading commands...");
//        Server server = helper.selectAllServers().iterator().next();
//        Cursor cursor = helper.getReadableDatabase().query(COMMANDS_TABLE_NAME, new String[]{ID, COMMAND}, null, null, null, null, null);
//        Ln.d("And found "+ cursor.getCount() +" commands.");
//        SimpleCursorAdapter adapter = new SimpleCursorAdapter(MainActivity.this, android.R.layout.two_line_list_item, cursor, new String[]{ID, COMMAND}, new int[] {android.R.id.text1, android.R.id.text2}, 0);
//        setListAdapter(adapter);
//    }

    private void offerServerDownload() {
        Ln.d("Offering server download");
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                new DownloadServersTask(MainActivity.this, prefs.getLong(PREF_USER_ID, -1)) {
                    @Override
                    protected void onSuccess(Void aVoid) throws Exception {
                        super.onSuccess(aVoid);

                        fillDrawer(new ScriptyHelper(MainActivity.this));
                    }
                }.execute();
            }
        });
        builder.create().show();
    }

}
