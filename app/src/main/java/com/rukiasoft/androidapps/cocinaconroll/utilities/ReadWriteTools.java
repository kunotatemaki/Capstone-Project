package com.rukiasoft.androidapps.cocinaconroll.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.classes.PreinstalledRecipeNamesList;
import com.rukiasoft.androidapps.cocinaconroll.classes.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.database.DatabaseRelatedTools;
import com.rukiasoft.androidapps.cocinaconroll.zip.UnzipUtility;

import org.acra.ACRA;
import org.apache.commons.io.FileUtils;
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


public class ReadWriteTools {
    //private final Context mContext;
    private final String TAG = LogHelper.makeLogTag(ReadWriteTools.class);
    public ReadWriteTools(){

    }

    public List<String> loadFiles(Context mContext, FilenameFilter filter, Boolean external_storage){
        List<String> list = new ArrayList<>();
        Boolean ret;

        ret = isExternalStorageWritable();
        if(!ret){
            LogHelper.e(TAG, "no hay external storage in loadfiles");
            return list;
        }
        // Get the directory for the app's private recipes directory.
        String path;
        if(external_storage) {
            path = getEditedStorageDir();
        }else {
            path = getOriginalStorageDir(mContext);
        }
        File file = new File(path);
        if (file.exists()) {
            String[] files = file.list(filter);
            if(files != null) {
                Collections.addAll(list, files);
            }
        }
        return list;
    }

