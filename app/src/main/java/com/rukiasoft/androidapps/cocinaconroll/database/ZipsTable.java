package com.rukiasoft.androidapps.cocinaconroll.database;

import android.database.sqlite.SQLiteDatabase;

import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;

/**
 * Created by Raúl Feliz Alonso on 24/09/2015 for the Udacity Nanodegree.
 */
public class ZipsTable {
    private static String TAG = LogHelper.makeLogTag(ZipsTable.class);
    // Database table
    final public static String FIELD_ID = "_id";
    final public static String FIELD_NAME = "name";
    final public static String FIELD_LINK = "link";
    final public static String FIELD_STATE = "state";

    public static final String TABLE_NAME = "zips";

    final public static String[] ALL_COLUMNS = {FIELD_ID, FIELD_NAME, FIELD_LINK, FIELD_STATE};

    // Database creation SQL statement
    private static final String DATABASE_CREATE = " create table if not exists "
            + TABLE_NAME + " ( " +
            FIELD_ID + " integer primary key autoincrement, " +
            FIELD_NAME + " TEXT NOT NULL, " +
            FIELD_LINK + " TEXT NOT NULL, " +
            FIELD_STATE + "  INTEGER" +
            ") " ;

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        /*LogHelper.w("Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("PRAGMA writable_schema = 1;");
        database.execSQL("delete from sqlite_master where type in ('table', 'index', 'trigger');");
        database.execSQL("PRAGMA writable_schema = 0;");
        database.execSQL("VACUUM;");
        database.execSQL("PRAGMA INTEGRITY_CHECK;");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);*/
    }
}
