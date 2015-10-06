package com.rukiasoft.androidapps.cocinaconroll.database;

import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import java.lang.reflect.Field;
import java.util.HashMap;

public class SuggestionsDB {


	private CocinaConRollDatabaseHelper mCocinaConRollDatabaseHelper;

	private HashMap<String, String> mAliasMap;


	public SuggestionsDB(Context context){
		mCocinaConRollDatabaseHelper = new CocinaConRollDatabaseHelper(context);
		
		// This HashMap is used to map table fields to Custom Suggestion fields
    	mAliasMap = new HashMap<>();
    	
    	// Unique id for the each Suggestions ( Mandatory ) 
    	mAliasMap.put("_ID", SuggestionsTable.FIELD_ID + " as " + "_id" );
    	
    	// Text for Suggestions ( Mandatory )
    	mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1,  SuggestionsTable.FIELD_NAME + " as " + SearchManager.SUGGEST_COLUMN_TEXT_1);
    	
    	// Icon for Suggestions ( Optional ) 
    	mAliasMap.put(SearchManager.SUGGEST_COLUMN_ICON_1, SuggestionsTable.FIELD_ICON + " as " + SearchManager.SUGGEST_COLUMN_ICON_1);
    	
    	// This value will be appended to the Intent data on selecting an item from Search result or Suggestions ( Optional )
    	mAliasMap.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, SuggestionsTable.FIELD_NAME_NORMALIZED + " as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
	}
		

	/** Returns Recipes  */
    public Cursor getRecipes(String[] selectionArgs){
        //call from search widget
    	String selection =  SuggestionsTable.FIELD_NAME_NORMALIZED + " like ? ";
        Tools tools = new Tools();
    	if(selectionArgs!=null){
    		selectionArgs[0] = "%"+tools.getNormalizedString(selectionArgs[0]) + "%";
    	}    	    	
    	
    	SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    	queryBuilder.setProjectionMap(mAliasMap);
    	
    	queryBuilder.setTables(SuggestionsTable.TABLE_NAME);

		return queryBuilder.query(mCocinaConRollDatabaseHelper.getReadableDatabase(),
                new String[]{"_ID",
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_ICON_1,
                        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID},
                selection,
                selectionArgs,
                null,
                null,
				SuggestionsTable.FIELD_NAME_NORMALIZED + " asc ", "50"
        );
    }

    /** Returns Recipes  */
    public Cursor getRecipes(String[] projection, String selection,
                             String[] selectionArgs, String sortOrder){

        Tools tools = new Tools();

        if(selection == null){
        //call from search widget when pressed, when user presses "Go" in the Keyboard of Search Dialog
            selection =  SuggestionsTable.FIELD_NAME_NORMALIZED + " like ? ";
            if(selectionArgs!=null){
                for(int i=0; i<selectionArgs.length; i++){
                    selectionArgs[i] = "%"+tools.getNormalizedString(selectionArgs[i]) + "%";
                }
            }
        }
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        if(projection == null){
            //call from search widget when pressed, when user presses "Go" in the Keyboard of Search Dialog
            queryBuilder.setProjectionMap(mAliasMap);
            projection = new String[]{"_ID",
                    SearchManager.SUGGEST_COLUMN_TEXT_1,
                    SearchManager.SUGGEST_COLUMN_ICON_1,
                    SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};
        }
        if(sortOrder == null){
            sortOrder = SuggestionsTable.FIELD_NAME_NORMALIZED + " asc ";
        }

        queryBuilder.setTables(SuggestionsTable.TABLE_NAME);

        return queryBuilder.query(mCocinaConRollDatabaseHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }


    /** Return Recipe corresponding to the id */
    public Cursor getRecipe(String id){
    	SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    	queryBuilder.setTables( SuggestionsTable.TABLE_NAME);
		return queryBuilder.query(mCocinaConRollDatabaseHelper.getReadableDatabase(),
                new String[]{SuggestionsTable.FIELD_ID, SuggestionsTable.FIELD_NAME, SuggestionsTable.FIELD_NAME_NORMALIZED, SuggestionsTable.FIELD_ICON},
                SuggestionsTable.FIELD_ID + " = ?", new String[]{id}, null, null, null, "1"
        );
    }

	public Uri insert(ContentValues values){
        //first, check if exist
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(SuggestionsTable.TABLE_NAME);
        Cursor c = queryBuilder.query(mCocinaConRollDatabaseHelper.getReadableDatabase(),
                new String[]{SuggestionsTable.FIELD_ID, SuggestionsTable.FIELD_NAME, SuggestionsTable.FIELD_NAME_NORMALIZED, SuggestionsTable.FIELD_ICON},
                SuggestionsTable.FIELD_NAME_NORMALIZED+ " = ?", new String[]{values.get(SuggestionsTable.FIELD_NAME_NORMALIZED).toString()}, null, null, null, null
        );
        if(c.getCount()>0)
            return null;
		long regId;
		SQLiteDatabase db = mCocinaConRollDatabaseHelper.getWritableDatabase();
		regId = db.insert(SuggestionsTable.TABLE_NAME, null, values);
        return ContentUris.withAppendedId(CocinaConRollContentProvider.CONTENT_URI_SUGGESTIONS, regId);
	}

    public int updateFavorite(ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mCocinaConRollDatabaseHelper.getWritableDatabase();
        int index =  db.update(SuggestionsTable.TABLE_NAME, values, selection, selectionArgs);
        return index;
    }
}