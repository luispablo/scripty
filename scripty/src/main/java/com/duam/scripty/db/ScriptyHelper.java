package com.duam.scripty.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.duam.scripty.UploadOperationsService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.util.Ln;

/**
 * Created by lgallo on 19/05/14.
 */
public class ScriptyHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "scripty_db";

    public static final String SERVERS_TABLE_NAME = "servers";
    public static final String ID = "_id";
    public static final String REMOTE_ID = "remote_id";
    public static final String USER_ID = "user_id";
    public static final String DESCRIPTION = "description";
    public static final String ADDRESS = "address";
    public static final String PORT = "port";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String SERVERS_TABLE_CREATE =
            "CREATE TABLE " + SERVERS_TABLE_NAME + " (" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    REMOTE_ID + " INTEGER, "+
                    USER_ID + " INTEGER, "+
                    DESCRIPTION + " VARCHAR(255), "+
                    ADDRESS + " VARCHAR(100), "+
                    PORT + " INTEGER, "+
                    USERNAME + " VARCHAR(100), "+
                    PASSWORD + " VARCHAR(100));";

    public static final String COMMANDS_TABLE_NAME = "commands";
    public static final String SERVER_ID = "server_id";
    public static final String COMMAND = "command";
    public static final String COMMAND_ID = "command_id";
    public static final String COMMANDS_TABLE_CREATE =
            "CREATE TABLE "+ COMMANDS_TABLE_NAME +" ("+
                    ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    REMOTE_ID +" INTEGER, "+
                    SERVER_ID +" INTEGER, "+
                    DESCRIPTION +" VARCHAR(255), "+
                    COMMAND +" VARCHAR(255));";

    public static final String[] SERVER_COLUMNS = {
            ID,
            USER_ID,
            DESCRIPTION,
            ADDRESS,
            PORT,
            USERNAME,
            PASSWORD,
            REMOTE_ID };

    public static final String OPERATIONS_TABLE_NAME = "operations";
    public static final String LOCAL_ID = "local_id";
    public static final String OPERATION_CODE = "operation_code";
    public static final String MODEL_CLASS = "model_class";
    public static final String CREATED_AT = "created_at";

    public static final String OPERATIONS_TABLE_CREATE =
            "CREATE TABLE "+ OPERATIONS_TABLE_NAME +" ("+
                    ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    LOCAL_ID +" INTEGER, "+
                    REMOTE_ID +" INTEGER, "+
                    OPERATION_CODE +" VARCHAR(50), "+
                    MODEL_CLASS +" VARCHAR(100), "+
                    CREATED_AT +" INTEGER );";

    public static final String[] OPERATION_COLUMNS = {
            ID, LOCAL_ID, REMOTE_ID, OPERATION_CODE, MODEL_CLASS, CREATED_AT };

    private Context context;

    public ScriptyHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SERVERS_TABLE_CREATE);
        db.execSQL(COMMANDS_TABLE_CREATE);
        db.execSQL(OPERATIONS_TABLE_CREATE);
    }

    public void emptyAllTables() {
        emptyCommandsTable();
        emptyServersTable();
        emptyOperationsTable();
    }

    public void emptyCommandsTable() {
        getWritableDatabase().delete(COMMANDS_TABLE_NAME, null, null);
    }

    public void emptyServersTable() {
        getWritableDatabase().delete(SERVERS_TABLE_NAME, null, null);
    }

    public void emptyOperationsTable() {
        getWritableDatabase().delete(OPERATIONS_TABLE_NAME, null, null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    protected long register(RemoteModel model, Operation.Code operation) {
        Operation op = Operation.register(model, operation);
        ContentValues values = values(op);
        long result =  getWritableDatabase().insert(OPERATIONS_TABLE_NAME, null, values);

        Ln.d("Registering: "+ operation.name() +": "+ model.toString());
        // Trigger db sincronization.
        UploadOperationsService.trigger(this.context);

        return result;
    }

    public List<Command> retrieveServerCommands(long serverId) {
        List<Command> commands = new ArrayList<>();
        Cursor c = getReadableDatabase().query(COMMANDS_TABLE_NAME, new String[]{ID, DESCRIPTION, COMMAND, REMOTE_ID}, SERVER_ID+" = ?", new String[]{String.valueOf(serverId)}, null, null, null);

        for (c.moveToFirst(); c.getCount() > 0 && !c.isAfterLast(); c.moveToNext()) {
            Command cmd = new Command();
            cmd.set_id(c.getLong(0));
            cmd.setServerId(serverId);
            cmd.setCommand(c.getString(2));
            cmd.setDescription(c.getString(1));
            cmd.setId(c.getLong(3));
            commands.add(cmd);
        }
        c.close();

        return commands;
    }

    public Command retrieveCommand(long id) {
        Cursor cursor = getReadableDatabase().query(COMMANDS_TABLE_NAME, new String[]{ID, DESCRIPTION, COMMAND, SERVER_ID, REMOTE_ID}, ID+" = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor.moveToFirst()) {
            Command command = new Command();
            command.setCommand(cursor.getString(2));
            command.setServerId(cursor.getLong(3));
            command.setId(cursor.getLong(4));
            command.setDescription(cursor.getString(1));
            command.set_id(id);

            cursor.close();

            return command;
        } else {
            return null;
        }
    }

    public Server retrieveServer(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(SERVERS_TABLE_NAME, SERVER_COLUMNS, ID+" = ?", new String[]{String.valueOf(id)}, null, null, null);

        Server server = null;

        if (c.moveToFirst()) {
            server = new Server();
            server.set_id(c.getLong(0));
            server.setUserId(c.getLong(1));
            server.setDescription(c.getString(2));
            server.setAddress(c.getString(3));
            server.setPort(c.getInt(4));
            server.setUsername(c.getString(5));
            server.setPassword(c.getString(6));
            server.setId(c.getLong(7));
        }

        return server;
    }

    public Command insertCommand(Command command) {
        command.set_id(getWritableDatabase().insert(COMMANDS_TABLE_NAME, null, values(command)));
        register(command, Operation.Code.INSERT);

        return command;
    }

    private ContentValues values(Operation op) {
        ContentValues values = new ContentValues();

        if (op.get_id() > 0) values.put(ID, op.get_id());
        if (op.getLocalId() > 0) values.put(LOCAL_ID, op.getLocalId());
        if (op.getRemoteId() > 0) values.put(REMOTE_ID, op.getRemoteId());

        values.put(OPERATION_CODE, op.getCode().name());
        values.put(MODEL_CLASS, op.getModelClass());
        values.put(CREATED_AT, op.getCreatedAt().getTime());

        return values;
    }

    private ContentValues values(Command command) {
        ContentValues values = new ContentValues();
        values.put(SERVER_ID, command.getServerId());
        values.put(DESCRIPTION, command.getDescription());
        values.put(COMMAND, command.getCommand());
        values.put(REMOTE_ID, command.getId());

        return values;
    }

    public Command updateCommand(Command command) {
        return updateCommand(command, true);
    }

    public Command updateCommand(Command command, boolean doRegister) {
        getWritableDatabase().update(COMMANDS_TABLE_NAME, values(command), ID+" = ?", new String[]{String.valueOf(command.get_id())});
        if (doRegister) register(command, Operation.Code.UPDATE);

        return command;
    }

    public int deleteCommand(long id) {
        Command cmd = retrieveCommand(id);

        int result = getWritableDatabase().delete(COMMANDS_TABLE_NAME, ID+ " = ?", new String[]{String.valueOf(id)});
        register(cmd, Operation.Code.DELETE);

        return result;
    }

    public int deleteServer(long id) {
        Server server = retrieveServer(id);
        int result = getWritableDatabase().delete(SERVERS_TABLE_NAME, ID+ " = ?", new String[]{String.valueOf(id)});
        register(server, Operation.Code.DELETE);

        return result;
    }

    public int deleteOperation(long id) {
        return getWritableDatabase().delete(OPERATIONS_TABLE_NAME, ID+ " = ?", new String[]{String.valueOf(id)});
    }

    private ContentValues values(Server server) {
        ContentValues values = new ContentValues();
        values.put(USER_ID, server.getUserId());
        values.put(DESCRIPTION, server.getDescription());
        values.put(ADDRESS, server.getAddress());
        values.put(PORT, server.getPort());
        values.put(USERNAME, server.getUsername());
        values.put(PASSWORD, server.getPassword());
        values.put(REMOTE_ID, server.getId());

        return values;
    }

    public Server insertServer(Server server) {
        server.set_id(getWritableDatabase().insert(SERVERS_TABLE_NAME, null, values(server)));
        register(server, Operation.Code.INSERT);

        return server;
    }

    public int updateServer(Server server) {
        return updateServer(server, true);
    }

    public int updateServer(Server server, boolean doRegister) {
        int result = getWritableDatabase().update(SERVERS_TABLE_NAME, values(server), ID+" = ?", new String[]{String.valueOf(server.get_id())});
        if (doRegister) register(server, Operation.Code.UPDATE);

        return result;
    }

    public boolean existsAnyServer() {
        Cursor c = getReadableDatabase().rawQuery("SELECT 1 FROM servers", null);
        return c.getCount() > 0;
    }

    public List<Operation> retrieveAllOperations() {
        List<Operation> operations = new ArrayList<>();

        Cursor c = getReadableDatabase().query(OPERATIONS_TABLE_NAME, OPERATION_COLUMNS, null, null, null, null, null);

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            Operation op = new Operation();
            op.set_id(c.getLong(c.getColumnIndexOrThrow(ID)));
            op.setCreatedAt(new Date(c.getLong(c.getColumnIndexOrThrow(CREATED_AT))));
            op.setModelClass(c.getString(c.getColumnIndexOrThrow(MODEL_CLASS)));
            op.setCode(Operation.Code.valueOf(c.getString(c.getColumnIndexOrThrow(OPERATION_CODE))));
            op.setRemoteId(c.getLong(c.getColumnIndexOrThrow(REMOTE_ID)));
            op.setLocalId(c.getLong(c.getColumnIndexOrThrow(LOCAL_ID)));

            operations.add(op);
        }

        return operations;
    }

    public List<Server> selectAllServers() {
        List<Server> servers = new ArrayList<>();
        String[] projection = {ID, USER_ID, DESCRIPTION, ADDRESS, PORT, USERNAME, PASSWORD, REMOTE_ID};
        Cursor c = getReadableDatabase().query(SERVERS_TABLE_NAME, projection, null, null, null, null, null);

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            Server s = new Server();
            s.set_id(c.getLong(c.getColumnIndexOrThrow(ID)));
            s.setAddress(c.getString(c.getColumnIndexOrThrow(ADDRESS)));
            s.setDescription(c.getString(c.getColumnIndexOrThrow(DESCRIPTION)));
            s.setPassword(c.getString(c.getColumnIndexOrThrow(PASSWORD)));
            s.setPort(c.getInt(c.getColumnIndexOrThrow(PORT)));
            s.setUserId(c.getLong(c.getColumnIndexOrThrow(USER_ID)));
            s.setUsername(c.getString(c.getColumnIndexOrThrow(USERNAME)));
            s.setId(c.getLong(c.getColumnIndexOrThrow(REMOTE_ID)));
            servers.add(s);
        }

        c.close();

        return servers;
    }
}
