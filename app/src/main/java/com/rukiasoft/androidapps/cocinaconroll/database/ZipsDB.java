package com.rukiasoft.androidapps.cocinaconroll.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class ZipsDB {

    private final CocinaConRollDatabaseHelper mCocinaConRollDatabaseHelper;
    private long regId;
	public ZipsDB(Context context){
        mCocinaConRollDatabaseHelper = new CocinaConRollDatabaseHelper(context);
	}

    /** Returns Zips  */
    public Cursor getZips(String[] projection, String selection,
                             String[] selectionArgs, String sortOrder){

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        if(sortOrder == null){
            sortOrder = ZipsTable.FIELD_NAME+ " asc ";
        }

        queryBuilder.setTables(ZipsTable.TABLE_NAME);

        return queryBuilder.query(mCocinaConRollDatabaseHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }


    /** Return Zip corresponding to the id */
    public Cursor getZip(String id){
    	SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    	queryBuilder.setTables( ZipsTable.TABLE_NAME);
		return queryBuilder.query(mCocinaConRollDatabaseHelper.getReadableDatabase(),
                new String[]{ZipsTable.FIELD_ID, ZipsTable.FIELD_NAME, ZipsTable.FIELD_LINK, ZipsTable.FIELD_STATE},
                ZipsTable.FIELD_ID + " = ?", new String[]{id}, null, null, null, "1"
        );
    }

	public Uri insert(ContentValues values){
        //first, check if exist
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(ZipsTable.TABLE_NAME);
        Cursor c = queryBuilder.query(mCocinaConRollDatabaseHelper.getReadableDatabase(),
                new String[]{ZipsTable.FIELD_ID, ZipsTable.FIELD_NAME, ZipsTable.FIELD_LINK, ZipsTable.FIELD_STATE},
                ZipsTable.FIELD_NAME+ " = ?", new String[]{values.get(ZipsTable.FIELD_NAME).toString()}, null, null, null, null
        );
        if(c.getCount()>0) {
            return ContentUris.withAppendedId(CocinaConRollContentProvider.CONTENT_URI_ZIPS, -1);
        }
		SQLiteDatabase db = mCocinaConRollDatabaseHelper.getWritableDatabase();
		regId = db.insert(ZipsTable.TABLE_NAME, null, values);
        Log.d("ZIP_DATABASE", "regId: " + ContentUris.withAppendedId(CocinaConRollContentProvider.CONTENT_URI_ZIPS, regId).toString());
        return ContentUris.withAppendedId(CocinaConRollContentProvider.CONTENT_URI_ZIPS, regId);
	}

    public int updateState(ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mCocinaConRollDatabaseHelper.getWritableDatabase();
        return db.update(ZipsTable.TABLE_NAME, values, selection, selectionArgs);
    }

    public int delete(String selection, String[] selectionArgs){
        SQLiteDatabase db = mCocinaConRollDatabaseHelper.getWritableDatabase();
        return db.delete(ZipsTable.TABLE_NAME, selection, selectionArgs);
    }
}