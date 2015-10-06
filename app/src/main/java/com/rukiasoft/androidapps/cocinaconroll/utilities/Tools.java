package com.rukiasoft.androidapps.cocinaconroll.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.signature.StringSignature;
import com.rukiasoft.androidapps.cocinaconroll.Constants;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.ToolbarAndRefreshActivity;
import com.rukiasoft.androidapps.cocinaconroll.database.CocinaConRollContentProvider;
import com.rukiasoft.androidapps.cocinaconroll.database.RecipeInfoDataBase;
import com.rukiasoft.androidapps.cocinaconroll.database.SuggestionsTable;
import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

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
        input = input.trim();
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
    public String saveBitmap(Context context, Bitmap bitmap, String name){


        FileOutputStream out = null;
        String filename = "";
        ReadWriteTools tools = new ReadWriteTools(context);
        File file = new File(tools.getEditedStorageDir());
        if (!file.exists()) {
            Boolean ret = file.mkdirs();
            if(!ret)
                return "";
        }
        try {
            filename = tools.getEditedStorageDir() + name;
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String ret = Constants.FILE_PATH.concat(filename);
        return  ret;
    }

    public void loadImageFromPath(Context context, ImageView imageView, String path, int defaultImage, boolean clear) {
        if(clear){
            Glide.clear(imageView);
            Glide.with(context)
                    .load(Uri.parse(path))
                    .centerCrop()
                    .skipMemoryCache(true)
                    .error(defaultImage)
                    .signature(new StringSignature(UUID.randomUUID().toString()))
                    .into(imageView);
        }else {
            Glide.with(context)
                    .load(Uri.parse(path))
                    .centerCrop()
                    .error(defaultImage)
                    .into(imageView);
        }
    }
    public void loadImageFromPath(Context context, BitmapImageViewTarget bitmapImageViewTarget, String path, int defaultImage) {
        Glide.with(context)
                .load(Uri.parse(path))
                .asBitmap()
                .centerCrop()
                .error(defaultImage)
                .into(bitmapImageViewTarget);
    }

    public List<RecipeInfoDataBase> getRecipeInfoInDatabase(Context context, String name, boolean match){
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
        Cursor cursor = context.getContentResolver().query(CocinaConRollContentProvider.CONTENT_URI_SUGGESTIONS,
                projection,
                selection ,
                selectionArgs, null);

        if (cursor.moveToFirst()) {
            do {
                RecipeInfoDataBase recipeInfoDataBase = new RecipeInfoDataBase();
                recipeInfoDataBase.setName(cursor.getString(0).toString());
                int fav = cursor.getInt(1);
                if(fav > 0){
                    recipeInfoDataBase.setFavorite(true);
                }else{
                    recipeInfoDataBase.setFavorite(false);
                }
                list.add(recipeInfoDataBase);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean isFavorite(Context context, String recipeName){
        List<RecipeInfoDataBase> list = getRecipeInfoInDatabase(context, recipeName, true);
        if(list.size() == 1) {
            return list.get(0).isFavorite();
        }else{
            return false;
        }
    }

    public void insertRecipeIntoSuggestions(Context context, RecipeItem recipeItem) {
        ContentValues values = new ContentValues();
        values.put(SuggestionsTable.FIELD_NAME, recipeItem.getName());
        values.put(SuggestionsTable.FIELD_NAME_NORMALIZED, getNormalizedString(recipeItem.getName()));
        int icon;
        if(recipeItem.getType().equals(Constants.TYPE_DESSERTS))    icon = R.drawable.ic_dessert_24;
        else if(recipeItem.getType().equals(Constants.TYPE_STARTERS))    icon = R.drawable.ic_starters_24;
        else if(recipeItem.getType().equals(Constants.TYPE_MAIN))    icon = R.drawable.ic_main_24;
        else icon = R.drawable.ic_all_24;
        values.put(SuggestionsTable.FIELD_ICON, icon);
        context.getContentResolver().insert(CocinaConRollContentProvider.CONTENT_URI_SUGGESTIONS, values);
    }

    public void updateFavorite(Context context, String name, boolean favorite) {
        ContentValues values = new ContentValues();
        values.put(SuggestionsTable.FIELD_NAME, name);
        int iFavorite = favorite? 1 : 0;
        values.put(SuggestionsTable.FIELD_NAME_FAVORITE, iFavorite);
        String clause = SuggestionsTable.FIELD_NAME_NORMALIZED + " = ? ";
        String[] args = {getNormalizedString(values.get(SuggestionsTable.FIELD_NAME).toString())};
        context.getContentResolver().update(CocinaConRollContentProvider.CONTENT_URI_SUGGESTIONS, values, clause, args);
    }
}
