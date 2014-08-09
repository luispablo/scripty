package com.duam.scripty.tasks;

import android.content.Context;

import com.duam.scripty.ScriptyService;
import com.duam.scripty.Utils;
import com.duam.scripty.db.Command;
import com.duam.scripty.db.ScriptyHelper;
import com.duam.scripty.db.Server;

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
        ScriptyService service = Utils.scriptyService();

        List<Server> servers = service.getServers(this.userId);
        Ln.d(servers.size() +" servers downloaded.");

        ScriptyHelper helper = new ScriptyHelper(getContext());

        for (Server s : servers) {
            helper.insert(s);

            Ln.d("Querying commands from server "+ s.getDescription());
            List<Command> commands = service.getCommands(s.get_id());
            Ln.d("Found "+ commands.size() +" commands");
            for (Command cmd : commands) {
                helper.insert(cmd);
            }
        }
        Ln.d("Servers inserted.");

        return null;
    }
}
