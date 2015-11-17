package com.rukiasoft.androidapps.cocinaconroll.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.classes.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.classes.ZipItem;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;


public class DatabaseRelatedTools {

    private final Context mContext;
    public DatabaseRelatedTools(Context context){
        mContext = context;
    }

    public void addRecipeToArrayAndDatabase(List<RecipeItem> recipeItemList, RecipeItem recipeItem){
        recipeItemList.add(recipeItem);
        insertRecipeIntoDatabase(recipeItem, true);
    }

    public void updateFavoriteById(int id, boolean favorite) {
        ContentValues values = new ContentValues();
        int iFavorite = favorite? 1 : 0;
        values.put(RecipesTable.FIELD_FAVORITE, iFavorite);
        String clause = RecipesTable.FIELD_ID + " = ? ";

        String[] args = {String.valueOf(id)};
        mContext.getContentResolver().update(CocinaConRollContentProvider.CONTENT_URI_RECIPES, values, clause, args);
    }

    public void updateFavoriteByName(String name, boolean favorite) {
        ContentValues values = new ContentValues();
        int iFavorite = favorite? 1 : 0;
        values.put(RecipesTable.FIELD_FAVORITE, iFavorite);
        String clause = RecipesTable.FIELD_NAME_NORMALIZED + " = ? ";

        String[] args = {getNormalizedString(name)};
        mContext.getContentResolver().update(CocinaConRollContentProvider.CONTENT_URI_RECIPES, values, clause, args);
    }

    public void updatePathsAndVersion(RecipeItem recipe) {
        ContentValues values = new ContentValues();

        values.put(RecipesTable.FIELD_PATH_RECIPE_EDITED, recipe.getPathRecipe());
        if((recipe.getState()&Constants.FLAG_EDITED_PICTURE) != 0){
            values.put(RecipesTable.FIELD_PATH_PICTURE_EDITED, recipe.getPathPicture());
        }
        values.put(RecipesTable.FIELD_STATE, recipe.getState());
        values.put(RecipesTable.FIELD_VERSION, recipe.getVersion());
        String clause = RecipesTable.FIELD_ID + " = ? ";

        String[] args = {String.valueOf(recipe.get_id())};
        mContext.getContentResolver().update(CocinaConRollContentProvider.CONTENT_URI_RECIPES, values, clause, args);
    }

    public void insertRecipeIntoDatabase(RecipeItem recipeItem, boolean update) {
        //set values
        ContentValues values = new ContentValues();
        values.put(RecipesTable.FIELD_NAME, recipeItem.getName());
        values.put(RecipesTable.FIELD_NAME_NORMALIZED, getNormalizedString(recipeItem.getName()));
        values.put(RecipesTable.FIELD_TYPE, recipeItem.getType());
        values.put(RecipesTable.FIELD_VERSION, recipeItem.getVersion());
        int icon;
        switch (recipeItem.getType()) {
            case Constants.TYPE_DESSERTS:
                icon = R.drawable.ic_dessert_24;
                break;
            case Constants.TYPE_STARTERS:
                icon = R.drawable.ic_starters_24;
                break;
            case Constants.TYPE_MAIN:
                icon = R.drawable.ic_main_24;
                break;
            default:
                icon = R.drawable.ic_all_24;
                break;
        }
        values.put(RecipesTable.FIELD_ICON, icon);
        int vegetarian = 0;
        if(recipeItem.getVegetarian()){
            vegetarian = 1;
        }
        values.put(RecipesTable.FIELD_VEGETARIAN, vegetarian);
        values.put(RecipesTable.FIELD_STATE, recipeItem.getState());
        values.put(RecipesTable.FIELD_FAVORITE, 0);
        values.put(RecipesTable.FIELD_DATE, recipeItem.getDate());
        if((recipeItem.getState()&(Constants.FLAG_EDITED|Constants.FLAG_OWN))!=0) {
            values.put(RecipesTable.FIELD_PATH_RECIPE_EDITED, recipeItem.getPathRecipe());
        }else {
            values.put(RecipesTable.FIELD_PATH_RECIPE, recipeItem.getPathRecipe());
        }
        if((recipeItem.getState()&Constants.FLAG_EDITED_PICTURE)!=0) {
            values.put(RecipesTable.FIELD_PATH_PICTURE_EDITED, recipeItem.getPathPicture());
        }else {
            values.put(RecipesTable.FIELD_PATH_PICTURE, recipeItem.getPathPicture());
        }
        //check if recipe exists. If not, insert. Otherwise, update
        List<RecipeItem> coincidences = searchRecipesInDatabase(RecipesTable.FIELD_NAME_NORMALIZED, getNormalizedString(recipeItem.getName()));
        if(coincidences.size() == 0) {
            mContext.getContentResolver().insert(CocinaConRollContentProvider.CONTENT_URI_RECIPES, values);
        }else if(update){
            String selection = RecipesTable.FIELD_NAME_NORMALIZED + " = ? ";
            String[] selectionArgs = {getNormalizedString(recipeItem.getName())};
            mContext.getContentResolver().update(CocinaConRollContentProvider.CONTENT_URI_RECIPES, values, selection, selectionArgs);
        }
    }

