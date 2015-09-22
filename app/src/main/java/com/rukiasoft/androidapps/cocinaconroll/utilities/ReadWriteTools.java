package com.rukiasoft.androidapps.cocinaconroll.utilities;

import android.content.Context;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.rukiasoft.androidapps.cocinaconroll.Constants;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.loader.PreinstalledRecipeNamesList;
import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeItem;

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
    Context mContext;
    
    public ReadWriteTools(Context mContext){
        this.mContext = mContext;
    }

    public List<String> loadFiles(FilenameFilter filter, Boolean external_storage){
        List<String> list = new ArrayList<>();
        Boolean ret;
        //TODO - era writable. Cambiado a readable. Comprobar
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
        int stringId = mContext.getApplicationInfo().labelRes;
        String dir = mContext.getString(stringId);
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
            recipeItem = parseFileIntoRecipe(source);
            recipeItem.setState(Constants.FLAG_ASSETS);
            recipeItem.setFileName(name);
            source.delete();
        }else {
            if (type.equals(Constants.PATH_TYPE_ORIGINAL))
                path = getOriginalStorageDir() + name;
            else if (type.equals(Constants.PATH_TYPE_EDITED))
                path = getEditedStorageDir() + name;
            source = new File(path);
            recipeItem = parseFileIntoRecipe(source);
            if(recipeItem == null)
                return recipeItem;
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
            recipeItem.setPath(getEditedStorageDir() + recipeItem.getPicture());
        else if((recipeItem.getState() & Constants.FLAG_ORIGINAL) != 0)
            recipeItem.setPath(getOriginalStorageDir() + recipeItem.getPicture());
        else if((recipeItem.getState() & Constants.FLAG_ASSETS) != 0)
            recipeItem.setPath(Constants.ASSETS_PATH + recipeItem.getPicture());

        File f = new File(recipeItem.getPath());
        if(!f.exists() && (recipeItem.getState() & Constants.FLAG_ASSETS) == 0)
            recipeItem.setPath(Constants.DEFAULT_PICTURE_NAME);

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

    public Boolean saveRecipeOnOrigialPath(RecipeItem recipe){
        String path = getOriginalStorageDir();
        return saveRecipe(recipe, path);
    }

    public Boolean saveRecipeOnEditedPath(RecipeItem recipe){
        String path = getEditedStorageDir();
        return saveRecipe(recipe, path);
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
            pathPicture = recipeItem.getPath();
        }
        if((flags & Constants.FLAG_ORIGINAL) != 0) {
            pathFile = getOriginalStorageDir() + recipeItem.getFileName();
            pathPicture = getOriginalStorageDir() + recipeItem.getPicture();
        }
        File file = new File(pathFile);
        if(file.exists())
            file.delete();
        file = new File(pathPicture);
        if(file.exists())
            file.delete();

    }

    public List<String> loadRecipesFromAssets() {

        List<String> list = new ArrayList<>();
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
        return preinstalledRecipeNames.getPreinstalledRecipeNameListAsListOfStrings();
    }
}
