package com.sensorberg.sdk.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "sensorberg.db";

    private static DBHelper instance = null;

    public static final String _ID ="_id";

    public static final String TABLE_GEOFENCES = "geofences";
    public static final String TG_FENCE = "fence";  //Geohash + zero padded radius as received from backend.

    private DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null)
            instance = new DBHelper(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_GEOFENCES + " (" +
                        TG_FENCE + " TEXT PRIMARY KEY " +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //Nuffin yet.
    }
}
