package com.rukiasoft.androidapps.cocinaconroll.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.WindowManager;

import com.rukiasoft.androidapps.cocinaconroll.Constants;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.ToolbarAndRefreshActivity;
import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeItem;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;

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

    public String getNormalizedString(String input){
        String normalized;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
            input = normalized.replaceAll("[^\\p{ASCII}]", "");
        }
        return input.toLowerCase();

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

    public void share(final Activity activity, RecipeItem recipe)
    {
        //TODO try with revealAction
        //need to "send multiple" to get more than one attachment
        Boolean installed = isPackageInstalled("com.google.android.gm", activity);
        final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("message/rfc822");
        if(installed)
            emailIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");

        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                new String[]{Constants.EMAIL});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, recipe.getName());
        String sender = String.format(activity.getResources().getString(R.string.sender), recipe.getAuthor());
        emailIntent.putExtra(Intent.EXTRA_TEXT, sender);
        //has to be an ArrayList
        ArrayList<Uri> uris = new ArrayList<>();
        //convert from paths to Android friendly Parcelable Uri's
        ReadWriteTools readWriteTools = new ReadWriteTools(activity);
        File fileXml = new File(readWriteTools.getEditedStorageDir() + recipe.getFileName());
        Uri u = Uri.fromFile(fileXml);
        uris.add(u);
        if(recipe.getPath().compareTo(Constants.DEFAULT_PICTURE_NAME) != 0) {
            File fileJpg = new File(recipe.getPath());
            u = Uri.fromFile(fileJpg);
            uris.add(u);
        }

        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(!installed){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            // Get the layout inflater

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setTitle(activity.getResources().getString(R.string.email_alert_title))
                    .setMessage(activity.getResources().getString(R.string.email_alert_body))
                    .setPositiveButton(activity.getResources().getString(R.string.aceptar), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            activity.startActivity(emailIntent);
                        }
                    });

            builder.show();
        }else
            activity.startActivity(emailIntent);
    }

    private boolean isPackageInstalled(String packagename, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }


}
