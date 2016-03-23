package com.rukiasoft.androidapps.cocinaconroll.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.api.client.util.IOUtils;
import com.rukiasoft.androidapps.cocinaconroll.CocinaConRollApplication;
import com.rukiasoft.androidapps.cocinaconroll.classes.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.database.DatabaseRelatedTools;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;
import com.rukiasoft.androidapps.cocinaconroll.utilities.ReadWriteTools;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import org.acra.ACRA;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class DriveService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String TAG = LogHelper.makeLogTag(DriveService.class);
    private static final String EXTRA_PARAM_RECIPE = "com.rukiasoft.androidapps.cocinaconroll.ui.extra.RECIPE";

    private static final CustomPropertyKey KEY_VERSION = new CustomPropertyKey("version", CustomPropertyKey.PRIVATE);

    private boolean mHasMore;
    private String mNextPageToken;

    public DriveService() {
        super("DriveService");
    }

    private CocinaConRollApplication getMyApplication(){
        return (CocinaConRollApplication)getApplication();
    }


    public static void startActionUploadRecipe(Context context, RecipeItem recipeItem) {

        Intent intent = new Intent(context, DriveService.class);
        intent.setAction(Constants.ACTION_UPLOAD_RECIPE);
        intent.putExtra(EXTRA_PARAM_RECIPE, recipeItem);
        context.startService(intent);
    }

    public static void startActionDeleteRecipe(Context context, RecipeItem recipeItem) {

        Intent intent = new Intent(context, DriveService.class);
        intent.setAction(Constants.ACTION_DELETE_RECIPE);
        intent.putExtra(EXTRA_PARAM_RECIPE, recipeItem);
        context.startService(intent);
    }


    public static void startActionGetRecipesFromDrive(Context context) {
        Intent intent = new Intent(context, DriveService.class);
        intent.setAction(Constants.ACTION_GET_RECIPES_FROM_DRIVE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_UPLOAD_RECIPE.equals(action)) {
                final RecipeItem recipeItem = intent.getParcelableExtra(EXTRA_PARAM_RECIPE);
                handleActionUploadRecipe(recipeItem);
            }else if (Constants.ACTION_DELETE_RECIPE.equals(action)) {
                final RecipeItem recipeItem = intent.getParcelableExtra(EXTRA_PARAM_RECIPE);
                handleActionDeleteRecipe(recipeItem);
            } else if (Constants.ACTION_GET_RECIPES_FROM_DRIVE.equals(action)) {
                handleActionGetRecipesFromDrive();
            }
        }
    }

    /**
     * Handle action UploadRecipe in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUploadRecipe(RecipeItem recipeItem) {
        Uri uriRecipe = Uri.parse(recipeItem.getPathRecipe());
        boolean updated;
        Uri uriPicture;
        List<Uri> filesToZip = new ArrayList<>();
        filesToZip.add(uriRecipe);
        if(!recipeItem.getPathPicture().equals(Constants.DEFAULT_PICTURE_NAME)
                && (recipeItem.getState() & Constants.FLAG_EDITED_PICTURE) != 0){
            uriPicture = Uri.parse(recipeItem.getPathPicture());
            filesToZip.add(uriPicture);
        }
        ReadWriteTools rwTools = new ReadWriteTools();
        String name = uriRecipe.getLastPathSegment().replace("xml", "zip");
        Uri uriZipPath = rwTools.zipRecipe(getApplicationContext(), filesToZip, name);
        if(uriZipPath == null){
            return;
        }
        updated = uploadFileToDrive(uriZipPath, Constants.MIME_TYPE_ZIP, recipeItem.getVersion());
        rwTools.deleteZipByPath(uriZipPath);
        if(updated) {
            Intent localIntent =
                    new Intent(Constants.ACTION_BROADCAST_UPLOADED_RECIPE)
                            // Puts the status into the Intent
                            .putExtra(Constants.KEY_RECIPE, recipeItem);
            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }

    private boolean uploadFileToDrive(Uri path, String mimeType, Integer version){
        try {
            Metadata metadata = fileExistInDriveAppFolder(path.getLastPathSegment(), mimeType);
            if (metadata != null) {
                updateFileInDriveAppFolder(metadata.getDriveId().asDriveFile(), path, version);
            } else {
                createFileInDriveAppFolder(path, mimeType, version);
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Metadata fileExistInDriveAppFolder(String name, String mimeType) throws Exception{
        Query query = new Query.Builder().addFilter(Filters.and(
                Filters.eq(SearchableField.MIME_TYPE, mimeType),
                Filters.eq(SearchableField.TITLE, name))).build();
        DriveApi.MetadataBufferResult mdResultSet =
                Drive.DriveApi.getAppFolder(getMyApplication().getGoogleApiClient())
                        .queryChildren(getMyApplication().getGoogleApiClient(), query).await();
        if(!mdResultSet.getStatus().isSuccess()){
            mdResultSet.release();
            throw (new Exception("RukiaSoft: error checking if a file exists in Drive"));
        }

        for(int i = 0; i< mdResultSet.getMetadataBuffer().getCount(); i++){
            Metadata metadata = mdResultSet.getMetadataBuffer().get(i);
            if(!metadata.isTrashed() && metadata.isInAppFolder()){
                return metadata;
            }
        }
        return null;
    }

    private void createFileInDriveAppFolder(Uri path, String mimeType, Integer version) throws Exception {
        DriveApi.DriveContentsResult resultContent = Drive.DriveApi.newDriveContents(getMyApplication().getGoogleApiClient()).await();
        if (!resultContent.getStatus().isSuccess()) {
            throw ( new Exception("RukiaSoft: error creating file in Drive"));
        }
        final DriveContents driveContents = resultContent.getDriveContents();

        FileInputStream fileInputStream;
        OutputStream outputStream = driveContents.getOutputStream();
        try {
            fileInputStream = new FileInputStream(new File(path.getPath()));
            IOUtils.copy(fileInputStream, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw ( new Exception("RukiaSoft: error creating file in Drive"));
        }
        String name = path.getLastPathSegment();
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(name)
                .setMimeType(mimeType)
                .setCustomProperty(KEY_VERSION, version.toString())
                .setLastViewedByMeDate(new Date())
                .build();

        // create a file on root folder
        DriveFolder.DriveFileResult result2 = Drive.DriveApi.getAppFolder(getMyApplication().getGoogleApiClient())
                .createFile(getMyApplication().getGoogleApiClient(), changeSet, driveContents)
                .await();
        if (!result2.getStatus().isSuccess()) {
            throw ( new Exception("RukiaSoft: error creating file in Drive"));
        }
    }

    private void updateFileInDriveAppFolder(DriveFile file, Uri path, Integer version) throws Exception{
        DriveApi.DriveContentsResult contentsResult = file.open(getMyApplication().getGoogleApiClient(),
                DriveFile.MODE_WRITE_ONLY, null).await();
        if (!contentsResult.getStatus().isSuccess()) {
            throw ( new Exception("RukiaSoft: error updating file in Drive"));
        }

        try {
            DriveContents driveContents = contentsResult.getDriveContents();
            FileInputStream fileInputStream;
            OutputStream outputStream = driveContents.getOutputStream();
            fileInputStream = new FileInputStream(new File(path.getPath()));
            IOUtils.copy(fileInputStream, outputStream);

            //Tools mTools = new Tools();
            //String date = mTools.getCurrentDate(getApplicationContext());
            DriveResource.MetadataResult metadataResult = file.getMetadata(getMyApplication().getGoogleApiClient()).await();
            Integer metadataVersion = getVersionFromMetadata(metadataResult.getMetadata());
            if(metadataVersion >= version){
                return;
            }
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setCustomProperty(KEY_VERSION, version.toString())
                    .setLastViewedByMeDate(new Date())
                    .build();
            Status status =
                    driveContents.commit(getMyApplication().getGoogleApiClient(), changeSet).await();
            if (!status.getStatus().isSuccess()){
                throw ( new Exception("RukiaSoft: error creating file in Drive"));
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw ( new Exception("RukiaSoft: error creating file in Drive"));
        }
    }

    private void handleActionDeleteRecipe(RecipeItem recipeItem) {
        Uri uriRecipe = Uri.parse(recipeItem.getPathRecipe());
        String name = uriRecipe.getLastPathSegment().replace("xml", "zip");
        //check if exists
        try {
            Metadata metadata = fileExistInDriveAppFolder(name, Constants.MIME_TYPE_ZIP);
            if (metadata != null) {
                Status result = metadata.getDriveId().asDriveFile().delete(getMyApplication().getGoogleApiClient()).await();
                if(result.isSuccess()){
                    Log.d(TAG, "borrado con éxito");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            Tools mTools = new Tools();
            ACRA.getErrorReporter().handleSilentException(e);
        }
    }

    /**
     * Handle action GetRecipesFromDrive in the provided background thread with the provided
    * parameters.
    */
    private void handleActionGetRecipesFromDrive() {
        mHasMore = true;
        List<Metadata> files = new ArrayList<>();
        while (mHasMore) {
            files.addAll(retrieveNextPage());
        }
        if(files.size()>0){
            checkFilesToDownload(files);
        }
    }


    private List<Metadata> retrieveNextPage() {
        // if there are no more results to retrieve,
        // return silently.
        List<Metadata> list = new ArrayList<>();
        if (!mHasMore) {
            return list;
        }
        // retrieve the results for the next page.
        Query query = new Query.Builder()
                .setPageToken(mNextPageToken)
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, Constants.MIME_TYPE_ZIP))
                .build();
        DriveApi.MetadataBufferResult result = Drive.DriveApi.query(getMyApplication().getGoogleApiClient(), query).await();
        if (!result.getStatus().isSuccess()) {
            mHasMore = false;
            return list;
        }

        for(int i = 0; i< result.getMetadataBuffer().getCount(); i++){
            Metadata metadata = result.getMetadataBuffer().get(i);
            if(!metadata.isTrashed() /*&& metadata.isInAppFolder()*/) {
                list.add(result.getMetadataBuffer().get(i));
            }
        }
        mNextPageToken = result.getMetadataBuffer().getNextPageToken();
        mHasMore = mNextPageToken != null;
        return list;
    }

    private void checkFilesToDownload(List<Metadata> files){
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools();
        ReadWriteTools rwTools = new ReadWriteTools();
        //download recipes if needed
        for(int i=0; i<files.size(); i++){
            String name = files.get(i).getTitle().replace("zip", "xml");
            RecipeItem recipeItem = dbTools.getRecipeByFileName(getApplicationContext(), name);
            Integer driveVersion = getVersionFromMetadata(files.get(i));
            if(recipeItem == null || recipeItem.getVersion() < driveVersion){
                if(downloadFile(files.get(i))){
                    rwTools.unzipRecipesInEdited(getApplicationContext(), files.get(i).getTitle());
                    rwTools.loadUpdatedFilesAndInsertInDatabase(getApplicationContext(), name, driveVersion);
                    rwTools.deleteZipByName(getApplicationContext(), files.get(i).getTitle());
                }
            }
        }
        //check if is needed to delete
        List<RecipeItem> synchronizedRecipes = dbTools.getRecipesByState(getApplicationContext(),
                Constants.FLAG_SINCRONIZED_WITH_DRIVE);
        for(RecipeItem recipeItem : synchronizedRecipes){
            if(!checkIfIsInDrive(recipeItem.getPathRecipe(), files)){
                Intent localIntent = new Intent(Constants.ACTION_BROADCAST_DELETED_RECIPE)
                        // Puts the status into the Intent
                        .putExtra(Constants.KEY_RECIPE, recipeItem);
                // Broadcasts the Intent to receivers in this app.
                LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

            }
        }
    }

    private boolean checkIfIsInDrive(String name, List<Metadata> files){
         Iterator<Metadata> iterator = files.iterator();
        while (iterator.hasNext()){
            String driveName = iterator.next().getTitle();
            if(driveName.contains("zip")){
                driveName = driveName.replace("zip", "");
            }
            if(name.contains(driveName)){
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    private int getVersionFromMetadata(Metadata metadata){
        Map<CustomPropertyKey, String> customProperties = metadata.getCustomProperties();
        Integer version = 0;
        if(customProperties.containsKey(KEY_VERSION)){
            try {
                version = Integer.valueOf(customProperties.get(KEY_VERSION));
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        return version;
    }

    private boolean downloadFile(Metadata metadata){
        DriveFile file = metadata.getDriveId().asDriveFile();
        DriveApi.DriveContentsResult driveContentsResult =
                file.open(getMyApplication().getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
        if (!driveContentsResult.getStatus().isSuccess()) {
            return false;
        }
        DriveContents driveContents = driveContentsResult.getDriveContents();


        FileOutputStream fileOutputStream;
        InputStream inputStream = driveContents.getInputStream();
        try {
            ReadWriteTools rwTools = new ReadWriteTools();
            String path = rwTools.getZipsStorageDir(getApplicationContext()) + metadata.getOriginalFilename();
            fileOutputStream = new FileOutputStream(new File(path));
            IOUtils.copy(inputStream, fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return true;
    }
}
