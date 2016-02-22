package com.rukiasoft.androidapps.cocinaconroll.database;

import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.HashMap;

public class RecipesDB {

    private final DatabaseRelatedTools dbTools;
	private final CocinaConRollDatabaseHelper mCocinaConRollDatabaseHelper;

	private final HashMap<String, String> mAliasMap;


	public RecipesDB(Context context){
        dbTools = new DatabaseRelatedTools();
		mCocinaConRollDatabaseHelper = new CocinaConRollDatabaseHelper(context);
		
		// This HashMap is used to map table fields to Custom Suggestion fields
    	mAliasMap = new HashMap<>();
    	
    	// Unique id for the each Suggestions ( Mandatory ) 
    	mAliasMap.put("_ID", RecipesTable.FIELD_ID + " as " + "_id" );
    	
    	// Text for Suggestions ( Mandatory )
    	mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1,  RecipesTable.FIELD_NAME + " as " + SearchManager.SUGGEST_COLUMN_TEXT_1);
    	
    	// Icon for Suggestions ( Optional ) 
    	mAliasMap.put(SearchManager.SUGGEST_COLUMN_ICON_1, RecipesTable.FIELD_ICON + " as " + SearchManager.SUGGEST_COLUMN_ICON_1);
    	
    	// This value will be appended to the Intent data on selecting an item from Search result or Suggestions ( Optional )
    	mAliasMap.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, RecipesTable.FIELD_NAME_NORMALIZED + " as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
	}
		

	/** Returns Recipes  */
    public Cursor getSuggestions(String[] selectionArgs){
        //call from search widget
    	String selection =  RecipesTable.FIELD_NAME_NORMALIZED + " like ? ";
        if(selectionArgs!=null){
    		selectionArgs[0] = "%"+dbTools.getNormalizedString(selectionArgs[0]) + "%";
    	}    	    	
    	
    	SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    	queryBuilder.setProjectionMap(mAliasMap);
    	
    	queryBuilder.setTables(RecipesTable.TABLE_NAME);

		return queryBuilder.query(mCocinaConRollDatabaseHelper.getReadableDatabase(),
                new String[]{"_ID",
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_ICON_1,
                        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID},
                selection,
                selectionArgs,
                null,
                null,
				RecipesTable.FIELD_NAME_NORMALIZED + " asc ", "50"
        );
    }

    /** Returns Suggestions  */
    public Cursor getSuggestions(String[] projection, String selection,
                             String[] selectionArgs, String sortOrder){

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(RecipesTable.TABLE_NAME);


        if(selection == null){
        //call from search widget when pressed, when user presses "Go" in the Keyboard of Search Dialog
            selection =  RecipesTable.FIELD_NAME_NORMALIZED + " like ? ";
            if(selectionArgs!=null){
                for(int i=0; i<selectionArgs.length; i++){
                    selectionArgs[i] = "%"+dbTools.getNormalizedString(selectionArgs[i]) + "%";
                }
            }
        }
        if(projection == null){
            //call from search widget when pressed, when user presses "Go" in the Keyboard of Search Dialog
            queryBuilder.setProjectionMap(mAliasMap);
            projection = new String[]{"_ID",
                    SearchManager.SUGGEST_COLUMN_TEXT_1,
                    SearchManager.SUGGEST_COLUMN_ICON_1,
                    SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};
        }
        if(sortOrder == null){
            sortOrder = RecipesTable.FIELD_NAME_NORMALIZED + " asc ";
        }


        return queryBuilder.query(mCocinaConRollDatabaseHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    /** Return Suggestion corresponding to the id */
    public Cursor getSuggestion(String id){
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables( RecipesTable.TABLE_NAME);
        return queryBuilder.query(mCocinaConRollDatabaseHelper.getReadableDatabase(),
                new String[]{RecipesTable.FIELD_ID, RecipesTable.FIELD_NAME, RecipesTable.FIELD_NAME_NORMALIZED, RecipesTable.FIELD_ICON},
                RecipesTable.FIELD_ID + " = ?", new String[]{id}, null, null, null, "1"
        );
    }

    /** Returns Recipes  */
    public Cursor getRecipes(String[] projection, String selection,
                             String[] selectionArgs, String sortOrder){

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(RecipesTable.TABLE_NAME);

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
    	queryBuilder.setTables( RecipesTable.TABLE_NAME);
		return queryBuilder.query(mCocinaConRollDatabaseHelper.getReadableDatabase(),
                RecipesTable.ALL_COLUMNS,
                RecipesTable.FIELD_ID + " = ?", new String[]{id}, null, null, null, "1"
        );
    }

	public Uri insert(ContentValues values){
        //first, check if exist
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(RecipesTable.TABLE_NAME);
        Cursor c = queryBuilder.query(mCocinaConRollDatabaseHelper.getReadableDatabase(),
                RecipesTable.ALL_COLUMNS,
                RecipesTable.FIELD_NAME_NORMALIZED+ " = ?", new String[]{values.get(RecipesTable.FIELD_NAME_NORMALIZED).toString()}, null, null, null, null
        );
        if(c.getCount()>0)
            return null;
		long regId;
		SQLiteDatabase db = mCocinaConRollDatabaseHelper.getWritableDatabase();
		regId = db.insert(RecipesTable.TABLE_NAME, null, values);
        return ContentUris.withAppendedId(CocinaConRollContentProvider.CONTENT_URI_RECIPES, regId);
	}

    public int updateFavorite(ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mCocinaConRollDatabaseHelper.getWritableDatabase();
        return db.update(RecipesTable.TABLE_NAME, values, selection, selectionArgs);
    }

    public int delete(String selection, String[] selectionArgs){
        SQLiteDatabase db = mCocinaConRollDatabaseHelper.getWritableDatabase();
        return db.delete(RecipesTable.TABLE_NAME, selection, selectionArgs);
    }
}