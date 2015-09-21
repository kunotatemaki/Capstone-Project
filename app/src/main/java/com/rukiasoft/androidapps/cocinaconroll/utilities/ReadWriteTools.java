package com.rukiasoft.androidapps.cocinaconroll.utilities;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import com.rukiasoft.androidapps.cocinaconroll.Constants;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.loader.PreinstalledRecipeNamesList;
import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeItem;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Ruler on 21/09/2015 for the Udacity Nanodegree.
 */
public class ReadWriteTools {

    public static List<String> loadFiles(Context context, FilenameFilter filter, Boolean external_storage){
        List<String> list = new ArrayList<>();
        Boolean ret;
        //TODO - era writable. Cambiado a readable. Comprobar
        ret = ReadWriteTools.isExternalStorageReadable();
        if(!ret){
            return list;
        }
        // Get the directory for the app's private recipes directory.
        String path;
        if(external_storage)
            path = getEditedStorageDir(context);
        else
            path = getOriginalStorageDir(context);
        File file = new File(path);
        if (file.exists()) {
            String[] files = file.list(filter);
            Collections.addAll(list, files);
        }
        return list;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


    /**
     * Checks if external storage is available to at least read
     * @return
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * Get
     * @param context
     * @return
     */
    public static String getOriginalStorageDir(Context context){
        String path = context.getExternalFilesDir(null) + String.valueOf(File.separatorChar)
                + Constants.RECIPES_DIR + String.valueOf(File.separatorChar);
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public static String getEditedStorageDir(Context context){
        File rootPath = Environment.getExternalStoragePublicDirectory("");
        int stringId = context.getApplicationInfo().labelRes;
        String dir = context.getString(stringId);
        dir = dir.replaceAll("\\s","");
        String path = rootPath.getAbsolutePath() + String.valueOf(File.separatorChar) +
                dir + String.valueOf(File.separatorChar) +
                Constants.RECIPES_DIR + String.valueOf(File.separatorChar);
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    /**
     * Read recipe from xml
     * @param context
     * @param name
     * @param type
     * @return
     */
    public static RecipeItem readRecipe(Context context, String name, Integer type) {
        RecipeItem recipeItem;
        String path = "";
        File source;
        if(type.equals(Constants.PATH_TYPE_ASSETS)) {
            InputStream inputStream;
            try {
                inputStream = context.getAssets().open(name);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            source = createFileFromInputStream(context, inputStream);
            recipeItem = parseFileIntoRecipe(source);
            recipeItem.setState(Constants.FLAG_ASSETS);
            recipeItem.setFileName(name);
            source.delete();
        }else {
            if (type.equals(Constants.PATH_TYPE_ORIGINAL))
                path = getOriginalStorageDir(context) + name;
            else if (type.equals(Constants.PATH_TYPE_EDITED))
                path = getEditedStorageDir(context) + name;
            source = new File(path);
            recipeItem = parseFileIntoRecipe(source);
            if(recipeItem == null)
                return recipeItem;
            recipeItem.setFileName(name);
            if (type.equals(Constants.PATH_TYPE_ORIGINAL)) {
                recipeItem.setState(Constants.FLAG_ORIGINAL);
                if(recipeItem.getDate() == null){
                    recipeItem.setDate(System.currentTimeMillis());
                    saveRecipeOnOrigialPath(context, recipeItem);
                }
            }
        }

        if((recipeItem.getState() & Constants.FLAG_EDITED_PICTURE) != 0)
            recipeItem.setPath(getEditedStorageDir(context) + recipeItem.getPicture());
        else if((recipeItem.getState() & Constants.FLAG_ORIGINAL) != 0)
            recipeItem.setPath(getOriginalStorageDir(context) + recipeItem.getPicture());
        else if((recipeItem.getState() & Constants.FLAG_ASSETS) != 0)
            recipeItem.setPath(Constants.ASSETS_PATH + recipeItem.getPicture());

        File f = new File(recipeItem.getPath());
        if(!f.exists() && (recipeItem.getState() & Constants.FLAG_ASSETS) == 0)
            recipeItem.setPath(Constants.DEFAULT_PICTURE_NAME);

        return recipeItem;
    }


    private static File createFileFromInputStream(Context context, InputStream inputStream) {

        try{
            //File f = new File(context.getFilesDir() + "temp.txt");
            File f = getTempFile(context);
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

    private static File getTempFile(Context context) {
        File file;
        try {
            file = File.createTempFile("tmp.xml", null, context.getCacheDir());
            return file;
        } catch (IOException e) {
            e.printStackTrace();// Error while creating file
        }
        return null;
    }

    private static RecipeItem parseFileIntoRecipe(File source){
        RecipeItem recipeItem = new RecipeItem();
        Serializer serializer = new Persister();
        try {
            recipeItem = serializer.read(RecipeItem.class, source);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return recipeItem;
    }

    public static Boolean saveRecipeOnOrigialPath(Context context, RecipeItem recipe){
        String path = getOriginalStorageDir(context);
        return saveRecipe(context, recipe, path);
    }

    public static Boolean saveRecipeOnEditedPath(Context context, RecipeItem recipe){
        String path = getEditedStorageDir(context);
        return saveRecipe(context, recipe, path);
    }

    public static Boolean saveRecipe(Context context, RecipeItem recipe, String path){
        Boolean ret;
        ret = isExternalStorageWritable();
        if(!ret){
            Tools.showToast(context, context.getResources().getString(R.string.no_storage_available));
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

    public static void deleteRecipe(Context context, RecipeItem recipeItem, Integer flags){
        String pathFile = "";
        String pathPicture = "";
        if((flags & Constants.FLAG_EDITED) != 0) {
            pathFile = getEditedStorageDir(context) + recipeItem.getFileName();
        }
        if((flags & Constants.FLAG_EDITED_PICTURE) != 0) {
            pathPicture = recipeItem.getPath();
        }
        if((flags & Constants.FLAG_ORIGINAL) != 0) {
            pathFile = getOriginalStorageDir(context) + recipeItem.getFileName();
            pathPicture = getOriginalStorageDir(context) + recipeItem.getPicture();
        }
        File file = new File(pathFile);
        if(file.exists())
            file.delete();
        file = new File(pathPicture);
        if(file.exists())
            file.delete();

    }

    public static List<String> loadRecipesFromAssets(Context context) {

        List<String> list = new ArrayList<>();
        AssetManager am = context.getAssets();
        RecipeAssetsItemLoader recipeAssetsItemLoader;
        try {
            recipeAssetsItemLoader = new RecipeAssetsItemLoader(am.open("preinstalled_recipes.xml"), context);
            list = recipeAssetsItemLoader.getItems();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        File source;
        InputStream inputStream;
        try {
            inputStream = context.getAssets().open("preinstalled_recipes.xml");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        source = createFileFromInputStream(context, inputStream);
        parseFileIntoRecipeList(source);
        source.delete();

        return list;
    }

    private static List<RecipeItem> parseFileIntoRecipeList(File source){

        List<RecipeItem> list = new ArrayList<>();
        Serializer serializer = new Persister();
        PreinstalledRecipeNamesList preinstalledRecipeNames = new PreinstalledRecipeNamesList();
        try {
            preinstalledRecipeNames = serializer.read(PreinstalledRecipeNamesList.class, source);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        //TODO-pass recipes to list and delete XMLFActory
        return list;
    }
}
