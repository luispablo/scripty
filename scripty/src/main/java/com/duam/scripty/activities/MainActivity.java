package com.duam.scripty.activities;

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

import com.duam.scripty.CommandFragment;
import com.duam.scripty.R;
import com.duam.scripty.Utils;
import com.duam.scripty.db.ScriptyHelper;
import com.duam.scripty.db.Server;
import com.duam.scripty.services.FullDBSyncService;
import com.duam.scripty.services.UploadOperationsService;
import com.duam.scripty.tasks.FullDBSyncTask;
import com.duam.scripty.tasks.LogoutTask;

import org.apache.commons.lang.time.DateUtils;

import java.util.Date;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectResource;
import roboguice.util.Ln;

import static com.duam.scripty.ScriptyConstants.PREF_DEVICE_CHECKED;
import static com.duam.scripty.ScriptyConstants.PREF_USER_ID;
import static com.duam.scripty.ScriptyConstants.PREF_LAST_SYNC_DB_MILLIS;
import static com.duam.scripty.activities.CommandActivity.COMMAND_SAVED;
import static com.duam.scripty.activities.ServerActivity.SERVER_SAVED;
import static com.duam.scripty.db.ScriptyHelper.DESCRIPTION;
import static com.duam.scripty.db.ScriptyHelper.ID;
import static com.duam.scripty.db.ScriptyHelper.SERVERS_TABLE_NAME;
import static com.duam.scripty.db.ScriptyHelper.SERVER_ID;

/**
 * Created by luispablo on 06/06/14.
 */
public class MainActivity extends RoboActivity implements CommandFragment.OnFragmentInteractionListener {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private SimpleCursorAdapter adapter;
    private long currentServerId = -1;

    private static final int SERVER_FRAGMENT = 10;
    private static final int COMMAND_ACTIVITY = 20;

    @InjectResource(R.string.main_title) String mainTitle;
    @InjectResource(R.string.no_servers) String noServers;

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
                setServerNameAsTitle();
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

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // Trigger db sincronization.
        UploadOperationsService.trigger(getApplicationContext());

        loadServers();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkValidation();
    }

    private void checkValidation() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        if (prefs.getBoolean(PREF_DEVICE_CHECKED, false)) {
            Ln.d("Everything's fine!");
        } else {
            Ln.d("The deviceChecked pref does not exist. Goto signin");
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        }
    }

    public void selectServer(long serverId) {
        this.currentServerId = serverId;
        loadCommands(serverId);
        setServerNameAsTitle();
    }

    private void setServerNameAsTitle() {
        String title = "";

        if (currentServerId > 0) {
            ScriptyHelper helper = new ScriptyHelper(MainActivity.this);
            Server server = helper.retrieveServer(currentServerId);
            title = String.format(mainTitle, server.getDescription());
        } else {
            title = noServers;
        }
        getActionBar().setTitle(title);
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
            case R.id.action_sync_db:
                syncDB();
                return true;
            case R.id.action_add_server:
                addServer();
                return true;
            case R.id.action_add_command:
                addCommand(currentServerId);
                return true;
            case R.id.action_logout:
                new LogoutTask(this).execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void syncDB() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final long userId = prefs.getLong(PREF_USER_ID, -1);

        new FullDBSyncTask(this, userId) {
            @Override
            protected void onFinally() throws RuntimeException {
                super.onFinally();

                loadServers();
                loadCommands(currentServerId);
            }
        }.execute();
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
                    loadServers();
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

    public long loadServers() {
        Cursor c = serversCursor(new ScriptyHelper(this));

        if (adapter == null) {
            adapter = new SimpleCursorAdapter(MainActivity.this, R.layout.drawer_list_item, c, new String[]{DESCRIPTION}, new int[] {android.R.id.text1}, 0);
            mDrawerList.setAdapter(adapter);
        } else {
            adapter.swapCursor(c);
            adapter.notifyDataSetChanged();
        }

        long firstServerId = adapter.getItemId(0);

        if (firstServerId > 0) {
            selectServer(firstServerId);
        } else {
            offerServerDownload();
        }

        return firstServerId;
    }

    private Cursor serversCursor(ScriptyHelper helper) {
        return helper.getReadableDatabase().query(SERVERS_TABLE_NAME, new String[]{ID, DESCRIPTION}, null, null, null, null, null);
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

                new FullDBSyncTask(MainActivity.this, prefs.getLong(PREF_USER_ID, -1)) {
                    @Override
                    protected void onSuccess(Void aVoid) throws Exception {
                        currentServerId = loadServers();
                    }
                }.execute();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs.getLong(PREF_USER_ID, -1) > 0) {
            Date lastSyncDB = new Date(prefs.getLong(PREF_LAST_SYNC_DB_MILLIS, 0));
            Date today = new Date();
            Ln.d("Checking when was last sync (" + lastSyncDB + ") vs today (" + today + ")");

            if (DateUtils.isSameDay(lastSyncDB, today)) {
                Ln.d("Already synced today. Not scheduling again...");
            } else {
                Ln.d("Not synced today, scheduling to do it now.");
                Intent intent = new Intent(this, FullDBSyncService.class);
                startService(intent);
                prefs.edit().putLong(PREF_LAST_SYNC_DB_MILLIS, today.getTime());
                prefs.edit().commit();
            }
        } else {
            Ln.d("No user logged in. Cannot schedule sync.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (Utils.isOnline(this)) {
            Intent intent = new Intent(this, FullDBSyncService.class);
            startService(intent);
        }
    }

    @Override
    public void onFragmentInteraction(String id) {

    }
}
