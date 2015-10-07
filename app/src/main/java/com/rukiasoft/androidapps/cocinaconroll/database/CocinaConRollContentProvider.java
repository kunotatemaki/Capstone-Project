package com.rukiasoft.androidapps.cocinaconroll.database;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

public class CocinaConRollContentProvider extends ContentProvider {


	public static final String AUTHORITY = "com.rukiasoft.androidapps.cocinaconroll.database.cocinaconrollcontentprovider";
	public static final Uri CONTENT_URI_SUGGESTIONS = Uri.parse("content://" + AUTHORITY + "/" + SuggestionsTable.TABLE_NAME);

    SuggestionsDB mSuggestionsDB = null;

    private static final int SUGGESTIONS_RECIPE = 1;
    private static final int SEARCH_RECIPE = 2;
    private static final int GET_RECIPE = 3;

    UriMatcher mUriMatcher = buildUriMatcher();

    private UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Suggestion items of Search Dialog is provided by this uri
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SUGGESTIONS_RECIPE);

        // This URI is invoked, when user presses "Go" in the Keyboard of Search Dialog
        // Listview items of SearchableActivity is provided by this uri
        uriMatcher.addURI(AUTHORITY, "suggestions", SEARCH_RECIPE);
        // This URI is invoked, when user selects a suggestion from search dialog or an item from the listview
        uriMatcher.addURI(AUTHORITY, "suggestions/#", GET_RECIPE);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mSuggestionsDB = new SuggestionsDB(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
    String[] selectionArgs, String sortOrder) {

        Cursor c = null;
        switch(mUriMatcher.match(uri)){
            case SUGGESTIONS_RECIPE:
                c = mSuggestionsDB.getRecipes(selectionArgs);
                break;
            case SEARCH_RECIPE:
                c = mSuggestionsDB.getRecipes(projection, selection, selectionArgs, sortOrder);
                break;
            case GET_RECIPE:
                String id = uri.getLastPathSegment();
                c = mSuggestionsDB.getRecipe(id);
        }
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (mUriMatcher.match(uri)){
            case SEARCH_RECIPE:
                return mSuggestionsDB.delete(selection, selectionArgs);
            default: throw new SQLException("Failed to insert row into " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
    throw new UnsupportedOperationException();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (mUriMatcher.match(uri)){
            case SEARCH_RECIPE:
                mSuggestionsDB.insert(values);
                break;
            default: throw new SQLException("Failed to insert row into " + uri);
        }
        return uri;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection,
        String[] selectionArgs) {
        int index;
        switch (mUriMatcher.match(uri)){
            case SEARCH_RECIPE:
                index = mSuggestionsDB.updateFavorite(values, selection, selectionArgs);
                break;
            default: throw new SQLException("Failed to insert row into " + uri);
        }
        return index;

    }
}