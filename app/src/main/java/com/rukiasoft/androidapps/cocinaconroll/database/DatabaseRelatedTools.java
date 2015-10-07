package com.rukiasoft.androidapps.cocinaconroll.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;

import com.rukiasoft.androidapps.cocinaconroll.Constants;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeItem;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;


public class DatabaseRelatedTools {

    Context mContext;
    public DatabaseRelatedTools(Context context){
        mContext = context;
    }

    //todo - comprobar que las descargadas y las originales también entran aquí
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
        List<RecipeInfoDataBase> list = getRecipeInfoInDatabase(recipeName, true);
        return list.size() == 1 && list.get(0).isFavorite();
    }

    public List<RecipeInfoDataBase> getRecipeInfoInDatabase(String name, boolean match){
        final String[] projection = {SuggestionsTable.FIELD_NAME, SuggestionsTable.FIELD_NAME_FAVORITE};
        List<RecipeInfoDataBase> list = new ArrayList<>();
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
                selection ,
                selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                RecipeInfoDataBase recipeInfoDataBase = new RecipeInfoDataBase();
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



}
