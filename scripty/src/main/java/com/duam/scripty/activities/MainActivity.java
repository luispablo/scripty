package com.duam.scripty.activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.duam.scripty.CommandFragment;
import com.duam.scripty.R;
import com.duam.scripty.ScriptyException;
import com.duam.scripty.Utils;
import com.duam.scripty.db.ScriptyHelper;
import com.duam.scripty.db.Server;
import com.duam.scripty.services.FullDBSyncService;
import com.duam.scripty.services.UploadOperationsService;
import com.duam.scripty.tasks.FullDBSyncTask;
import com.duam.scripty.tasks.LogoutTask;
import com.duam.scripty.tasks.ValidateDeviceTask;

import org.apache.commons.lang.time.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectResource;
import roboguice.util.Ln;

import static com.duam.scripty.ScriptyConstants.PREF_DEVICE_CHECKED;
import static com.duam.scripty.ScriptyConstants.PREF_USER_ID;
import static com.duam.scripty.ScriptyConstants.PREF_LAST_SYNC_DB_MILLIS;
import static com.duam.scripty.activities.NewCommandActivity.COMMAND_SAVED;
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
    private ArrayAdapter<Object> adapter;
    private long currentServerId = -1;
    private boolean alreadyOfferedDownload = false;

    private static final int SERVER_FRAGMENT = 10;
    private static final int COMMAND_ACTIVITY = 20;

    private static final int SERVER_ITEM = 1;
    private static final int ACTION_ITEM = 2;

    @InjectResource(R.string.main_title) String mainTitle;
    @InjectResource(R.string.no_servers) String noServers;
    @InjectResource(R.string.add_server) String addServer;
    @InjectResource(R.string.validate_device_title) String validateDeviceTitle;
    @InjectResource(R.string.validate_device_message) String validateDeviceMessage;

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
                if (id > 0) {
                    selectServer(id);
                } else {
                    addServer();
                }
            }
        });

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // Trigger db sincronization.
        UploadOperationsService.trigger(getApplicationContext());

        try {
            loadServers();
        } catch (IllegalAccessException | InstantiationException e) {
            Ln.e(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Uri data = getIntent().getData();

        if (data != null) {
            validateDevice(data);
        } else {
            checkValidation();
        }
    }

    private void validateDevice(Uri data) {
        List<String> params = data.getPathSegments();
        long deviceId = Long.parseLong(params.get(1));
        String key = params.get(3);

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle(validateDeviceTitle);
        pd.setMessage(validateDeviceMessage);

        new ValidateDeviceTask(this, deviceId, key) {
            @Override
            protected void onPreExecute() throws Exception {
                pd.show();
            }
            @Override
            protected void onException(Exception e) throws RuntimeException {
                Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);

                if (!(e instanceof ScriptyException)) Ln.e(e);
            }
            @Override
            protected void onSuccess(Void aVoid) throws Exception {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putBoolean(PREF_DEVICE_CHECKED, true);
                editor.commit();
            }
            @Override
            protected void onFinally() throws RuntimeException {
                pd.dismiss();
            }
        }.execute();
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

    private Server currentServer() {
        if (currentServerId > 0) {
            ScriptyHelper helper = ScriptyHelper.getInstance(MainActivity.this);
            return helper.retrieveServer(currentServerId);
        } else {
            return null;
        }
    }

    private void setServerNameAsTitle() {
        Server currentServer = currentServer();

        if (currentServer != null) {
            getActionBar().setTitle(String.format(mainTitle, currentServer.getDescription()));
        } else {
            getActionBar().setTitle(noServers);
        }
    }

    private void loadCommands(long serverId) {
        currentServerId = serverId;

        CommandFragment fragment = CommandFragment.newInstance(serverId);
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

        Server currentServer = currentServer();

        ((MenuItem) menu.findItem(R.id.action_add_command)).setVisible(currentServer != null);
        ((MenuItem) menu.findItem(R.id.action_edit_server)).setVisible(currentServer != null);
        ((MenuItem) menu.findItem(R.id.action_delete_server)).setVisible(currentServer != null);

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
            case R.id.action_add_command:
                addCommand(currentServerId);
                return true;
            case R.id.action_logout:
                new LogoutTask(this).execute();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
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

                try {
                    loadServers();
                } catch (IllegalAccessException | InstantiationException e) {
                    Ln.e(e);
                }
                loadCommands(currentServerId);
            }
        }.execute();
    }

    private void addCommand(long serverId) {
        Intent intent = new Intent(this, NewCommandActivity.class);
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
                    try {
                        loadServers();
                    } catch (IllegalAccessException | InstantiationException e) {
                        Ln.e(e);
                    }
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

    public long loadServers() throws IllegalAccessException, InstantiationException {
        ScriptyHelper helper = ScriptyHelper.getInstance(this);

        List<Server> servers = helper.all(Server.class);

        final List<Object> items = new ArrayList<>();
        items.addAll(servers);
        items.add(addServer);

        adapter = new ArrayAdapter<Object>(this, R.layout.drawer_list_item, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);
                }

                String text = (getItemViewType(position) == SERVER_ITEM) ? ((Server) getItem(position)).getDescription(): (String) getItem(position);
                ((TextView) convertView.findViewById(R.id.txtDrawer)).setText(text);

                return convertView;
            }
            @Override
            public long getItemId(int position) {
                return (getItemViewType(position) == SERVER_ITEM) ? ((Server) getItem(position)).get_id(): -1;
            }
            @Override
            public int getItemViewType(int position) {
                if (items.get(position) instanceof Server) {
                    return SERVER_ITEM;
                } else if (items.get(position) instanceof String) {
                    return ACTION_ITEM;
                } else {
                    return -1;
                }
            }
            @Override
            public int getViewTypeCount() {
                if (currentServer() != null) {
                    return 2;
                } else {
                    return 1;
                }
            }
        };
        mDrawerList.setAdapter(adapter);

        long firstServerId = adapter.getItemId(0);

        if (servers == null || servers.isEmpty()) {
            if (!alreadyOfferedDownload) offerServerDownload();
        } else {
            selectServer(firstServerId);
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
        alreadyOfferedDownload = true;
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
