package com.duam.scripty;

import android.content.Context;

import java.util.List;

import retrofit.RestAdapter;
import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;

import static com.duam.scripty.ScriptyConstants.SCRIPTY_SERVER_URL;

/**
 * Created by lgallo on 19/05/14.
 */
public class DownloadServersTask extends RoboAsyncTask<Void> {
    protected long userId;

    public DownloadServersTask(Context context, long userId) {
        super(context);
        this.userId = userId;
    }

    @Override
    public Void call() throws Exception {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(SCRIPTY_SERVER_URL).build();
        ScriptyService service = restAdapter.create(ScriptyService.class);

        List<Server> servers = service.getServers(this.userId);
        Ln.d("Se descargaron "+ servers.size() +" servers.");

        ScriptyHelper helper = new ScriptyHelper(getContext());

        for (Server s : servers) {
            helper.insertServer(s);
        }
        Ln.d("Se insertaron los servers");

        return null;
    }
}
