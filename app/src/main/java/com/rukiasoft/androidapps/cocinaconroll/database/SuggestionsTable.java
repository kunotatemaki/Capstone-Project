package com.rukiasoft.androidapps.cocinaconroll.database;

import android.database.sqlite.SQLiteDatabase;

import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;

/**
 * Created by Ruler on 24/09/2015 for the Udacity Nanodegree.
 */
public class SuggestionsTable {
    private static String TAG = LogHelper.makeLogTag(SuggestionsTable.class);
    // Database table
    public static final String FIELD_ID = "_id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ICON = "icon";
    public static final String TABLE_NAME = "suggestions";

    // Database creation SQL statement
    private static final String DATABASE_CREATE = " create table " + TABLE_NAME + "" +
            " ( " +
            FIELD_ID + " integer primary key autoincrement, " +
            FIELD_NAME + " varchar(100), " +
            FIELD_ICON + "  int " +
            ") " ;

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        LogHelper.w("Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }
}
