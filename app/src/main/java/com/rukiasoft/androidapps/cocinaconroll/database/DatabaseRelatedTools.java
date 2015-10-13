package com.rukiasoft.androidapps.cocinaconroll.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import com.rukiasoft.androidapps.cocinaconroll.classes.ZipToDownload;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.classes.RecipeItem;


import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;


public class DatabaseRelatedTools {

    final Context mContext;
    public DatabaseRelatedTools(Context context){
        mContext = context;
    }

    public void addRecipeToArrayAndDatabase(List<RecipeItem> recipeItemList, RecipeItem recipeItem){
        recipeItemList.add(recipeItem);
        insertRecipeIntoDatabase(recipeItem);
    }

    public void updateFavorite(int id, boolean favorite) {
        ContentValues values = new ContentValues();
        int iFavorite = favorite? 1 : 0;
        values.put(RecipesTable.FIELD_FAVORITE, iFavorite);
        String clause = RecipesTable.FIELD_ID + " = ? ";

        String[] args = {String.valueOf(id)};
        mContext.getContentResolver().update(CocinaConRollContentProvider.CONTENT_URI_RECIPES, values, clause, args);
    }

    public void updatePaths(RecipeItem recipe) {
        ContentValues values = new ContentValues();

        values.put(RecipesTable.FIELD_PATH_RECIPE_EDITED, recipe.getPathRecipe());
        if((recipe.getState()&Constants.FLAG_EDITED_PICTURE) != 0){
            values.put(RecipesTable.FIELD_PATH_PICTURE_EDITED, recipe.getPathPicture());
        }
        values.put(RecipesTable.FIELD_STATE, recipe.getState());
        String clause = RecipesTable.FIELD_ID + " = ? ";

        String[] args = {String.valueOf(recipe.get_id())};
        mContext.getContentResolver().update(CocinaConRollContentProvider.CONTENT_URI_RECIPES, values, clause, args);
    }

    public void insertRecipeIntoDatabase(RecipeItem recipeItem) {
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
        values.put(RecipesTable.FIELD_PATH_RECIPE, recipeItem.getPathRecipe());
        values.put(RecipesTable.FIELD_PATH_PICTURE, recipeItem.getPathPicture());
        mContext.getContentResolver().insert(CocinaConRollContentProvider.CONTENT_URI_RECIPES, values);

    }

    public void removeRecipefromDatabase(int id) {
        //// TODO: 13/10/15 no eliminar si no es creada. dejar en estado original
        Uri.Builder uribuilder = ContentUris.appendId(CocinaConRollContentProvider.CONTENT_URI_RECIPES.buildUpon(), id);
        Cursor cursor = mContext.getContentResolver().query(uribuilder.build(),
                null,
                null,
                null, null);
        List<RecipeItem> list = getRecipesFromCursor(cursor);
        if(list.size() > 0){
            RecipeItem item = list.get(0);
            int state = item.getState();
            state = (state&(~Constants.FLAG_EDITED_PICTURE));
            state = (state&(~Constants.FLAG_EDITED));
            state = (state&(~Constants.FLAG_OWN));
        }
        /*String nName = getNormalizedString(name);
        String selection = RecipesTable.FIELD_NAME_NORMALIZED + " = ? ";
        final String[] selectionArgs = {nName};
        mContext.getContentResolver().delete(CocinaConRollContentProvider.CONTENT_URI_RECIPES, selection, selectionArgs);*/
    }


    /*public List<RecipeItem> searchRecipesInDatabaseByName(String name, boolean match){
        final String[] projection = {RecipesTable.FIELD_NAME, RecipesTable.FIELD_FAVORITE};
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


        return getRecipesFromCursor(cursor);
    }*/

    public List<RecipeItem> searchRecipesInDatabase(String field, String[] selectionArgs){
        final String[] projection = RecipesTable.ALL_COLUMNS;
        String selection = null;
        String sortOrder = RecipesTable.FIELD_NAME_NORMALIZED + " asc ";
        if(field != null) {
            if (field.equals(RecipesTable.FIELD_STATE)) {
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

    public List<RecipeItem> getRecipesFromCursor(Cursor cursor) {
        List<RecipeItem> list = new ArrayList<>();
        if(cursor != null && cursor.moveToFirst()){
            do {
                RecipeItem item =  new RecipeItem();
                item.set_id(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_ID)));
                item.setName(cursor.getString(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_NAME)));
                item.setIcon(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_ICON)));
                int favorite = cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_FAVORITE));
                item.setFavorite(favorite != 0);
                item.setState(cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_STATE)));
                item.setType(cursor.getString(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_TYPE)));
                int vegetarian = cursor.getInt(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_VEGETARIAN));
                item.setVegetarian(vegetarian != 0);
                if((item.getState()&Constants.FLAG_EDITED_PICTURE) != 0){
                    //picture edited
                    item.setPathPicture(cursor.getString(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_PATH_PICTURE_EDITED)));
                }else{
                    item.setPathPicture(cursor.getString(cursor.getColumnIndexOrThrow(RecipesTable.FIELD_PATH_PICTURE)));
                }
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
