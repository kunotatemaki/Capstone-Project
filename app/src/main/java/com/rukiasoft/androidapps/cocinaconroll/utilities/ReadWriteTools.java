package com.rukiasoft.androidapps.cocinaconroll.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.database.DatabaseRelatedTools;
import com.rukiasoft.androidapps.cocinaconroll.loader.PreinstalledRecipeNamesList;
import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.zip.UnzipUtility;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ruler on 21/09/2015 for the Udacity Nanodegree.
 */
public class ReadWriteTools {
    final Context mContext;
    
    public ReadWriteTools(Context mContext){
        this.mContext = mContext;
    }

    public List<String> loadFiles(FilenameFilter filter, Boolean external_storage){
        List<String> list = new ArrayList<>();
        Boolean ret;

        ret = isExternalStorageWritable();
        if(!ret){
            return list;
        }
        // Get the directory for the app's private recipes directory.
        String path;
        if(external_storage)
            path = getEditedStorageDir();
        else
            path = getOriginalStorageDir();
        File file = new File(path);
        if (file.exists()) {
            String[] files = file.list(filter);
            Collections.addAll(list, files);
        }
        return list;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


    /**
     * Checks if external storage is available to at least read
     */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * Get
     */
    public String getOriginalStorageDir(){
        String path = mContext.getExternalFilesDir(null) + String.valueOf(File.separatorChar)
                + Constants.RECIPES_DIR + String.valueOf(File.separatorChar);
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public String getEditedStorageDir(){
        File rootPath = Environment.getExternalStoragePublicDirectory("");
        String path = rootPath.getAbsolutePath() + String.valueOf(File.separatorChar) +
                Constants.BASE_DIR + String.valueOf(File.separatorChar) +
                Constants.RECIPES_DIR + String.valueOf(File.separatorChar);
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    /**
     * Read recipe from xml
     */
    public RecipeItem readRecipe(String name, Integer type) {
        RecipeItem recipeItem;
        String path = "";
        File source;
        if(type.equals(Constants.PATH_TYPE_ASSETS)) {
            InputStream inputStream;
            try {
                inputStream = mContext.getAssets().open(name);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            source = createFileFromInputStream(inputStream);
            if(source == null)
                return null;
            recipeItem = parseFileIntoRecipe(source);
            if(recipeItem == null){
                return null;
            }
            recipeItem.setState(Constants.FLAG_ASSETS);
            recipeItem.setFileName(name);
            recipeItem.setFilePath(Constants.ASSETS_PATH);
            source.delete();
        }else {
            if (type.equals(Constants.PATH_TYPE_ORIGINAL))
                path = getOriginalStorageDir() + name;
            else if (type.equals(Constants.PATH_TYPE_EDITED))
                path = getEditedStorageDir() + name;
            source = new File(path);
            recipeItem = parseFileIntoRecipe(source);
            if(recipeItem == null)
                return null;
            recipeItem.setFilePath(path);
            recipeItem.setFileName(name);
            if (type.equals(Constants.PATH_TYPE_ORIGINAL)) {
                recipeItem.setState(Constants.FLAG_ORIGINAL);
                if(recipeItem.getDate() == null){
                    recipeItem.setDate(System.currentTimeMillis());
                    saveRecipeOnOrigialPath(recipeItem);
                }
            }
        }

        if((recipeItem.getState() & Constants.FLAG_EDITED_PICTURE) != 0)
            recipeItem.setPicturePath(Constants.FILE_PATH + getEditedStorageDir() + recipeItem.getPicture());
        else if((recipeItem.getState() & Constants.FLAG_ORIGINAL) != 0)
            recipeItem.setPicturePath(Constants.FILE_PATH + getOriginalStorageDir() + recipeItem.getPicture());
        else if((recipeItem.getState() & Constants.FLAG_ASSETS) != 0)
            recipeItem.setPicturePath(Constants.ASSETS_PATH + recipeItem.getPicture());


        return recipeItem;
    }


    private File createFileFromInputStream(InputStream inputStream) {

        try{
            //File f = new File(mContext.getFilesDir() + "temp.txt");
            File f = getTempFile();
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length;
            while((length=inputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }
            outputStream.close();
            inputStream.close();
            return f;
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File getTempFile() {
        File file;
        try {
            file = File.createTempFile("tmp.xml", null, mContext.getCacheDir());
            return file;
        } catch (IOException e) {
            e.printStackTrace();// Error while creating file
        }
        return null;
    }

    private RecipeItem parseFileIntoRecipe(File source){
        RecipeItem recipeItem;
        Serializer serializer = new Persister();
        try {
            recipeItem = serializer.read(RecipeItem.class, source, false);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return recipeItem;
    }

    public void saveRecipeOnOrigialPath(RecipeItem recipe){
        String path = getOriginalStorageDir();
        saveRecipe(recipe, path);
    }

    public void saveRecipeOnEditedPath(RecipeItem recipe){
        String path = getEditedStorageDir();
        saveRecipe(recipe, path);
    }

    public Boolean saveRecipe(RecipeItem recipe, String path){
        Boolean ret;
        ret = isExternalStorageWritable();
        if(!ret){
            if(mContext instanceof AppCompatActivity) {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.no_storage_available), Toast.LENGTH_LONG)
                .show();
            }
            return ret;
        }

        File file = new File(path);
        if (!file.exists()) {
            ret = file.mkdirs();
            if(!ret)
                return ret;
        }

        Serializer serializer = new Persister();
        File result = new File(path + recipe.getFileName());

        try {
            serializer.write(recipe, result);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public void deleteRecipe(RecipeItem recipeItem, Integer flags){
        String pathFile = "";
        String pathPicture = "";
        if((flags & Constants.FLAG_EDITED) != 0) {
            pathFile = getEditedStorageDir() + recipeItem.getFileName();
        }
        if((flags & Constants.FLAG_EDITED_PICTURE) != 0) {
            pathPicture = recipeItem.getPicturePath();
        }
        if((flags & Constants.FLAG_ORIGINAL) != 0) {
            pathFile = getOriginalStorageDir() + recipeItem.getFileName();
            pathPicture = getOriginalStorageDir() + recipeItem.getPicture();
        }
        File file = new File(pathFile);
        if(file.exists())
            file.delete();
        file = new File(String.valueOf(Uri.parse(pathPicture)));
        if(file.exists()) {
            file.delete();
        }else{
            Uri uri = Uri.fromFile(file);
            String deletePath = getEditedStorageDir() + uri.getLastPathSegment();
            file = new File(deletePath);
            if(file.exists())
                file.delete();
        }

    }

    public List<String> loadRecipesFromAssets() {

        List<String> list;
        File source;
        InputStream inputStream;
        try {
            inputStream = mContext.getAssets().open("preinstalled_recipes.xml");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        source = createFileFromInputStream(inputStream);
        list = parseFileIntoRecipeList(source);
        source.delete();

        return list;
    }

    private List<String> parseFileIntoRecipeList(File source){

        Serializer serializer = new Persister();
        PreinstalledRecipeNamesList preinstalledRecipeNames;
        try {
            preinstalledRecipeNames = serializer.read(PreinstalledRecipeNamesList.class, source);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return preinstalledRecipeNames.getPreinstalledRecipeNameListAsListOfStrings();
    }

    public void deleteImageFromEditedPath(String name) {
        String path = getEditedStorageDir() + name;
        File file = new File(path);
        if(file.exists())
            file.delete();
    }

    public String saveBitmap(Bitmap bitmap, String name){


        FileOutputStream out = null;
        String filename = "";
        File file = new File(getEditedStorageDir());
        if (!file.exists()) {
            Boolean ret = file.mkdirs();
            if(!ret)
                return "";
        }
        try {
            filename = getEditedStorageDir() + name;
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
        return Constants.FILE_PATH.concat(filename);
    }

    public void loadImageFromPath(ImageView imageView, String path, int defaultImage) {
       Glide.with(mContext)
               .load(Uri.parse(path))
               .centerCrop()
               .error(defaultImage)
               .into(imageView);
    }
    public void loadImageFromPath(BitmapImageViewTarget bitmapImageViewTarget, String path, int defaultImage) {
        Glide.with(mContext)
                .load(Uri.parse(path))
                .asBitmap()
                .centerCrop()
                .error(defaultImage)
                .into(bitmapImageViewTarget);
    }

    public void share(final Activity activity, RecipeItem recipe)
    {
        //TODO try with revealAction
        //need to "send multiple" to get more than one attachment
        Tools tools = new Tools();
        Boolean installed = tools.isPackageInstalled("com.google.android.gm", activity);
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
        File fileXml = new File(getEditedStorageDir() + recipe.getFileName());
        Uri u = Uri.fromFile(fileXml);
        uris.add(u);
        if(recipe.getPicturePath().compareTo(Constants.DEFAULT_PICTURE_NAME) != 0) {
            File fileJpg = new File(getEditedStorageDir() + recipe.getPicture());
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

    public String getZipsStorageDir(){
        return mContext.getExternalFilesDir(null) + String.valueOf(File.separatorChar)
                + Constants.ZIPS_DIR + String.valueOf(File.separatorChar);
    }

    public Boolean unzipRecipes(String name){
        UnzipUtility unzipper = new UnzipUtility();
        try {
            unzipper.unzip(getZipsStorageDir() + name,
                    getOriginalStorageDir());
        } catch (Exception ex) {
            // some errors occurred
            ex.printStackTrace();
            return false;
        }
        return true;
    }


    public void initDatabase() {
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools(mContext);
        MyFileFilter filter = new MyFileFilter();
        List<String> listEdited = loadFiles(filter, true);
        List<String> listOriginal = loadFiles(filter, false);
        List<String> listAssets = loadRecipesFromAssets();

        for(int i=0; i<listEdited.size(); i++) {
            RecipeItem recipeItem= readRecipe(listEdited.get(i),
                    Constants.PATH_TYPE_EDITED);
            if(recipeItem != null) {
                dbTools.insertRecipeIntoSuggestions(recipeItem);
            }
        }

        for(int i=0; i<listOriginal.size(); i++) {
            if(listEdited.contains(listOriginal.get(i)))
                continue;
            RecipeItem recipeItem= readRecipe(listOriginal.get(i),
                    Constants.PATH_TYPE_ORIGINAL);
            if(recipeItem != null) {
                dbTools.insertRecipeIntoSuggestions(recipeItem);
            }
        }

        for(int i=0; i<listAssets.size(); i++) {
            RecipeItem recipeItem;
            if(listEdited.contains(listAssets.get(i)))
                continue;
            recipeItem = readRecipe(listAssets.get(i),
                    Constants.PATH_TYPE_ASSETS);
            if (recipeItem != null) {
                dbTools.insertRecipeIntoSuggestions(recipeItem);
            }
        }
        return;
    }



    private class MyFileFilter implements FilenameFilter {

        @Override
        public boolean accept(File directory, String fileName) {
            return fileName.endsWith(".xml");
        }
    }

}
