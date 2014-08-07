package com.duam.scripty.tasks;

import android.content.Context;

import com.duam.scripty.ScriptyService;
import com.duam.scripty.db.Server;

import java.util.List;

import retrofit.RestAdapter;
import roboguice.util.RoboAsyncTask;

import static com.duam.scripty.ScriptyConstants.SCRIPTY_SERVER_URL;

/**
 * Created by luispablo on 06/08/14.
 */
public class DownloadServers2Task extends RoboAsyncTask<List<Server>> {
    private long userId;

    protected DownloadServers2Task(Context context, long userId) {
        super(context);
        this.userId = userId;
    }

    @Override
    public List<Server> call() throws Exception {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(SCRIPTY_SERVER_URL).build();
        ScriptyService service = restAdapter.create(ScriptyService.class);

        return service.getServers(this.userId);
    }
}
