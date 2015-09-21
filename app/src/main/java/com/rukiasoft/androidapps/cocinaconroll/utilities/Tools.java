package com.rukiasoft.androidapps.cocinaconroll.utilities;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.rukiasoft.androidapps.cocinaconroll.Constants;
import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeItem;

/**
 * Created by Ruler on 21/09/2015 for the Udacity Nanodegree.
 */
public class Tools {
    static private Toast toast = null;

    public static void showToast(final Context context, final String text) {

        //sólo quiero mostrar si viene de una activity
        if (!(context instanceof Activity)) {
            //Log.d(TAG, "no venía de una activity");
            return;
        }
        try {
            if (toast != null) {
                if (toast.getView().isShown()) {
                    toast.setText(text);
                    return;
                }
            }
            toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
            //Log.e(TAG, "error en showToast: " + e.getMessage());
        }
    }

    public static Boolean isInTimeframe(RecipeItem recipeItem){
        try {
            Integer seconds = Constants.TIMEFRAME_NEW_RECIPE_SECONDS_DAY * Constants.TIMEFRAME_NEW_RECIPE_DAYS;
            Long timeframe = System.currentTimeMillis() - seconds;
            return recipeItem.getDate() != null && recipeItem.getDate() != -1 && recipeItem.getDate() > timeframe;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }

    }
}