    public List<String> loadRecipesFromOldDirectory(FilenameFilter filter){
        List<String> list = new ArrayList<>();
        Boolean ret;

        ret = isExternalStorageWritable();
        if(!ret){
            LogHelper.e(TAG, "no hay external storage in loadrecipesfromolddirectory");
            return list;
        }
        // Get the directory for the old app's private recipes directory.
        String path = getOldEditedStorageDir();
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
    public String getOriginalStorageDir(Context mContext){
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

    public String getOldEditedStorageDir(){
        File rootPath = Environment.getExternalStoragePublicDirectory("");
        String path = rootPath.getAbsolutePath() + String.valueOf(File.separatorChar) +
                Constants.OLD_BASE_DIR + String.valueOf(File.separatorChar) +
                Constants.RECIPES_DIR + String.valueOf(File.separatorChar);
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public File getOldBaseEditedStorageDirToBeDeleted(){
        File rootPath = Environment.getExternalStoragePublicDirectory("");
        String path = rootPath.getAbsolutePath() + String.valueOf(File.separatorChar) +
                Constants.OLD_BASE_DIR;
        return new File(path);
    }

    /**
     * Read recipe from xml
     */
    public RecipeItem readRecipe(Context mContext, String name, Integer type) {
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
            source = createFileFromInputStream(mContext, inputStream);
            if(source == null)
                return null;
            recipeItem = parseFileIntoRecipe(source);
            if(recipeItem == null){
                return null;
            }
            recipeItem.setState(Constants.FLAG_ASSETS);
            recipeItem.setPathRecipe(Constants.ASSETS_PATH + name);
            source.delete();
        }else {
            if (type.equals(Constants.PATH_TYPE_ORIGINAL)) {
                path = getOriginalStorageDir(mContext) + name;
            }else if (type.equals(Constants.PATH_TYPE_EDITED)) {
                path = getEditedStorageDir() + name;
            }else if (type.equals(Constants.PATH_TYPE_OLD_EDITED)) {
                path = getOldEditedStorageDir() + name;
            }
            source = new File(path);
            recipeItem = parseFileIntoRecipe(source);
            if(recipeItem == null)
                return null;
            recipeItem.setPathRecipe(path);
            if (type.equals(Constants.PATH_TYPE_ORIGINAL)) {
                recipeItem.setState(Constants.FLAG_ORIGINAL);
                if(recipeItem.getDate() == -1L){
                    recipeItem.setDate(System.currentTimeMillis());
                }
            }
        }

        if((recipeItem.getState() & Constants.FLAG_EDITED_PICTURE) != 0)
            recipeItem.setPathPicture(Constants.FILE_PATH + getEditedStorageDir() + recipeItem.getPicture());
        else if((recipeItem.getState() & Constants.FLAG_ORIGINAL) != 0)
            recipeItem.setPathPicture(Constants.FILE_PATH + getOriginalStorageDir(mContext) + recipeItem.getPicture());
        else if((recipeItem.getState() & Constants.FLAG_ASSETS) != 0)
            recipeItem.setPathPicture(Constants.ASSETS_PATH + recipeItem.getPicture());

        return recipeItem;
    }


    private File createFileFromInputStream(Context mContext, InputStream inputStream) {

        try{
            //File f = new File(mContext.getFilesDir() + "temp.txt");
            File f = getTempFile(mContext);
            if(f== null){
                return null;
            }else {
                OutputStream outputStream = new FileOutputStream(f);

                byte buffer[] = new byte[1024];
                int length;
                while((length=inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer,0,length);
                }
                outputStream.close();
                inputStream.close();
                return f;
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File getTempFile(Context mContext) {
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

    /*public void saveRecipeOnOrigialPath(RecipeItem recipe){
        String path = getOriginalStorageDir();
        saveRecipe(recipe, path);
    }*/

    public String saveRecipeOnEditedPath(Context mContext, RecipeItem recipe){
        String dir = getEditedStorageDir();
        String pathFile = recipe.getPathRecipe();
        String name;
        if(pathFile == null || pathFile.isEmpty()) {
            Tools mTools = new Tools();
            name = "own_" + mTools.getCurrentDate(mContext) + ".xml";
        }else{
            Uri uri = Uri.parse(pathFile);
            name = uri.getLastPathSegment();
        }
        return saveRecipe(mContext, recipe, dir, name);
    }

    public String saveRecipe(Context mContext, RecipeItem recipe, String dir, String name){
        if(!isExternalStorageWritable()){
            if(mContext instanceof AppCompatActivity) {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.no_storage_available), Toast.LENGTH_LONG)
                .show();
            }
            LogHelper.e(TAG, "no hay external storage in saverecipe");
            return "";
        }

        File file = new File(dir);
        if (!file.exists()) {
            if(!file.mkdirs())
                return "";
        }

        Serializer serializer = new Persister();
        String path = dir.concat(name);
        File result = new File(path);

        try {
            serializer.write(recipe, result);
            return path;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    public void deleteRecipe(RecipeItem recipeItem){

        try {
            File file = new File(recipeItem.getPathRecipe());
            if (file.exists())
                file.delete();
            if ((recipeItem.getState() & Constants.FLAG_EDITED_PICTURE) != 0) {
                file = new File(String.valueOf(Uri.parse(recipeItem.getPathPicture())));
                if (file.exists()) {
                    file.delete();
                } else {
                    Uri uri = Uri.fromFile(file);
                    String deletePath = getEditedStorageDir() + uri.getLastPathSegment();
                    file = new File(deletePath);
                    if (file.exists())
                        file.delete();
                }
            }
        }catch(Exception e){
            ACRA.getErrorReporter().handleSilentException(e);
        }
    }

    public void deleteRecipe(String path){

        try {
            File file = new File(path);
            if (file.exists())
                file.delete();
        }catch(Exception e){
            ACRA.getErrorReporter().handleSilentException(e);
        }
    }

    public List<String> loadRecipesFromAssets(Context mContext) {

        List<String> list;
        File source;
        InputStream inputStream;
        try {
            inputStream = mContext.getAssets().open("preinstalled_recipes.xml");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        source = createFileFromInputStream(mContext, inputStream);
        list = parseFileIntoRecipeList(source);
        if (source != null) {
            source.delete();
        }

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
        Uri uri = Uri.parse(name);
        name =  uri.getLastPathSegment();
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

    public void loadImageFromPath(Context mContext, ImageView imageView, String path, int defaultImage, int version) {
       Glide.with(mContext)
               .load(Uri.parse(path))
               .centerCrop()
               .signature(new MediaStoreSignature(Constants.MIME_TYPE_PICTURE, version, 0))
               .error(defaultImage)
               .into(imageView);
    }

    public void loadImageFromPathInCircle(Context mContext, ImageView imageView, String path, int defaultImage, int version) {
       Glide.with(mContext)
               .load(Uri.parse(path))
               .centerCrop()
               .signature(new MediaStoreSignature(Constants.MIME_TYPE_PICTURE, version, 0))
               .transform(new GlideCircleTransform(mContext))
               .error(defaultImage)
               .into(imageView);
    }

    public void loadImageFromPath(Context mContext, BitmapImageViewTarget bitmapImageViewTarget, String path, int defaultImage, int version) {
        Glide.with(mContext)
                .load(Uri.parse(path))
                .asBitmap()
                .signature(new MediaStoreSignature(Constants.MIME_TYPE_PICTURE, version, 0))
                .centerCrop()
                .error(defaultImage)
                .into(bitmapImageViewTarget);
    }

    public void share(final Activity activity, RecipeItem recipe)
    {
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
        File fileXml = new File(recipe.getPathRecipe());
        Uri u = Uri.fromFile(fileXml);
        uris.add(u);
        if((recipe.getState()&Constants.FLAG_EDITED_PICTURE) != 0) {
            Uri uri = Uri.parse(recipe.getPathPicture());
            String name = uri.getLastPathSegment();
            File fileJpg = new File(getEditedStorageDir() + name);
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

    public String getZipsStorageDir(Context mContext){
        //create the dir if dont exist
        String path = mContext.getExternalFilesDir(null) + String.valueOf(File.separatorChar)
                + Constants.ZIPS_DIR + String.valueOf(File.separatorChar);
        File f = new File(path);
        if(!f.exists()){
            f.mkdirs();
        }
        return path;
    }

    public Uri zipRecipe(Context mContext, List<Uri> filesToZip, String zipName){
        UnzipUtility unzipper = new UnzipUtility();
        String zipPath = getZipsStorageDir(mContext) + zipName;
        try {
            unzipper.zip(filesToZip, zipPath);
        } catch (Exception ex) {
            // some errors occurred
            ex.printStackTrace();
            return null;
        }
        return Uri.parse(zipPath);
    }

    public Boolean unzipRecipesInOriginal(Context mContext, String name){
        return unzipRecipes(mContext, name, getOriginalStorageDir(mContext));
    }

    public Boolean unzipRecipesInEdited(Context mContext, String name){
        return unzipRecipes(mContext, name, getEditedStorageDir());
    }

    private Boolean unzipRecipes(Context mContext, String name, String path){
        UnzipUtility unzipper = new UnzipUtility();
        try {
            unzipper.unzip(getZipsStorageDir(mContext) + name,
                    path);
        } catch (Exception ex) {
            // some errors occurred
            ex.printStackTrace();
            return false;
        }
        return true;
    }


    public void initDatabaseWithOriginalPath(Context mContext) {
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools();
        MyFileFilter filter = new MyFileFilter();

        List<String> listAssets = loadRecipesFromAssets(mContext);
        for(int i=0; i<listAssets.size(); i++) {
            RecipeItem recipeItem;
            recipeItem = readRecipe(mContext, listAssets.get(i),
                    Constants.PATH_TYPE_ASSETS);
            if (recipeItem != null) {
                dbTools.insertRecipeIntoDatabase(mContext, recipeItem, true);
            }
        }

        List<String> listOriginal = loadFiles(mContext, filter, false);
        for(int i=0; i<listOriginal.size(); i++) {
            RecipeItem recipeItem= readRecipe(mContext, listOriginal.get(i),
                    Constants.PATH_TYPE_ORIGINAL);
            if(recipeItem != null) {
                dbTools.insertRecipeIntoDatabase(mContext, recipeItem, true);
            }
        }

    }

    public void initDatabaseWithEditedPath(Context mContext) {
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools();
        MyFileFilter filter = new MyFileFilter();

        //files created or modified from previous versions
        List<String> listOldFiles = loadRecipesFromOldDirectory(filter);
        for(int i=0; i<listOldFiles.size(); i++) {
            RecipeItem recipeItem = readRecipe(mContext, listOldFiles.get(i),
                    Constants.PATH_TYPE_OLD_EDITED);
            if(recipeItem != null) {
                if((recipeItem.getState()&(Constants.FLAG_EDITED | Constants.FLAG_OWN)) == 0){
                    //not created nor edited. It was an original recipe set as favorite
                    dbTools.updateFavoriteByFileName(mContext, recipeItem.getName(), recipeItem.getFavourite());
                    //delete the file
                    deleteRecipe(recipeItem);
                }else{
                    String picture = "";
                    if((recipeItem.getState() & Constants.FLAG_EDITED_PICTURE) != 0) {
                        picture = recipeItem.getPicture();
                    }
                    moveFileToEditedStorageAndDeleteOriginal(listOldFiles.get(i), picture);
                }
                //dbTools.insertRecipeIntoDatabase(recipeItem, true);
            }
        }

        //delete the old directory
        if(getOldBaseEditedStorageDirToBeDeleted() != null) {
            try {
                FileUtils.deleteDirectory(getOldBaseEditedStorageDirToBeDeleted());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //edited directory
        List<String> listEdited = loadFiles(mContext, filter, true);
        for(int i=0; i<listEdited.size(); i++) {
            RecipeItem recipeItem= readRecipe(mContext, listEdited.get(i),
                    Constants.PATH_TYPE_EDITED);
            if(recipeItem != null) {
                dbTools.insertRecipeIntoDatabase(mContext, recipeItem, true);
            }
        }


    }

    private void moveFileToEditedStorageAndDeleteOriginal(String name, String picture){
        String sourcePath = getOldEditedStorageDir() + name;
        String sourceImagePath;
        File source = new File(sourcePath);
        File sourceImage = null;

        String destinationPath = getEditedStorageDir() + name;
        String destinationImagePath;
        File destination = new File(destinationPath);
        File destinationImage = null;

        if(!picture.isEmpty()) {
            sourceImagePath = getOldEditedStorageDir() + picture;
            sourceImage = new File(sourceImagePath);
            destinationImagePath = getEditedStorageDir() + picture;
            destinationImage = new File(destinationImagePath);
        }

        try
        {
            FileUtils.copyFile(source, destination);
            FileUtils.forceDelete(source);
            if(sourceImage != null && destinationImage != null){
                FileUtils.copyFile(sourceImage, destinationImage);
                FileUtils.forceDelete(sourceImage);
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public RecipeItem readRecipeInfo(Context mContext, String pathRecipe) {
        RecipeItem recipeItem;
        File source;
        if(pathRecipe == null){
            Exception caughtException = new Exception("Error intentado leer una receta sin pathRecipe");
            ACRA.getErrorReporter().handleSilentException(caughtException);
            return null;
        }

        if(pathRecipe.contains(Constants.ASSETS_PATH)) {
            Uri uri = Uri.parse(pathRecipe);
            String name =  uri.getLastPathSegment();
            InputStream inputStream;
            try {
                inputStream = mContext.getAssets().open(name);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            source = createFileFromInputStream(mContext, inputStream);
            if(source == null)
                return null;
            recipeItem = parseFileIntoRecipe(source);
            if(recipeItem == null){
                return null;
            }
            recipeItem.setState(Constants.FLAG_ASSETS);
            //recipeItem.setFileName(name);
            recipeItem.setPathRecipe(Constants.ASSETS_PATH + "/" + name);
            source.delete();
        }else {
            source = new File(pathRecipe);
            recipeItem = parseFileIntoRecipe(source);
            if(recipeItem == null)
                return null;
            recipeItem.setPathRecipe(pathRecipe);

        }

        return recipeItem;

    }

    public void loadNewFilesAndInsertInDatabase(Context mContext) {
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools();
        MyFileFilter filter = new MyFileFilter();
        List<String> listOriginal = loadFiles(mContext, filter, false);
        for(int i=0; i<listOriginal.size(); i++) {
            RecipeItem recipeItem= readRecipe(mContext, listOriginal.get(i),
                    Constants.PATH_TYPE_ORIGINAL);
            if(recipeItem != null) {
                dbTools.insertRecipeIntoDatabase(mContext, recipeItem, false);
            }
        }
    }

    public void loadUpdatedFilesAndInsertInDatabase(Context mContext, String name, int version) {
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools();
        RecipeItem recipeItem= readRecipe(mContext, name,
                Constants.PATH_TYPE_EDITED);
        if(recipeItem != null) {
            recipeItem.setVersion(version);
            recipeItem.setState(Constants.FLAG_SINCRONIZED_WITH_DRIVE);
            dbTools.insertRecipeIntoDatabase(mContext, recipeItem, true);
        }
    }

    private class MyFileFilter implements FilenameFilter {

        @Override
        public boolean accept(File directory, String fileName) {
            return fileName.endsWith(".xml");
        }
    }

    public void deleteImage(String image){
        if(image != null){
            deleteImageFromEditedPath(image);
        }
        /*if(file != null){
            deleteRecipe(file);
        }*/
    }

    public boolean deleteFile(Uri path){
        return deleteFile(path.getPath());
    }

    public boolean deleteFile(String path) {
        File file = new File(path);
        return file.exists() && file.delete();
    }

    public boolean deleteZipByPath(Uri path){
        return deleteFile(path);
    }

    public boolean deleteZipByName(Context mContext, String name){
        String path = getZipsStorageDir(mContext) + name;
        return deleteFile(Uri.parse(path));
    }

}
