package com.rukiasoft.androidapps.cocinaconroll.database;

import android.database.sqlite.SQLiteDatabase;

import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;

/**
 * Created by Ruler on 24/09/2015 for the Udacity Nanodegree.
 */
public class RecipesTable {
    private static String TAG = LogHelper.makeLogTag(RecipesTable.class);
    // Database table
    public static final String FIELD_ID = "_id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ICON = "icon";
    public static final String FIELD_NAME_NORMALIZED = "normalized";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_FAVORITE = "favorite";
    public static final String FIELD_STATE = "state";
    public static final String FIELD_VEGETARIAN = "vegetarian";
    public static final String FIELD_PATH_PICTURE = "path_picture";
    public static final String FIELD_PATH_RECIPE = "path_recipe";
    public static final String FIELD_PATH_PICTURE_EDITED = "path_picture_edited";
    public static final String FIELD_PATH_RECIPE_EDITED = "path_recipe_edited";
    public static final String FIELD_DATE = "path_date";

    public static final String TABLE_NAME = "recipes";

    final public static String[] ALL_COLUMNS = {FIELD_ID, FIELD_NAME, FIELD_NAME_NORMALIZED, FIELD_TYPE,
            FIELD_ICON, FIELD_FAVORITE, FIELD_STATE, FIELD_VEGETARIAN, FIELD_PATH_RECIPE, FIELD_PATH_PICTURE,
            FIELD_PATH_RECIPE_EDITED, FIELD_PATH_PICTURE_EDITED, FIELD_DATE};

    // Database creation SQL statement
    private static final String DATABASE_CREATE = " create table " + TABLE_NAME + "" +
            " ( " +
            FIELD_ID + " integer primary key autoincrement, " +
            FIELD_NAME + " TEXT NOT NULL, " +
            FIELD_NAME_NORMALIZED + " TEXT NOT NULL, " +
            FIELD_TYPE + " TEXT NOT NULL, " +
            FIELD_ICON + "  int, " +
            FIELD_FAVORITE + "  int, " +
            FIELD_STATE + "  int, " +
            FIELD_VEGETARIAN + "  int, " +
            FIELD_PATH_RECIPE + " TEXT, " +
            FIELD_PATH_PICTURE + " TEXT, " +
            FIELD_PATH_RECIPE_EDITED + " TEXT, " +
            FIELD_PATH_PICTURE_EDITED + " TEXT, " +
            FIELD_DATE + " int " +
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
