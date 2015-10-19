package com.rukiasoft.androidapps.cocinaconroll.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Raúl Feliz Alonso on 24/09/2015 for the Udacity Nanodegree.
 */
public class CocinaConRollDatabaseHelper extends SQLiteOpenHelper {


    private static final String DB_NAME = "cocinaconroll.db";

    private static final int DATABASE_VERSION = 1;


    public CocinaConRollDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        RecipesTable.onCreate(database);
        ZipsTable.onCreate(database);

    }


    // Method is called during an upgrade of the database,
    // e.g. if you increase the database version
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        RecipesTable.onUpgrade(database, oldVersion, newVersion);
        ZipsTable.onUpgrade(database, oldVersion, newVersion);
    }

}


