package com.rukiasoft.androidapps.cocinaconroll.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.Toast;

import com.rukiasoft.androidapps.cocinaconroll.Constants;
import com.rukiasoft.androidapps.cocinaconroll.ToolbarAndRefreshActivity;
import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeItem;

/**
 * Created by Ruler on 21/09/2015 for the Udacity Nanodegree.
 */
public class Tools {

    public Tools(){
    }

    public Boolean isInTimeframe(RecipeItem recipeItem){
        try {
            Integer seconds = Constants.TIMEFRAME_NEW_RECIPE_SECONDS_DAY * Constants.TIMEFRAME_NEW_RECIPE_DAYS;
            Long timeframe = System.currentTimeMillis() - seconds;
            return recipeItem.getDate() != null && recipeItem.getDate() != -1 && recipeItem.getDate() > timeframe;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     *
     * @param context context of the application
     * @return true if has vibrator, false otherwise
     */
    @SuppressLint("NewApi")
    public Boolean hasVibrator(Context context) {

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            return true;
        else{
            String vs = Context.VIBRATOR_SERVICE;
            Vibrator mVibrator = (Vibrator) context.getSystemService(vs);
            return mVibrator.hasVibrator();
        }
    }

    /**
     * set the refresh layout to be shown in the activity
     * @param activity activity having refresh layout
     * @param refreshLayout refresh layout
     */
    public static void setRefreshLayout(Activity activity, SwipeRefreshLayout refreshLayout){
        if(activity instanceof ToolbarAndRefreshActivity) {
            ((ToolbarAndRefreshActivity) activity).setRefreshLayout(refreshLayout);
            ((ToolbarAndRefreshActivity) activity).disableRefreshLayoutSwipe();
        }
    }



    /**
     * set the refresh layout to be shown in the activity
     * @param activity activity having refresh layout
     * @param refreshLayout refresh layout
     */
    public void showRefreshLayout(Activity activity, SwipeRefreshLayout refreshLayout){
        if(activity instanceof ToolbarAndRefreshActivity) {
            ((ToolbarAndRefreshActivity) activity).showRefreshLayoutSwipeProgress();
        }
    }

    /**
    * set the refresh layout to be hidden in the activity
    * @param activity activity having refresh layout
    * @param refreshLayout refresh layout
    */
    public void hideRefreshLayout(Activity activity, SwipeRefreshLayout refreshLayout){
        if(activity instanceof ToolbarAndRefreshActivity) {
            ((ToolbarAndRefreshActivity) activity).hideRefreshLayoutSwipeProgress();
        }
    }
}
