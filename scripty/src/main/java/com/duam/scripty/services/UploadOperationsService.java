package com.duam.scripty.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.duam.scripty.R;
import com.duam.scripty.ScriptyException;
import com.duam.scripty.ScriptyService;
import com.duam.scripty.Utils;
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
                    upload(op);
                } catch (ScriptyException e) {
                    Utils.simpleNotify(this, getString(R.string.upload_operations), e.getMessage());
                    Ln.e(e);
                } catch (RetrofitError e) {
                    Ln.e(e);
                } finally {
                    helper.deleteOperation(op.get_id());
                }
            }
        }

        Ln.d("-- Service done!");
    }

    private void upload(Operation op) throws ScriptyException{
        Ln.d("Uploading operation ["+ op.toString() +"]");

        if (op.isClass(Server.class)) {
            uploadServer(op);
        } else if (op.isClass(Command.class)) {
            uploadCommand(op);
        } else {
            String errorMessage = getString(R.string.op_upload_invalid_class, op.getModelClass());
            throw new ScriptyException(errorMessage);
        }
    }

    private void uploadServer(Operation op) throws ScriptyException {
        ScriptyService service = Utils.scriptyService();

        if (op.isDelete()) {
            service.deleteServer(op.getRemoteId());
        } else {
            ScriptyHelper helper = new ScriptyHelper(getApplicationContext());
            Server server = helper.retrieveServer(op.getLocalId());

            if (server != null) {
                if (op.isInsert()) {
                    server.setId(service.createServer(server.getUserId(), server.getDescription(),
                                server.getAddress(), server.getPort(), server.getUsername(),
                                server.getPassword()).getId());
                    helper.update(server, false);
                    Ln.d("Created remote server: " + server.toString());
                } else if (op.isUpdate()) {
                    service.updateServer(server.getId(), server.getUserId(), server.getDescription(),
                                        server.getAddress(), server.getPort(), server.getUsername(),
                                        server.getPassword());
                } else {
                    throw new ScriptyException(getString(R.string.op_upload_invalid_operation, op.getCode()));
                }
            } else {
                throw new ScriptyException(getString(R.string.op_upload_not_found, op.getModelClass(), op.getLocalId()));
            }
        }
    }

    private void uploadCommand(Operation op) throws ScriptyException{
        ScriptyService service = Utils.scriptyService();

        if (op.isDelete()) {
            service.deleteCommand(op.getRemoteId());
        } else {
            ScriptyHelper helper = new ScriptyHelper(getApplicationContext());
            Command cmd = helper.retrieveCommand(op.getLocalId());
            Server server = helper.retrieveServer(cmd.getServerId());

            if (cmd != null && server != null) {
                if (op.isInsert()) {
                    cmd.setId(service.createCommand(server.getId(), cmd.getDescription(), cmd.getCommand()).getId());
                    helper.update(cmd, false);
                } else if (op.isUpdate()) {

                    service.updateCommand(cmd.getId(), server.getId(), cmd.getDescription(), cmd.getCommand());
                } else {
                    throw new ScriptyException(getString(R.string.op_upload_invalid_operation, op.getCode()));
                }
            } else {
                throw new ScriptyException(getString(R.string.op_upload_not_found, op.getModelClass(), op.getLocalId()));
            }
        }
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
