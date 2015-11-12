package com.rukiasoft.androidapps.cocinaconroll.ui;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.api.client.util.IOUtils;
import com.rukiasoft.androidapps.cocinaconroll.CocinaConRollApplication;
import com.rukiasoft.androidapps.cocinaconroll.classes.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class DriveService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS

    private static final String EXTRA_PARAM_RECIPE = "com.rukiasoft.androidapps.cocinaconroll.ui.extra.RECIPE";

    private static final String MIME_TYPE_RECIPE = "application/xml";
    private static final String MIME_TYPE_PICTURE = "image/jpeg";

    private static final CustomPropertyKey KEY_MODIFIED = new CustomPropertyKey("modified", CustomPropertyKey.PRIVATE);

    private boolean mHasMore;
    private String mNextPageToken;

    public DriveService() {
        super("DriveService");
    }

    private CocinaConRollApplication getMyApplication(){
        return (CocinaConRollApplication)getApplication();
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     */
    public static void startActionUploadRecipe(Context context, RecipeItem recipeItem) {

        Intent intent = new Intent(context, DriveService.class);
        intent.setAction(Constants.ACTION_UPLOAD_RECIPE);
        intent.putExtra(EXTRA_PARAM_RECIPE, recipeItem);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
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
        UploadFileToDrive(uriRecipe, MIME_TYPE_RECIPE);
        Uri uriPicture;
        if(!recipeItem.getPathPicture().equals(Constants.DEFAULT_PICTURE_NAME)){
            uriPicture = Uri.parse(recipeItem.getPathPicture());
            //UploadFileToDrive(uriPicture, MIME_TYPE_PICTURE);
        }
    }

    private boolean UploadFileToDrive(Uri path, String mimeType){
        try {
            DriveFile file = fileExistInDriveAppFolder(path.getLastPathSegment(), mimeType);
            if (file != null) {
                updateFileInDriveAppFolder(file, path);
            } else {
                createFileInDriveAppFolder(path, mimeType);
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private DriveFile fileExistInDriveAppFolder(String name, String mimeType) throws Exception{
        Query query = new Query.Builder().addFilter(Filters.and(
                Filters.eq(SearchableField.MIME_TYPE, mimeType),
                Filters.eq(SearchableField.TITLE, name))).build();
        DriveApi.MetadataBufferResult mdResultSet =
                Drive.DriveApi.getRootFolder(getMyApplication().getGoogleApiClient())
                        .queryChildren(getMyApplication().getGoogleApiClient(), query).await();
        if(!mdResultSet.getStatus().isSuccess()){
            throw (new Exception("RukiaSoft: error checking if a file exists in Drive"));
        }

        for(int i = 0; i< mdResultSet.getMetadataBuffer().getCount(); i++){
            Metadata metadata = mdResultSet.getMetadataBuffer().get(i);
            if(!metadata.isTrashed()){
                return metadata.getDriveId().asDriveFile();
            }
        }

        return null;
    }

    private void createFileInDriveAppFolder(Uri path, String mimeType) throws Exception {
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
        Tools mTools = new Tools();
        String date = mTools.getCurrentDate(getApplicationContext());
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(name)
                .setMimeType(mimeType)
                .setCustomProperty(KEY_MODIFIED, date)
                .setLastViewedByMeDate(new Date())
                .build();

        // create a file on root folder
        DriveFolder.DriveFileResult result2 = Drive.DriveApi.getRootFolder(getMyApplication().getGoogleApiClient())
                .createFile(getMyApplication().getGoogleApiClient(), changeSet, driveContents)
                .await();
        if (!result2.getStatus().isSuccess()) {
            throw ( new Exception("RukiaSoft: error creating file in Drive"));
        }
    }

    private void updateFileInDriveAppFolder(DriveFile file, Uri path) throws Exception{
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

            Tools mTools = new Tools();
            String date = mTools.getCurrentDate(getApplicationContext());
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setCustomProperty(KEY_MODIFIED, date)
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
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, MIME_TYPE_RECIPE))
                .build();
        DriveApi.MetadataBufferResult result = Drive.DriveApi.query(getMyApplication().getGoogleApiClient(), query).await();
        if (!result.getStatus().isSuccess()) {
            mHasMore = false;
            return list;
        }

        for(int i = 0; i< result.getMetadataBuffer().getCount(); i++){
            list.add(result.getMetadataBuffer().get(i));
        }
        mNextPageToken = result.getMetadataBuffer().getNextPageToken();
        mHasMore = mNextPageToken != null;
        return list;
    }
}
