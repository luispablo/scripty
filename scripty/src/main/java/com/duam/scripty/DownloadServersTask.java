package com.duam.scripty;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import retrofit.RestAdapter;

import static com.duam.scripty.ScriptyConstants.SCRIPTY_SERVER_URL;

/**
 * Created by lgallo on 19/05/14.
 */
public class DownloadServersTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = DownloadServersTask.class.getName();

    protected long userId;
    protected Context context;

    public DownloadServersTask(Context context, long userId) {
        super();
        this.userId = userId;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(SCRIPTY_SERVER_URL).build();
        ScriptyService service = restAdapter.create(ScriptyService.class);

        List<Server> servers = service.getServers(this.userId);
        Log.d(TAG, servers.size() + " servers downloaded.");

        ScriptyHelper helper = new ScriptyHelper(this.context);

        for (Server s : servers) {
            helper.insertServer(s);

            Log.d(TAG, "Querying commands from server "+ s.getDescription());
            List<Command> commands = service.getCommands(s.get_id());
            Log.d(TAG, "Found "+ commands.size() +" commands");
            for (Command cmd : commands) {
                helper.insertCommand(cmd);
            }
        }
        Log.d(TAG, "Servers inserted.");

        return null;
    }
}