    public void removeRecipefromDatabase(int id) {
        Uri.Builder uribuilder = ContentUris.appendId(CocinaConRollContentProvider.CONTENT_URI_RECIPES.buildUpon(), id);
        Cursor cursor = mContext.getContentResolver().query(uribuilder.build(),
                null,
                null,
                null, null);
        List<RecipeItem> list = getRecipesFromCursor(cursor);
        if(list.size() > 0){
            RecipeItem item = list.get(0);
            int state = item.getState();
            String selection = RecipesTable.FIELD_ID + " = ? ";
            String[] selectionArgs = {String.valueOf(id)};
            if((state&Constants.FLAG_OWN) != 0){
                //own recipe, delete from database
                mContext.getContentResolver().delete(CocinaConRollContentProvider.CONTENT_URI_RECIPES, selection, selectionArgs);
            }else{
                //updated recipe, reset it
                state = (state&(~Constants.FLAG_EDITED_PICTURE));
                state = (state&(~Constants.FLAG_EDITED));
                ContentValues values = new ContentValues();
                values.put(RecipesTable.FIELD_STATE, state);
                values.put(RecipesTable.FIELD_PATH_PICTURE_EDITED, "");
                values.put(RecipesTable.FIELD_PATH_RECIPE_EDITED, "");
                mContext.getContentResolver().update(CocinaConRollContentProvider.CONTENT_URI_RECIPES, values, selection, selectionArgs);
            }
            
        }
    }


    public List<RecipeItem> searchRecipesInDatabase() {
        String[] sSelectionArgs = new String[1];
        return searchRecipesInDatabase(null, sSelectionArgs);
    }

    public List<RecipeItem> searchRecipesInDatabase(String field, int selectionArgs) {
        String[] sSelectionArgs = new String[1];
        sSelectionArgs[0] = String.valueOf(selectionArgs);
        return searchRecipesInDatabase(field, sSelectionArgs);
    }

    public List<RecipeItem> searchRecipesInDatabase(String field, long selectionArgs) {
        String[] sSelectionArgs = new String[1];
        sSelectionArgs[0] = String.valueOf(selectionArgs);
        return searchRecipesInDatabase(field, sSelectionArgs);
    }

    public List<RecipeItem> searchRecipesInDatabase(String field, String selectionArgs){
        String[] sSelectionArgs = new String[1];
        sSelectionArgs[0] = selectionArgs;
        return searchRecipesInDatabase(field, sSelectionArgs);
    }

    private List<RecipeItem> searchRecipesInDatabase(String field, String[] selectionArgs){
        if(selectionArgs[0] == null || selectionArgs[0].isEmpty()){
            selectionArgs = null;
        }
        final String[] projection = RecipesTable.ALL_COLUMNS;
        String selection = null;
        String sortOrder = RecipesTable.FIELD_NAME_NORMALIZED + " asc ";
        if(field != null) {
            if (field.equals(RecipesTable.FIELD_STATE) || field.equals(RecipesTable.FIELD_DATE)) {
                selection = field + " > ? ";
            } else {
                selection = field + " = ? ";
            }
        }
        Cursor cursor = mContext.getContentResolver().query(CocinaConRollContentProvider.CONTENT_URI_RECIPES,
                projection,
                selection,
                selectionArgs, sortOrder);

        return getRecipesFromCursor(cursor);
    }

