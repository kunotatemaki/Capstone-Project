package com.rukiasoft.androidapps.cocinaconroll.suggestions;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import java.util.HashMap;

public class RecipeSuggestionsDB {
	
	private static final String DBNAME = "country";
	
	private static final int VERSION = 1;
	
	private RecipeSuggestionsDBOpenHelper mRecipeSuggestionsDBOpenHelper;
	
	private static final String FIELD_ID = "_id";
	private static final String FIELD_NAME = "name";
	private static final String FIELD_ICON = "icon";
	private static final String TABLE_NAME = "recipesuggestions";
	private HashMap<String, String> mAliasMap;
	
	
	public RecipeSuggestionsDB(Context context){
		mRecipeSuggestionsDBOpenHelper = new RecipeSuggestionsDBOpenHelper(context, null);
		
		// This HashMap is used to map table fields to Custom Suggestion fields
    	mAliasMap = new HashMap<>();
    	
    	// Unique id for the each Suggestions ( Mandatory ) 
    	mAliasMap.put("_ID", FIELD_ID + " as " + "_id" );
    	
    	// Text for Suggestions ( Mandatory )
    	mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, FIELD_NAME + " as " + SearchManager.SUGGEST_COLUMN_TEXT_1);
    	
    	// Icon for Suggestions ( Optional ) 
    	mAliasMap.put( SearchManager.SUGGEST_COLUMN_ICON_1, FIELD_ICON + " as " + SearchManager.SUGGEST_COLUMN_ICON_1);
    	
    	// This value will be appended to the Intent data on selecting an item from Search result or Suggestions ( Optional )
    	mAliasMap.put( SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, FIELD_ID + " as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID );
	}
		

	/** Returns Recipes  */
    public Cursor getRecipes(String[] selectionArgs){
    	
    	String selection = FIELD_NAME + " like ? ";
    	
    	if(selectionArgs!=null){
    		selectionArgs[0] = "%"+selectionArgs[0] + "%";   		
    	}    	    	
    	
    	SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    	queryBuilder.setProjectionMap(mAliasMap);
    	
    	queryBuilder.setTables(TABLE_NAME);

		return queryBuilder.query(mRecipeSuggestionsDBOpenHelper.getReadableDatabase(),
                new String[]{"_ID",
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_ICON_1,
                        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID},
                selection,
                selectionArgs,
                null,
                null,
                FIELD_NAME + " asc ", "10"
        );
	    
    }
    
    /** Return Recipe corresponding to the id */
    public Cursor getRecipe(String id){
    	
    	SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();    	
    	
    	queryBuilder.setTables(TABLE_NAME);

		return queryBuilder.query(mRecipeSuggestionsDBOpenHelper.getReadableDatabase(),
                new String[]{"_id", "name", "icon"},
                "_id = ?", new String[]{id}, null, null, null, "1"
        );
    }

	
	class RecipeSuggestionsDBOpenHelper extends SQLiteOpenHelper{

		public RecipeSuggestionsDBOpenHelper(Context context,
											 CursorFactory factory) {
			super(context, DBNAME, factory, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql;
			
			// Defining table structure
			sql = " create table " + TABLE_NAME + "" +
				" ( " +
					FIELD_ID + " integer primary key autoincrement, " +
					FIELD_NAME + " varchar(100), " +
					FIELD_ICON + "  int " +
				") " ;
			
			// Creating table
			db.execSQL(sql);			
			
			for(int i=0;i<Country.countries.length;i++){
				
				// Defining insert statement
				sql = "insert into " + TABLE_NAME + " ( " +
						FIELD_NAME + " , " +
						FIELD_ICON + " ) " +
						" values ( " + 
						" '" + Country.countries[i] + "' ," +
						"  " + Country.flags[i] + ") ";
				
				// Inserting values into table
				db.execSQL(sql);					
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub			
		}		
	}	
}