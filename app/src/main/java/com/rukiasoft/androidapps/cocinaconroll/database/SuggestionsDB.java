package com.rukiasoft.androidapps.cocinaconroll.database;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;

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
    	mAliasMap.put( SearchManager.SUGGEST_COLUMN_ICON_1,  SuggestionsTable.FIELD_ICON + " as " + SearchManager.SUGGEST_COLUMN_ICON_1);
    	
    	// This value will be appended to the Intent data on selecting an item from Search result or Suggestions ( Optional )
    	mAliasMap.put( SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,  SuggestionsTable.FIELD_ID + " as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID );
	}
		

	/** Returns Recipes  */
    public Cursor getRecipes(String[] selectionArgs){
    	
    	String selection =  SuggestionsTable.FIELD_NAME + " like ? ";
    	
    	if(selectionArgs!=null){
    		selectionArgs[0] = "%"+selectionArgs[0] + "%";   		
    	}    	    	
    	
    	SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    	queryBuilder.setProjectionMap(mAliasMap);
    	
    	queryBuilder.setTables( SuggestionsTable.TABLE_NAME);

		return queryBuilder.query(mCocinaConRollDatabaseHelper.getReadableDatabase(),
                new String[]{"_ID",
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_ICON_1,
                        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID},
                selection,
                selectionArgs,
                null,
                null,
				SuggestionsTable.FIELD_NAME + " asc ", "10"
        );
	    
    }
    
    /** Return Recipe corresponding to the id */
    public Cursor getRecipe(String id){
    	
    	SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();    	
    	
    	queryBuilder.setTables( SuggestionsTable.TABLE_NAME);

		return queryBuilder.query(mCocinaConRollDatabaseHelper.getReadableDatabase(),
                new String[]{"_id", "name", "icon"},
                "_id = ?", new String[]{id}, null, null, null, "1"
        );
    }

}