    public String getNormalizedString(String input){
        String normalized;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
            input = normalized.replaceAll("[^\\p{ASCII}]", "");
        }
        input = input.trim();
        return input.toLowerCase();

    }

    public RecipeItem getRecipeByFileName(String name){
        String[] sSelectionArgs = new String[1];
        sSelectionArgs[0] = "%" + name;

        final String[] projection = RecipesTable.ALL_COLUMNS;
        String sortOrder = RecipesTable.FIELD_NAME_NORMALIZED + " asc ";
        String field = RecipesTable.FIELD_PATH_RECIPE;
        String selection = field + " like ? ";

        Cursor cursor = mContext.getContentResolver().query(CocinaConRollContentProvider.CONTENT_URI_RECIPES,
                projection,
                selection,
                sSelectionArgs, sortOrder);

        List<RecipeItem> list = getRecipesFromCursor(cursor);
        if(list.size()>0){
            return list.get(0);
        }else{
            return null;
        }
    }

    public Uri insertNewZip(String name, String link) {
        ContentValues values = new ContentValues();
        values.put(ZipsTable.FIELD_NAME, name);
        values.put(ZipsTable.FIELD_LINK, link);
        values.put(ZipsTable.FIELD_STATE, Constants.STATE_NOT_DOWNLOADED);
        return mContext.getContentResolver().insert(CocinaConRollContentProvider.CONTENT_URI_ZIPS, values);
    }

    public List<ZipItem> getZipsByState(Integer state) {
        String selection = ZipsTable.FIELD_STATE + " = ? ";
        String sState;
        try {
            sState = String.valueOf(state);
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
        final String[] selectionArgs = {sState};
        return getZips(selection, selectionArgs);
    }

    public List<ZipItem> getZips(String selection, String[] selectionArgs) {
        final String[] projection = {ZipsTable.FIELD_NAME, ZipsTable.FIELD_LINK};
        List<ZipItem> list = new ArrayList<>();

        Cursor cursor = mContext.getContentResolver().query(CocinaConRollContentProvider.CONTENT_URI_ZIPS,
                projection,
                selection,
                selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                ZipItem zipToDownload = new ZipItem();
                zipToDownload.setName(cursor.getString(0));
                zipToDownload.setLink(cursor.getString(1));
                list.add(zipToDownload);
            }while(cursor.moveToNext());
            cursor.close();
        }

        return list;
    }

    public void updateZipState(String name, Integer state) {
        ContentValues values = new ContentValues();
        values.put(ZipsTable.FIELD_STATE, state);
        String clause = ZipsTable.FIELD_NAME + " = ? ";
        String[] args = {name};
        mContext.getContentResolver().update(CocinaConRollContentProvider.CONTENT_URI_ZIPS, values, clause, args);
    }

    public List<RecipeItem> getRecipesFromCursor(Cursor cursor) {
        List<RecipeItem> list = new ArrayList<>();
        if(cursor != null && cursor.moveToFirst()){
            do {
                RecipeItem item =  new RecipeItem();
                item.set_id(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_ID)));
                item.setName(cursor.getString(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_NAME)));
                item.setIcon(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_ICON)));
                int favorite = cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_FAVORITE));
                item.setFavourite(favorite != 0);
                item.setState(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_STATE)));
                item.setType(cursor.getString(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_TYPE)));
                int vegetarian = cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_VEGETARIAN));
                item.setVegetarian(vegetarian != 0);
                item.setDate(cursor.getLong(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_DATE)));
                if((item.getState()&Constants.FLAG_EDITED_PICTURE) != 0){
                    //picture edited
                    item.setPathPicture(cursor.getString(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_PATH_PICTURE_EDITED)));
                }else{
                    item.setPathPicture(cursor.getString(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_PATH_PICTURE)));
                }
                item.setVersion(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_VERSION)));
                Uri uri = Uri.parse(item.getPathPicture());
                String recipePictureName = uri.getLastPathSegment();
                item.setPicture(recipePictureName);

                if((item.getState()&(Constants.FLAG_EDITED|Constants.FLAG_OWN)) != 0){
                    //recipe edited
                    item.setPathRecipe(cursor.getString(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_PATH_RECIPE_EDITED)));
                }else{
                    item.setPathRecipe(cursor.getString(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_PATH_RECIPE)));
                }

                list.add(item);
            }while(cursor.moveToNext());
            cursor.close();
        }

        return list;
    }



}
