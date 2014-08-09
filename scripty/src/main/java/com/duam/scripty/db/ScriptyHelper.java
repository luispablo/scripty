package com.duam.scripty.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.duam.scripty.ScriptyException;
import com.duam.scripty.services.UploadOperationsService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

    public static final String[] COMMAND_COLUMNS = {
            ID, REMOTE_ID, SERVER_ID, DESCRIPTION, COMMAND
    };

    public static final String[] SERVER_COLUMNS = {
            ID, USER_ID, DESCRIPTION, ADDRESS, PORT, USERNAME, PASSWORD, REMOTE_ID
    };

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

    public static final HashMap<Class, String[]> COLUMNS = new HashMap<>();
    public static final HashMap<Class, String> TABLES = new HashMap<>();

    static {
        TABLES.put(Server.class, SERVERS_TABLE_NAME);
        TABLES.put(Command.class, COMMANDS_TABLE_NAME);
        TABLES.put(Operation.class, OPERATIONS_TABLE_NAME);

        COLUMNS.put(Server.class, SERVER_COLUMNS);
        COLUMNS.put(Command.class, COMMAND_COLUMNS);
        COLUMNS.put(Operation.class, OPERATION_COLUMNS);
    }

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
        Cursor c = getReadableDatabase().query(COMMANDS_TABLE_NAME, new String[]{ID, DESCRIPTION, COMMAND, REMOTE_ID}, SERVER_ID + " = ?", new String[]{String.valueOf(serverId)}, null, null, null);

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

    private long getLong(Cursor c, String column) {
        return c.getLong(c.getColumnIndexOrThrow(column));
    }

    private String getString(Cursor c, String column) {
        return c.getString(c.getColumnIndexOrThrow(column));
    }

    private int getInt(Cursor c, String column) {
        return c.getInt(c.getColumnIndexOrThrow(column));
    }

    private Command buildCommandFromCursor(Cursor c) {
        Command command = new Command();
        command.setCommand(getString(c, COMMAND));
        command.setServerId(getLong(c, SERVER_ID));
        command.setId(getLong(c, REMOTE_ID));
        command.setDescription(getString(c, DESCRIPTION));
        command.set_id(getLong(c, ID));

        return command;
    }

    private Server buildServerFromCursor(Cursor c) {
        Server server = new Server();
        server.setDescription(getString(c, DESCRIPTION));
        server.set_id(getLong(c, ID));
        server.setId(getLong(c, REMOTE_ID));
        server.setAddress(getString(c, ADDRESS));
        server.setPassword(getString(c, PASSWORD));
        server.setPort(getInt(c, PORT));
        server.setUserId(getLong(c, USER_ID));
        server.setUsername(getString(c, USERNAME));

        return server;
    }

    private <T extends RemoteModel> T buildFromCursor(Cursor cursor, Class<T> modelClass)
            throws IllegalAccessException, InstantiationException {
        T modelObject = null;

        if (modelClass.equals(Server.class)) {
            modelObject = (T) buildServerFromCursor(cursor);
        } else if (modelClass.equals(Command.class)) {
            modelObject = (T) buildCommandFromCursor(cursor);
        }

        return modelObject;
    }

    public Command retrieveCommand(long id) {
        Command command = null;
        Cursor cursor = getReadableDatabase().query(COMMANDS_TABLE_NAME, COMMAND_COLUMNS, ID+" = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor.moveToFirst()) command = buildCommandFromCursor(cursor);

        cursor.close();

        return command;
    }

    public Server retrieveServerByRemoteId(long remoteId) {
        Server server = null;
        Cursor c = getReadableDatabase().query(TABLES.get(Server.class), COLUMNS.get(Server.class),
                REMOTE_ID+" = ?", new String[]{String.valueOf(remoteId)}, null, null, null);

        if (c.moveToFirst()) server = buildServerFromCursor(c);

        return server;
    }

    public Server retrieveServer(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(SERVERS_TABLE_NAME, SERVER_COLUMNS, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

        Server server = null;

        if (c.moveToFirst()) server = buildServerFromCursor(c);

        return server;
    }

    public void insert(RemoteModel obj) throws ScriptyException {
        insert(obj, true);
    }

    public void insert(RemoteModel obj, boolean doRegister) throws ScriptyException {
        SQLiteDatabase db = getWritableDatabase();
        long _id = db.insert(TABLES.get(obj.getClass()), null, values(obj));
        obj.set_id(_id);
        Ln.d("Inserted: "+ obj);

        if (doRegister) register(obj, Operation.Code.INSERT);
    }

    private ContentValues values(RemoteModel obj) throws ScriptyException {
        ContentValues values = null;

        if (obj instanceof Server) {
            values = values((Server) obj);
        } else if (obj instanceof Command) {
            values = values((Command) obj);
        } else {
            throw new ScriptyException("Cannot get values from class "+ obj.getClass());
        }

        return values;
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

    public int update(RemoteModel obj) throws ScriptyException {
        return update(obj, true);
    }

    public int update(RemoteModel obj, boolean doRegister) throws ScriptyException {
        Ln.d("Updating object ["+ obj.toString() +"]");
        String table = TABLES.get(obj.getClass());
        Ln.d("Table name: "+ table);
        ContentValues values = values(obj);
        Ln.d("Values to set: "+ values);
        SQLiteDatabase db = getWritableDatabase();
        Ln.d("Local ID: "+ obj.get_id());

        int rows = db.update(table, values, ID + " = ?", new String[]{String.valueOf(obj.get_id())});

        if (doRegister) register(obj, Operation.Code.UPDATE);

        return rows;
    }

    public int delete(RemoteModel model, boolean doRegister) throws ScriptyException {
        int result = 0;
        SQLiteDatabase db = getWritableDatabase();
        String tableName = TABLES.get(model.getClass());

        if (model.get_id() > 0) {
            // Use local id
            result = db.delete(tableName, ID+" = ?", new String[]{String.valueOf(model.get_id())});
        } else if (model.getId() > 0) {
            // Use remote id
            result = db.delete(tableName, REMOTE_ID+" = ?", new String[]{String.valueOf(model.getId())});
        } else {
            // Cannot delete!
            throw new ScriptyException("No ID to delete");
        }

        return result;
    }

    public int deleteCommand(long id) {
        return deleteCommand(id, true);
    }

    public int deleteCommand(long id, boolean doRegister) {
        Command cmd = retrieveCommand(id);

        int result = getWritableDatabase().delete(COMMANDS_TABLE_NAME, ID+ " = ?", new String[]{String.valueOf(id)});

        if (doRegister) register(cmd, Operation.Code.DELETE);

        return result;
    }

    public int deleteServer(long id) {
        return deleteServer(id, true);
    }

    public int deleteServer(long id, boolean doRegister) {
        Server server = retrieveServer(id);
        int result = getWritableDatabase().delete(SERVERS_TABLE_NAME, ID+ " = ?", new String[]{String.valueOf(id)});

        if (doRegister) register(server, Operation.Code.DELETE);

        return result;
    }

    public int deleteOperation(long id) {
        return getWritableDatabase().delete(OPERATIONS_TABLE_NAME, ID+ " = ?", new String[]{String.valueOf(id)});
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

    public <T extends RemoteModel> List<T> all(Class<T> modelClass)
    throws InstantiationException, IllegalAccessException {
        List<T> models = new ArrayList<>();
        Cursor c = getReadableDatabase().query(TABLES.get(modelClass), COLUMNS.get(modelClass), null, null, null, null, null);

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            models.add(buildFromCursor(c, modelClass));
        }
        c.close();

        return models;
    }

    public RemoteModel fixReferences(RemoteModel obj) throws ScriptyException {
        if (obj instanceof Command) {
            return fixReferences((Command) obj);
        } else if (obj instanceof Server) {
            return obj; // (nothing to fix...
        } else {
            throw new ScriptyException("Cannot fix references for "+ obj.getClass().getName());
        }
    }

    public Command fixReferences(Command cmd) {
        Ln.d("Searching server with id "+ cmd.getServerId());
        cmd.setServerId(retrieveServerByRemoteId(cmd.getServerId()).get_id());
        return cmd;
    }

    public <T extends RemoteModel> void sync(List<T> remotes, Class<T> clazz)
    throws IllegalAccessException, InstantiationException, ScriptyException {
        Ln.d("Syncing "+ clazz.getName() +" with "+ remotes.size() +" remotes.");
        List<T> locals = all(clazz);

        for (RemoteModel remote : remotes) {
            remote = fixReferences(remote);
            Ln.d(" - remote ["+ remote.toString() +"]");
            T local = RemoteModel.getByRemoteId(locals, remote.getId());

            if (local != null) {
                Ln.d(" --- local ["+ local.toString() +"]");
                remote.set_id(local.get_id());
                locals.remove(local);

                if (local.hasChanged(remote)) {
                    Ln.d(" ------ changed, UPDATE.");
                    update(remote, false);
                } else {
                    Ln.d(" ------ not changed.");
                }
            } else {
                Ln.d(" --- has no local match, INSERT.");
                insert(remote, false);
            }
        }

        Ln.d(" - "+ locals.size() +" locals remaining to delete.");
        for (T local : locals) {
            Ln.d(" --- deleting ["+ local.toString() +"]");
            delete(local, false);
        }
    }
}
