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
        values.put(SuggestionsTable.FIELD_NAME, name);
        int iFavorite = favorite? 1 : 0;
        values.put(SuggestionsTable.FIELD_NAME_FAVORITE, iFavorite);
        String clause = SuggestionsTable.FIELD_NAME_NORMALIZED + " = ? ";

        String[] args = {getNormalizedString(values.get(SuggestionsTable.FIELD_NAME).toString())};
        mContext.getContentResolver().update(CocinaConRollContentProvider.CONTENT_URI_SUGGESTIONS, values, clause, args);
    }

    private void insertRecipeIntoSuggestions(RecipeItem recipeItem) {
        ContentValues values = new ContentValues();
        values.put(SuggestionsTable.FIELD_NAME, recipeItem.getName());
        values.put(SuggestionsTable.FIELD_NAME_NORMALIZED, getNormalizedString(recipeItem.getName()));
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
        values.put(SuggestionsTable.FIELD_ICON, icon);
        mContext.getContentResolver().insert(CocinaConRollContentProvider.CONTENT_URI_SUGGESTIONS, values);
    }

    private void removeRecipefromSuggestions(String name) {
        String nName = getNormalizedString(name);
        String selection = SuggestionsTable.FIELD_NAME_NORMALIZED + " = ? ";
        final String[] selectionArgs = {nName};
        mContext.getContentResolver().delete(CocinaConRollContentProvider.CONTENT_URI_SUGGESTIONS, selection, selectionArgs);
    }

    public boolean isFavorite(String recipeName) {
        List<SuggestionsItem> list = getRecipeInfoInDatabase(recipeName, true);
        return list.size() == 1 && list.get(0).isFavorite();
    }

    public List<SuggestionsItem> getRecipeInfoInDatabase(String name, boolean match){
        final String[] projection = {SuggestionsTable.FIELD_NAME, SuggestionsTable.FIELD_NAME_FAVORITE};
        List<SuggestionsItem> list = new ArrayList<>();
        String selection;
        name = getNormalizedString(name);
        if(match){
            selection = SuggestionsTable.FIELD_NAME_NORMALIZED + " = ? ";
        }else{
            selection = SuggestionsTable.FIELD_NAME_NORMALIZED + " like ? ";
            name = "%" + name + "%";
        }
        final String[] selectionArgs = {name};
        Cursor cursor = mContext.getContentResolver().query(CocinaConRollContentProvider.CONTENT_URI_SUGGESTIONS,
                projection,
                selection,
                selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                SuggestionsItem recipeInfoDataBase = new SuggestionsItem();
                recipeInfoDataBase.setName(cursor.getString(0));
                int fav = cursor.getInt(1);
                if(fav > 0){
                    recipeInfoDataBase.setFavorite(true);
                }else{
                    recipeInfoDataBase.setFavorite(false);
                }
                list.add(recipeInfoDataBase);
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
}
