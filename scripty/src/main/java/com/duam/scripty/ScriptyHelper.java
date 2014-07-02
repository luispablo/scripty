package com.duam.scripty;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lgallo on 19/05/14.
 */
public class ScriptyHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "scripty_db";

    public static final String SERVERS_TABLE_NAME = "servers";
    public static final String ID = "_id";
    public static final String USER_ID = "user_id";
    public static final String DESCRIPTION = "description";
    public static final String ADDRESS = "address";
    public static final String PORT = "port";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String SERVERS_TABLE_CREATE =
            "CREATE TABLE " + SERVERS_TABLE_NAME + " (" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
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
            PASSWORD };

    public ScriptyHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SERVERS_TABLE_CREATE);
        db.execSQL(COMMANDS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    public Command retrieveCommand(long id) {
        Cursor cursor = getReadableDatabase().query(COMMANDS_TABLE_NAME, new String[]{ID, DESCRIPTION, COMMAND, SERVER_ID}, ID+" = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor.moveToFirst()) {
            Command command = new Command();
            command.setCommand(cursor.getString(2));
            command.setServerId(cursor.getLong(3));
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
        }

        return server;
    }

    public void insertCommand(Command command) {
        command.set_id(getWritableDatabase().insert(COMMANDS_TABLE_NAME, null, values(command)));
    }

    private ContentValues values(Command command) {
        ContentValues values = new ContentValues();
        values.put(SERVER_ID, command.getServerId());
        values.put(DESCRIPTION, command.getDescription());
        values.put(COMMAND, command.getCommand());

        return values;
    }

    public void updateCommad(Command command) {
        getWritableDatabase().update(COMMANDS_TABLE_NAME, values(command), ID+" = ?", new String[]{String.valueOf(command.get_id())});
    }

    public int deleteCommand(long id) {
        return getWritableDatabase().delete(COMMANDS_TABLE_NAME, ID+ " = ?", new String[]{String.valueOf(id)});
    }

    private ContentValues values(Server server) {
        ContentValues values = new ContentValues();
        values.put(USER_ID, server.getUserId());
        values.put(DESCRIPTION, server.getDescription());
        values.put(ADDRESS, server.getAddress());
        values.put(PORT, server.getPort());
        values.put(USERNAME, server.getUsername());
        values.put(PASSWORD, server.getPassword());

        return values;
    }

    public void insertServer(Server server) {
        server.set_id(getWritableDatabase().insert(SERVERS_TABLE_NAME, null, values(server)));
    }

    public int updateServer(Server server) {
        return getWritableDatabase().update(SERVERS_TABLE_NAME, values(server), ID+" = ?", new String[]{String.valueOf(server.get_id())});
    }

    public boolean existsAnyServer() {
        Cursor c = getReadableDatabase().rawQuery("SELECT 1 FROM servers", null);
        return c.getCount() > 0;
    }

    public List<Server> selectAllServers() {
        List<Server> servers = new ArrayList<>();
        String[] projection = {ID, USER_ID, DESCRIPTION, ADDRESS, PORT, USERNAME, PASSWORD};
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
            servers.add(s);
        }

        c.close();

        return servers;
    }
}
