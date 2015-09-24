package com.rukiasoft.androidapps.cocinaconroll.suggestions;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.rukiasoft.androidapps.cocinaconroll.Constants;

public class RecipeSuggestionsContentProvider extends ContentProvider {
	
	 public static final String AUTHORITY = Constants.PACKAGE_NAME + ".suggestions.RecipeSuggestionsContentProvider";
	 public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/recipesuggestions" );

     RecipeSuggestionsDB mRecipeSuggestionsDB = null;

     private static final int SUGGESTIONS_RECIPE = 1;
     private static final int SEARCH_RECIPE = 2;
     private static final int GET_RECIPE = 3;
     private static final int INSERT_RECIPE = 4;

     UriMatcher mUriMatcher = buildUriMatcher();
     
     private UriMatcher buildUriMatcher(){
    	 UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);    	 
    	 
    	 // Suggestion items of Search Dialog is provided by this uri
    	 uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SUGGESTIONS_RECIPE);
    	 
    	 // This URI is invoked, when user presses "Go" in the Keyboard of Search Dialog
    	 // Listview items of SearchableActivity is provided by this uri    	 
    	 uriMatcher.addURI(AUTHORITY, "recipesuggestions", SEARCH_RECIPE);
    	 
    	 // This URI is invoked, when user selects a suggestion from search dialog or an item from the listview
    	 uriMatcher.addURI(AUTHORITY, "recipesuggestions/#", GET_RECIPE);

		 // This URI is invoked, when app inserts a recipe into the database
    	 uriMatcher.addURI(AUTHORITY, "recipesuggestions/#", INSERT_RECIPE);
    	 
    	 return uriMatcher;
     }
     
     
     @Override
	 public boolean onCreate() {
		 	mRecipeSuggestionsDB = new RecipeSuggestionsDB(getContext());
		 	return true;
	 }
     
     @Override
	 public Cursor query(Uri uri, String[] projection, String selection,
			 String[] selectionArgs, String sortOrder) {
    	 
    	 Cursor c = null;
    	 switch(mUriMatcher.match(uri)){
    	 case SUGGESTIONS_RECIPE:
    		 c = mRecipeSuggestionsDB.getRecipes(selectionArgs);
    		 break;
    	 case SEARCH_RECIPE:
    		 c = mRecipeSuggestionsDB.getRecipes(selectionArgs);
    		 break;
    	 case GET_RECIPE:
    		 String id = uri.getLastPathSegment();
    		 c = mRecipeSuggestionsDB.getRecipe(id);
    	 }

    	 return c;
    	 
	}     

	 @Override
	 public int delete(Uri uri, String selection, String[] selectionArgs) {
		 	throw new UnsupportedOperationException();
	 }

	 @Override
	 public String getType(Uri uri) {
		 	throw new UnsupportedOperationException();
	 }

	 @Override
	 public Uri insert(Uri uri, ContentValues values) {
		 	throw new UnsupportedOperationException();
	 }	 
	 

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
	 		throw new UnsupportedOperationException();
	}
}