package com.duam.scripty;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.duam.scripty.db.Command;
import com.duam.scripty.db.Operation;
import com.duam.scripty.db.Server;
import com.duam.scripty.db.ScriptyHelper;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import roboguice.util.Ln;

import static com.duam.scripty.ScriptyConstants.SCRIPTY_SERVER_URL;

/**
 * Created by luispablo on 28/07/14.
 */
public class UploadOperationsService extends IntentService {

    private static final String TAG = UploadOperationsService.class.getName();

    public UploadOperationsService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Ln.d("-- Service starting...");

        ScriptyHelper helper = new ScriptyHelper(getApplicationContext());
        List<Operation> operations = helper.retrieveAllOperations();

        if (operations.isEmpty()) {
            Ln.d("No operations to send to server.");
        } else {
            Ln.d(operations.size() +" operations to send to server");

            for (Operation op : operations) {
                try {
                    if (upload(op)) {
                        helper.deleteOperation(op.get_id());
                    } else {
                        Ln.e("Danger! ERROR!");
                        // ERROR... ???
                        helper.deleteOperation(op.get_id());
                        // POR AHORA LO DEJO ASÍ, PERO NO VA!
                    }
                } catch (RetrofitError e) {
                    Ln.e(e);
                }
            }
        }

        Ln.d("-- Service done!");
    }

    private boolean upload(Operation op) {
        boolean result = false;
        Ln.d("Uploading operation ["+ op.toString() +"]");

        if (op.getModelClass().equals(Server.class.getName())) {
            result = uploadServer(op);
        } else if (op.getModelClass().equals(Command.class.getName())) {
            result = uploadCommand(op);
        } else {
            // ERROR!!!
        }

        return result;
    }

    private boolean uploadServer(Operation op) {
        boolean result = false;

        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(SCRIPTY_SERVER_URL).build();
        ScriptyService service = restAdapter.create(ScriptyService.class);
        ScriptyHelper helper = new ScriptyHelper(getApplicationContext());

        Server server = helper.retrieveServer(op.getLocalId());

        if (server != null) {
            if (Operation.Code.INSERT.equals(op.getCode())) {
                server.setId(service.createServer(server.getUserId(),
                        server.getDescription(), server.getAddress(),
                        server.getPort(), server.getUsername(), server.getPassword()).getId());
                result = helper.updateServer(server, false) == 1;
                Ln.d("Created remote server: " + server.toString());
            } else if (Operation.Code.UPDATE.equals(op.getCode())) {
                service.updateServer(server.getId(), server.getUserId(), server.getDescription(),
                        server.getAddress(), server.getPort(), server.getUsername(), server.getPassword());
                result = true;
            }
        } else if (Operation.Code.DELETE.equals(op.getCode())) {
            service.deleteServer(op.getRemoteId());
            result = true;
        } else {
            // ERROR !!
        }

        return result;
    }

    private boolean uploadCommand(Operation op) {
        boolean result = false;

        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(SCRIPTY_SERVER_URL).build();
        ScriptyService service = restAdapter.create(ScriptyService.class);
        ScriptyHelper helper = new ScriptyHelper(getApplicationContext());

        Command cmd = helper.retrieveCommand(op.getLocalId());

        if (cmd != null) {
            if (Operation.Code.INSERT.equals(op.getCode())) {
                cmd.setId(service.createCommand(cmd.getServerId(), cmd.getDescription(), cmd.getCommand()).getId());
                helper.updateCommand(cmd, false);
                result = true;
            } else if (Operation.Code.UPDATE.equals(op.getCode())) {
                service.updateCommand(cmd.getId(), cmd.getServerId(), cmd.getDescription(), cmd.getCommand());
                result = true;
            }
        } else if (Operation.Code.DELETE.equals(op.getCode())) {
            service.deleteCommand(op.getRemoteId());
            result = true;
        } else {
            // ERROR !!!
        }

        return result;
    }

    public static void trigger(Context context) {
        if (!Utils.isServiceRunning(context, UploadOperationsService.class)) {
            Intent intent = new Intent(context, UploadOperationsService.class);
            context.startService(intent);
        } else {
            Ln.d("Not starting service, already started.");
        }
    }
}
