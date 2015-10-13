package com.rukiasoft.androidapps.cocinaconroll.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.rukiasoft.androidapps.cocinaconroll.ui.ToolbarAndRefreshActivity;
import com.rukiasoft.androidapps.cocinaconroll.classes.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.wifi.WifiHandler;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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
    public void setRefreshLayout(Activity activity, SwipeRefreshLayout refreshLayout){
        if(activity instanceof ToolbarAndRefreshActivity) {
            ((ToolbarAndRefreshActivity) activity).setRefreshLayout(refreshLayout);
            ((ToolbarAndRefreshActivity) activity).disableRefreshLayoutSwipe();
        }
    }



    /**
     * set the refresh layout to be shown in the activity
     * @param activity activity having refresh layout
     */
    public void showRefreshLayout(Activity activity){
        if(activity instanceof ToolbarAndRefreshActivity) {
            ((ToolbarAndRefreshActivity) activity).showRefreshLayoutSwipeProgress();
        }
    }

    /**
    * set the refresh layout to be hidden in the activity
    * @param activity activity having refresh layout
    */
    public void hideRefreshLayout(Activity activity){
        if(activity instanceof ToolbarAndRefreshActivity) {
            ((ToolbarAndRefreshActivity) activity).hideRefreshLayoutSwipeProgress();
        }
    }

    /**
     * get the application name
     */
    public String getApplicationName(Context context) {
        int stringId = context.getApplicationInfo().labelRes;
        return context.getString(stringId);
    }

    public void setScreenOnIfSettingsAllowed(Activity activity, Boolean state){
        if(state && getBooleanFromPreferences(activity, "option_screen_on"))
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public Boolean getBooleanFromPreferences(Context context, String name) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(name, false);
    }


    public boolean isPackageInstalled(String packagename, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Integer getIntegerFromPreferences(Context context, String name) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(name, Integer.MIN_VALUE);

    }

    public void savePreferences(Context context, String name, String value) {

        //SharedPreferences preferences = context.getSharedPreferences("sacarino",Context.MODE_PRIVATE);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = preferences.edit();
        ed.putString(name, value);
        ed.apply();

    }

    public void savePreferences(Context context, String name, int value) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = preferences.edit();
        ed.putInt(name, value);
        ed.apply();

    }

    public void savePreferences(Context context, String name, long value) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = preferences.edit();
        ed.putLong(name, value);
        ed.apply();

    }

    public void savePreferences(Context context, String name, Double value) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = preferences.edit();
        ed.putFloat(name, value.floatValue());
        ed.apply();

    }

    public void savePreferences(Context context, String name, Boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = preferences.edit();
        ed.putBoolean(name, value);
        ed.apply();

    }

    public String getCurrentDate(Context context) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(Constants.FORMAT_DATE_TIME,
                context.getResources().getConfiguration().locale);
        return df.format(c.getTime());
    }

    public void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public boolean hasPermissionForDownloading(Context context) {
       Boolean downloadWithWifi = getBooleanFromPreferences(context, "option_update_wifi");
            return !(downloadWithWifi && !WifiHandler.IsWifiConnected(context));
    }

    public String getStringFromPreferences(Context context, String name) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(name, "");

    }

}
