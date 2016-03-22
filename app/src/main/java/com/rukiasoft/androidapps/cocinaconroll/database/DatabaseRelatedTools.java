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

     public DatabaseRelatedTools(){

    }

    public void addRecipeToArrayAndDatabase(Context mContext, List<RecipeItem> recipeItemList, RecipeItem recipeItem){
        recipeItemList.add(recipeItem);
        insertRecipeIntoDatabase(mContext, recipeItem, true);
    }

    public void updateFavoriteById(Context mContext, int id, boolean favorite) {
        ContentValues values = new ContentValues();
        int iFavorite = favorite? 1 : 0;
        values.put(RecipesTable.FIELD_FAVORITE, iFavorite);
        String clause = RecipesTable.FIELD_ID + " = ? ";

        String[] args = {String.valueOf(id)};
        mContext.getContentResolver().update(CocinaConRollContentProvider.CONTENT_URI_RECIPES, values, clause, args);
    }

    public void updateFavoriteByFileName(Context mContext, String name, boolean favorite) {
        ContentValues values = new ContentValues();
        int iFavorite = favorite? 1 : 0;
        values.put(RecipesTable.FIELD_FAVORITE, iFavorite);
        String clause = RecipesTable.FIELD_PATH_RECIPE + " like ? ";

        //String[] args = {getNormalizedString(name)};
        String[] args = {"%" + name};
        mContext.getContentResolver().update(CocinaConRollContentProvider.CONTENT_URI_RECIPES, values, clause, args);
    }

    public void updateStateById(Context mContext, int id, Integer state) {
        ContentValues values = new ContentValues();
        values.put(RecipesTable.FIELD_STATE, state);
        String clause = RecipesTable.FIELD_ID + " = ? ";

        String[] args = {String.valueOf(id)};
        mContext.getContentResolver().update(CocinaConRollContentProvider.CONTENT_URI_RECIPES, values, clause, args);
    }


    public void updatePathsAndVersion(Context mContext, RecipeItem recipe) {
        ContentValues values = new ContentValues();

        values.put(RecipesTable.FIELD_PATH_RECIPE_EDITED, recipe.getPathRecipe());
        if((recipe.getState()&Constants.FLAG_EDITED_PICTURE) != 0){
            values.put(RecipesTable.FIELD_PATH_PICTURE_EDITED, recipe.getPathPicture());
        }
        values.put(RecipesTable.FIELD_STATE, recipe.getState());
        values.put(RecipesTable.FIELD_VERSION, recipe.getVersion());
        values.put(RecipesTable.FIELD_VEGETARIAN, recipe.getVegetarian()?1:0);
        String clause = RecipesTable.FIELD_ID + " = ? ";

        String[] args = {String.valueOf(recipe.get_id())};
        mContext.getContentResolver().update(CocinaConRollContentProvider.CONTENT_URI_RECIPES, values, clause, args);
    }

    public void insertRecipeIntoDatabase(Context mContext, RecipeItem recipeItem, boolean update) {
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
        values.put(RecipesTable.FIELD_VEGETARIAN, recipeItem.getVegetarian()?1:0);
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
        RecipeItem coincidence = getRecipeByPathName(mContext, recipeItem.getPathRecipe());
        if(coincidence == null) {
            mContext.getContentResolver().insert(CocinaConRollContentProvider.CONTENT_URI_RECIPES, values);
        }else if(update){
            String selection = RecipesTable.FIELD_ID + " = ? ";
            String[] selectionArgs = {coincidence.get_id().toString()};
            mContext.getContentResolver().update(CocinaConRollContentProvider.CONTENT_URI_RECIPES, values, selection, selectionArgs);
        }
    }

    public void removeRecipefromDatabase(Context mContext, int id) {
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


    public List<RecipeItem> searchRecipesInDatabase(Context mContext) {
        String[] sSelectionArgs = new String[1];
        return searchRecipesInDatabase(mContext, null, sSelectionArgs);
    }

    public List<RecipeItem> searchRecipesInDatabase(Context mContext, String field, int selectionArgs) {
        String[] sSelectionArgs = new String[1];
        sSelectionArgs[0] = String.valueOf(selectionArgs);
        return searchRecipesInDatabase(mContext, field, sSelectionArgs);
    }

    public List<RecipeItem> searchRecipesInDatabaseByState(Context mContext, int state){
        List<RecipeItem> list = searchRecipesInDatabase(mContext);
        List<RecipeItem> listFiltered = new ArrayList<>();
        for(RecipeItem recipeItem : list){
            if((recipeItem.getState() & state) != 0){
                listFiltered.add(recipeItem);
            }
        }
        return listFiltered;
    }

    public List<RecipeItem> searchRecipesInDatabase(Context mContext, String field, long selectionArgs) {
        String[] sSelectionArgs = new String[1];
        sSelectionArgs[0] = String.valueOf(selectionArgs);
        return searchRecipesInDatabase(mContext, field, sSelectionArgs);
    }

    public List<RecipeItem> searchRecipesInDatabase(Context mContext, String field, String selectionArgs){
        String[] sSelectionArgs = new String[1];
        sSelectionArgs[0] = selectionArgs;
        return searchRecipesInDatabase(mContext, field, sSelectionArgs);
    }

    private List<RecipeItem> searchRecipesInDatabase(Context mContext, String field, String[] selectionArgs){
        if(selectionArgs[0] == null || selectionArgs[0].isEmpty()){
            selectionArgs = null;
        }
        final String[] projection = RecipesTable.ALL_COLUMNS;
        String selection = null;
        String sortOrder = RecipesTable.FIELD_NAME_NORMALIZED + " asc ";
        if(field != null) {
            if (field.equals(RecipesTable.FIELD_DATE)) {
                selection = field + " > ? ";
            } else{
                selection = field + " = ? ";
            }
        }
        Cursor cursor = mContext.getContentResolver().query(CocinaConRollContentProvider.CONTENT_URI_RECIPES,
                projection,
                selection,
                selectionArgs, sortOrder);

        return getRecipesFromCursor(cursor);
    }

    public List<RecipeItem> getRecipesByState(Context mContext, Integer flag){
        List<RecipeItem> recipes = searchRecipesInDatabase(mContext);
        List<RecipeItem> coincidences = new ArrayList<>();
        for(RecipeItem recipe : recipes){
            if((recipe.getState() & flag) !=0){
                coincidences.add(recipe);
            }
        }
        return coincidences;
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

    public RecipeItem getRecipeByPathName(Context mContext, String path){
        Uri uPath = Uri.parse(path);
        return getRecipeByFileName(mContext, uPath.getLastPathSegment());
    }

    public RecipeItem getRecipeByFileName(Context mContext, String name){
        String[] sSelectionArgs = new String[2];
        sSelectionArgs[0] = "%" + name;
        sSelectionArgs[1] = "%" + name;

        final String[] projection = RecipesTable.ALL_COLUMNS;
        String sortOrder = RecipesTable.FIELD_NAME_NORMALIZED + " asc ";
        String field1 = RecipesTable.FIELD_PATH_RECIPE;
        String field2 = RecipesTable.FIELD_PATH_RECIPE_EDITED;
        String selection = field1 + " like ? OR " + field2 + " like ?";

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

    public Uri insertNewZip(Context mContext, String name, String link) {
        ContentValues values = new ContentValues();
        values.put(ZipsTable.FIELD_NAME, name);
        values.put(ZipsTable.FIELD_LINK, link);
        values.put(ZipsTable.FIELD_STATE, Constants.STATE_NOT_DOWNLOADED);
        return mContext.getContentResolver().insert(CocinaConRollContentProvider.CONTENT_URI_ZIPS, values);
    }

    public List<ZipItem> getZipsByState(Context mContext, Integer state) {
        String selection = ZipsTable.FIELD_STATE + " = ? ";
        String sState;
        try {
            sState = String.valueOf(state);
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
        final String[] selectionArgs = {sState};
        return getZips(mContext, selection, selectionArgs);
    }

    public List<ZipItem> getAllZips(Context mContext) {
        return getZips(mContext, null, null);
    }

    public List<ZipItem> getZips(Context mContext, String selection, String[] selectionArgs) {
        final String[] projection = ZipsTable.ALL_COLUMNS;

        Cursor cursor = mContext.getContentResolver().query(CocinaConRollContentProvider.CONTENT_URI_ZIPS,
                projection,
                selection,
                selectionArgs, null);

        return getZipsFromCursor(cursor);
    }

    public void removeZipfromDatabase(Context mContext, int id) {
        String selection = RecipesTable.FIELD_ID + " = ? ";
        String[] selectionArgs = {String.valueOf(id)};
        mContext.getContentResolver().delete(CocinaConRollContentProvider.CONTENT_URI_ZIPS, selection, selectionArgs);

    }

    public void updateZipState(Context mContext, String name, Integer state) {
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
    
    private List<ZipItem> getZipsFromCursor(Cursor cursor){
        List<ZipItem> list = new ArrayList<>(); 
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ZipItem zipToDownload = new ZipItem();
                zipToDownload.setName(cursor.getString(cursor.getColumnIndexOrThrow(ZipsTable.FIELD_NAME)));
                zipToDownload.setLink(cursor.getString(cursor.getColumnIndexOrThrow(ZipsTable.FIELD_LINK)));
                zipToDownload.setId(cursor.getInt(cursor.getColumnIndexOrThrow(ZipsTable.FIELD_ID)));
                zipToDownload.setState(cursor.getInt(cursor.getColumnIndexOrThrow(ZipsTable.FIELD_STATE)));
                list.add(zipToDownload);
            }while(cursor.moveToNext());
            cursor.close();
        }
        return list;
    }


}
