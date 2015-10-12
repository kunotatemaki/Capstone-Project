package com.rukiasoft.androidapps.cocinaconroll.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import com.rukiasoft.androidapps.cocinaconroll.classes.ZipToDownload;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeItem;


import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;


public class DatabaseRelatedTools {

    final Context mContext;
    public DatabaseRelatedTools(Context context){
        mContext = context;
    }

    public void addRecipeToArrayAndSuggestions(List<RecipeItem> recipeItemList, RecipeItem recipeItem){
        recipeItemList.add(recipeItem);
        insertRecipeIntoSuggestions(recipeItem);
    }

    public void removeRecipeFromArrayAndSuggestions(List<RecipeItem> recipeItemList, int index){
        String name = recipeItemList.get(index).getName();
        recipeItemList.remove(index);
        removeRecipefromSuggestions(name);
    }

    public void updateFavorite(String name, boolean favorite) {
        ContentValues values = new ContentValues();
        values.put(RecipesTable.FIELD_NAME, name);
        int iFavorite = favorite? 1 : 0;
        values.put(RecipesTable.FIELD_FAVORITE, iFavorite);
        String clause = RecipesTable.FIELD_NAME_NORMALIZED + " = ? ";

        String[] args = {getNormalizedString(values.get(RecipesTable.FIELD_NAME).toString())};
        mContext.getContentResolver().update(CocinaConRollContentProvider.CONTENT_URI_RECIPES, values, clause, args);
    }

    public void insertRecipeIntoSuggestions(RecipeItem recipeItem) {
        ContentValues values = new ContentValues();
        values.put(RecipesTable.FIELD_NAME, recipeItem.getName());
        values.put(RecipesTable.FIELD_NAME_NORMALIZED, getNormalizedString(recipeItem.getName()));
        values.put(RecipesTable.FIELD_TYPE, recipeItem.getType());
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
        values.put(RecipesTable.FIELD_PATH_RECIPE, recipeItem.getFilePath());
        values.put(RecipesTable.FIELD_PATH_PICTURE, recipeItem.getPicturePath());
        mContext.getContentResolver().insert(CocinaConRollContentProvider.CONTENT_URI_RECIPES, values);

    }

    private void removeRecipefromSuggestions(String name) {
        String nName = getNormalizedString(name);
        String selection = RecipesTable.FIELD_NAME_NORMALIZED + " = ? ";
        final String[] selectionArgs = {nName};
        mContext.getContentResolver().delete(CocinaConRollContentProvider.CONTENT_URI_RECIPES, selection, selectionArgs);
    }

    public boolean isFavorite(String recipeName) {
        List<RecipeDatabaseItem> list = searchRecipesInDatabaseByName(recipeName, true);
        return list.size() == 1 && list.get(0).getFavorite() == 1;
    }

    public List<RecipeDatabaseItem> searchRecipesInDatabaseByName(String name, boolean match){
        final String[] projection = {RecipesTable.FIELD_NAME, RecipesTable.FIELD_FAVORITE};
        List<RecipeDatabaseItem> list = new ArrayList<>();
        String selection;
        name = getNormalizedString(name);
        if(match){
            selection = RecipesTable.FIELD_NAME_NORMALIZED + " = ? ";
        }else{
            selection = RecipesTable.FIELD_NAME_NORMALIZED + " like ? ";
            name = "%" + name + "%";
        }
        final String[] selectionArgs = {name};
        Cursor cursor = mContext.getContentResolver().query(CocinaConRollContentProvider.CONTENT_URI_RECIPES,
                projection,
                selection,
                selectionArgs, null);

        //TODO arreglar
        if (cursor != null && cursor.moveToFirst()) {
            do {
                RecipeDatabaseItem recipeInfoDataBase = new RecipeDatabaseItem();
                recipeInfoDataBase.setName(cursor.getString(0));
                recipeInfoDataBase.setFavorite(cursor.getInt(1));

                list.add(recipeInfoDataBase);
            }while(cursor.moveToNext());
            cursor.close();
        }

        return list;
    }

    public List<RecipeDatabaseItem> searchRecipesInDatabaseByType(String type){
        List<RecipeDatabaseItem> list = new ArrayList<>();
        String selection;
        selection = RecipesTable.FIELD_TYPE + " = ? ";
        final String[] selectionArgs = {type};
        Cursor cursor = mContext.getContentResolver().query(CocinaConRollContentProvider.CONTENT_URI_RECIPES,
                RecipesTable.ALL_COLUMNS,
                selection,
                selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                RecipeDatabaseItem item =  new RecipeDatabaseItem();
                item.setName(cursor.getString(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_NAME)));
                item.set_id(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_ID)));
                item.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_FAVORITE)));
                item.setOwn(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_STATE)));
                item.setVegetarian(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_VEGETARIAN)));
                list.add(item);
            }while(cursor.moveToNext());
            cursor.close();
        }

        return list;
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

    public Uri insertNewZip(String name, String link) {
        ContentValues values = new ContentValues();
        values.put(ZipsTable.FIELD_NAME, name);
        values.put(ZipsTable.FIELD_LINK, link);
        values.put(ZipsTable.FIELD_STATE, Constants.STATE_NOT_DOWNLOADED);
        return mContext.getContentResolver().insert(CocinaConRollContentProvider.CONTENT_URI_ZIPS, values);
    }

    public List<ZipToDownload> getZipsByState(Integer state) {
        final String[] projection = {ZipsTable.FIELD_NAME, ZipsTable.FIELD_LINK};
        List<ZipToDownload> list = new ArrayList<>();
        String selection;
        selection = ZipsTable.FIELD_STATE + " = ? ";
        String sState;
        try {
            sState = String.valueOf(state);
        }catch (NumberFormatException e){
            return list;
        }
        final String[] selectionArgs = {sState};
        Cursor cursor = mContext.getContentResolver().query(CocinaConRollContentProvider.CONTENT_URI_ZIPS,
                projection,
                selection,
                selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                ZipToDownload zipToDownload = new ZipToDownload();
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

    public List<RecipeDatabaseItem> getRecipesFromCursor(Cursor cursor) {
        List<RecipeDatabaseItem> list = new ArrayList<>();
        if(cursor != null && cursor.moveToFirst()){
            do {
                RecipeDatabaseItem item =  new RecipeDatabaseItem();
                item.set_id(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_ID)));
                item.setName(cursor.getString(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_NAME)));
                item.setName(cursor.getString(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_NAME)));
                item.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_FAVORITE)));
                item.setOwn(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_STATE)));
                item.setVegetarian(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_VEGETARIAN)));
                item.setName(cursor.getString(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_NAME)));
                item.set_id(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_ID)));
                item.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_FAVORITE)));
                item.setOwn(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_STATE)));
                item.setVegetarian(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_VEGETARIAN)));
                list.add(item);
            }while(cursor.moveToNext());
            cursor.close();
        }

        return list;
    }
}
