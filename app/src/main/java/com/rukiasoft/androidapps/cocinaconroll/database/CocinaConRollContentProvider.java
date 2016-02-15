package com.rukiasoft.androidapps.cocinaconroll.database;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.support.annotation.NonNull;

public class CocinaConRollContentProvider extends ContentProvider {


	public static final String AUTHORITY = "com.rukiasoft.androidapps.cocinaconroll.database.cocinaconrollcontentprovider";
	public static final Uri CONTENT_URI_SUGGESTIONS_WHEN_KEYBOARD_GO = Uri.parse("content://" + AUTHORITY + "/suggestions");
	public static final Uri CONTENT_URI_RECIPES = Uri.parse("content://" + AUTHORITY + "/" + RecipesTable.TABLE_NAME);
	public static final Uri CONTENT_URI_ZIPS = Uri.parse("content://" + AUTHORITY + "/" + ZipsTable.TABLE_NAME);

    private RecipesDB mRecipesDB = null;
    private ZipsDB mZipsDB = null;

    private static final int SUGGESTIONS_RECIPE = 1;
    private static final int SEARCH_SUGGESTION = 2;
    private static final int GET_SUGGESTION = 3;
    private static final int SEARCH_RECIPE = 4;
    private static final int GET_RECIPE = 5;
    private static final int SEARCH_ZIP = 6;
    private static final int GET_ZIP = 7;


    private final UriMatcher mUriMatcher = buildUriMatcher();

    private UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Suggestion items of Search Dialog is provided by this uri
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SUGGESTIONS_RECIPE);

        // This URI is invoked, when user presses "Go" in the Keyboard of Search Dialog
        // Listview items of SearchableActivity is provided by this uri
        uriMatcher.addURI(AUTHORITY, "suggestions", SEARCH_SUGGESTION);
        // This URI is invoked, when user selects a suggestion from search dialog or an item from the listview
        uriMatcher.addURI(AUTHORITY, "suggestions/#", GET_SUGGESTION);

        uriMatcher.addURI(AUTHORITY, RecipesTable.TABLE_NAME, SEARCH_RECIPE);
        // This URI is invoked, when user selects a suggestion from search dialog or an item from the listview
        uriMatcher.addURI(AUTHORITY, RecipesTable.TABLE_NAME + "/#", GET_RECIPE);

        uriMatcher.addURI(AUTHORITY, "zips", SEARCH_ZIP);

        uriMatcher.addURI(AUTHORITY, "zips/#", GET_ZIP);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mRecipesDB = new RecipesDB(getContext());
        mZipsDB = new ZipsDB(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor c = null;
        String id_recipe;
        switch(mUriMatcher.match(uri)){
            case SUGGESTIONS_RECIPE:
                c = mRecipesDB.getSuggestions(selectionArgs);
                break;
            case SEARCH_SUGGESTION:
                c = mRecipesDB.getSuggestions(projection, selection, selectionArgs, sortOrder);
                break;
            case GET_SUGGESTION:
                id_recipe = uri.getLastPathSegment();
                c = mRecipesDB.getSuggestion(id_recipe);
                break;
            case SEARCH_RECIPE:
                c = mRecipesDB.getRecipes(projection, selection, selectionArgs, sortOrder);
                break;
            case GET_RECIPE:
                id_recipe = uri.getLastPathSegment();
                c = mRecipesDB.getRecipe(id_recipe);
                break;
            case SEARCH_ZIP:
                c = mZipsDB.getZips(projection, selection, selectionArgs, sortOrder);
                break;
            case GET_ZIP:
                String id_zip = uri.getLastPathSegment();
                c = mZipsDB.getZip(id_zip);
                break;
        }
        return c;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        switch (mUriMatcher.match(uri)){
            case SEARCH_RECIPE:
                return mRecipesDB.delete(selection, selectionArgs);
            case SEARCH_ZIP:
                return mZipsDB.delete(selection, selectionArgs);
            default: throw new SQLException("Failed to insert row into " + uri);
        }
    }

    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Uri returnUri;
        switch (mUriMatcher.match(uri)){
            case SEARCH_RECIPE:
                returnUri = mRecipesDB.insert(values);
                break;
            case SEARCH_ZIP:
                returnUri = mZipsDB.insert(values);
                break;
            default: throw new SQLException("Failed to insert row into " + uri);
        }
        return returnUri;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int index;
        switch (mUriMatcher.match(uri)){
            case SEARCH_RECIPE:
                index = mRecipesDB.updateFavorite(values, selection, selectionArgs);
                break;
            case SEARCH_ZIP:
                index = mZipsDB.updateState(values, selection, selectionArgs);
                break;
            default: throw new SQLException("Failed to insert row into " + uri);
        }
        return index;

    }
}