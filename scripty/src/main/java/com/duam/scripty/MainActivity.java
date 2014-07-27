package com.duam.scripty;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectResource;
import roboguice.util.Ln;

import static com.duam.scripty.ScriptyConstants.PREF_USER_ID;
import static com.duam.scripty.ScriptyHelper.DESCRIPTION;
import static com.duam.scripty.ScriptyHelper.ID;
import static com.duam.scripty.ScriptyHelper.SERVERS_TABLE_NAME;
import static com.duam.scripty.ScriptyHelper.SERVER_ID;
import static com.duam.scripty.ServerActivity.SERVER_SAVED;
import static com.duam.scripty.CommandActivity.COMMAND_SAVED;

/**
 * Created by luispablo on 06/06/14.
 */
public class MainActivity extends RoboActivity implements CommandFragment.OnFragmentInteractionListener{
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private SimpleCursorAdapter adapter;
    private long currentServerId = -1;

    private static final int SERVER_FRAGMENT = 10;
    private static final int COMMAND_ACTIVITY = 20;

    @InjectResource(R.string.main_title) String mainTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(getString(R.string.drawer_title));
            }
        };
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectServer(id);
            }
        });

        loadServers();

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    public void selectServer(long serverId) {
        loadCommands(serverId);
        setServerNameAsTitle(serverId);
    }

    private void setServerNameAsTitle(long serverId) {
        ScriptyHelper helper = new ScriptyHelper(MainActivity.this);
        Server server = helper.retrieveServer(serverId);

        getActionBar().setTitle(String.format(mainTitle, server.getDescription()));
    }

    private void loadCommands(long serverId) {
        currentServerId = serverId;

        Fragment fragment = CommandFragment.newInstance(serverId);
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle your other action bar items...
        switch (item.getItemId()) {
            case R.id.action_add_server:
                addServer();
                return true;
            case R.id.action_add_command:
                addCommand(currentServerId);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addCommand(long serverId) {
        Intent intent = new Intent(this, CommandActivity.class);
        intent.putExtra(SERVER_ID, serverId);
        startActivityForResult(intent, COMMAND_ACTIVITY);
    }

    private void addServer() {
        Intent intent = new Intent(this, ServerActivity.class);
        startActivityForResult(intent, SERVER_FRAGMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SERVER_FRAGMENT:
                if (resultCode == SERVER_SAVED) {
                    refreshServers();
                } else {
                    Ln.d("Not saved... :(");
                }
                break;
            case COMMAND_ACTIVITY:
                if (resultCode == COMMAND_SAVED) {
                    loadCommands(currentServerId);
                }
                break;
        }
    }

    public void refreshServers() {
        adapter.swapCursor(serversCursor(new ScriptyHelper(this)));
        adapter.notifyDataSetChanged();

        long firstServerId = adapter.getItemId(0);

        if (firstServerId > 0) {
            selectServer(firstServerId);
        }
    }

    private long loadServers() {
        ScriptyHelper helper = new ScriptyHelper(MainActivity.this);

        // Si no hay servers ofrecer la descarga.
        if (!helper.existsAnyServer()) {
            offerServerDownload();
            return -1;
        }
        else {
            return fillDrawer(helper);
        }
    }

    private Cursor serversCursor(ScriptyHelper helper) {
        return helper.getReadableDatabase().query(SERVERS_TABLE_NAME, new String[]{ID, DESCRIPTION}, null, null, null, null, null);
    }

    private long fillDrawer(ScriptyHelper helper) {
        Cursor cursor = serversCursor(helper);
        adapter = new SimpleCursorAdapter(MainActivity.this, R.layout.drawer_list_item, cursor, new String[]{DESCRIPTION}, new int[] {android.R.id.text1}, 0);
        mDrawerList.setAdapter(adapter);

        if (cursor.moveToFirst()) {
            return cursor.getLong(0);
        } else {
            return -1;
        }
    }

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
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                new DownloadServersTask(MainActivity.this, prefs.getLong(PREF_USER_ID, -1)) {
                    @Override
                    protected void onSuccess(Void aVoid) throws Exception {
                        super.onSuccess(aVoid);

                        currentServerId = fillDrawer(new ScriptyHelper(MainActivity.this));
                    }
                }.execute();
            }
        });
        builder.create().show();
    }

    @Override
    public void onFragmentInteraction(String id) {

    }
}
