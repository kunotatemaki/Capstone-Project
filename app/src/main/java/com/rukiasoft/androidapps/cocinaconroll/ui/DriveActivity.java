package com.rukiasoft.androidapps.cocinaconroll.ui;

import android.app.Activity;
import android.content.IntentSender;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.api.client.util.IOUtils;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by iRuler on 10/11/15.
 */
public class DriveActivity extends ToolbarAndRefreshActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = LogHelper.makeLogTag(DriveActivity.class);
    private static final String DATABASE_DIR = "databases";
    private static final String RECIPES_DIR = "recipes";
    private static final CustomPropertyKey KEY_MODIFIED = new CustomPropertyKey("modified", CustomPropertyKey.PRIVATE);

    private Activity mActivity;


    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;


    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    protected DriveActivity(){
        mActivity = this;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "API client connected.");
        /*Tools mTools = new Tools();
        if(!mTools.getBooleanFromPreferences(mActivity, Constants.PROPERTY_DRIVE_FOLDER_TREE_CREATED)){
            createFolderTree();
        }*/
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // TODO: 11/11/15 mirar lo de estar sin conexión para que no mande todo el rato la pantalla
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + connectionResult.toString());
        if (!connectionResult.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.

        try {
            connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
            //shownToAllowDrive = true;
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }

    }

    private boolean checkIfCloudBackupAllowed(){
        Tools tools = new Tools();
        return tools.getBooleanFromPreferences(this, "option_cloud_backup");
    }

    protected boolean connectToDrive(boolean check){
        if(check && !checkIfCloudBackupAllowed()) {
            return false;
        }
        if (mGoogleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient.connect();
        return true;
    }

    /**
     * Called when activity gets visible. A connection to Drive services need to
     * be initiated as soon as the activity is visible. Registers
     * {@code ConnectionCallbacks} and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    protected void onResume() {
        super.onResume();
        connectToDrive(true);
    }

    /**
     * Called when activity gets invisible. Connection to Drive service needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }



    /**
     * Getter for the {@code GoogleApiClient}.
     */
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }


    private void createFolderTree(){
        if(!checkIfCloudBackupAllowed()) return;
        CreateFolderTask task = new CreateFolderTask();
        task.execute();
    }

    private class CreateFolderTask extends AsyncTask<Void, Integer, Boolean> {
        protected Boolean doInBackground(Void... params) {
            //busco si existen los directorios y si no, los creo
            //database dir
            return (createFolder(DATABASE_DIR) & createFolder(RECIPES_DIR));

        }

        protected void onPostExecute(Boolean result) {
            Tools mTools = new Tools();
            mTools.savePreferences(mActivity, Constants.PROPERTY_DRIVE_FOLDER_TREE_CREATED, result);
        }
    }

    private boolean createFolder(String name){
        Query query = new Query.Builder().addFilter(Filters.and(
                Filters.eq(SearchableField.MIME_TYPE, "application/vnd.google-apps.folder"),
                Filters.eq(SearchableField.TITLE, name))).build();
        DriveApi.MetadataBufferResult mdResultSet = Drive.DriveApi.getAppFolder(getGoogleApiClient()).queryChildren(getGoogleApiClient(), query).await();
        if(!mdResultSet.getStatus().isSuccess()){
            return false;
        }

        int nFolders = 0;
        for(int i = 0; i< mdResultSet.getMetadataBuffer().getCount(); i++){
            Metadata metadata = mdResultSet.getMetadataBuffer().get(i);
            if(!metadata.isTrashed()){
                nFolders++;
            }
        }

        if(nFolders == 0){
            //no existe ningún fichero con ese nombre, lo creo
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(name)
                    .build();
            DriveFolder.DriveFolderResult result = Drive.DriveApi.getAppFolder(getGoogleApiClient()).createFolder(
                    getGoogleApiClient(), changeSet).await();
            if(!result.getStatus().isSuccess()){
                return false;
            }else{
                return true;
            }
        }
        return true;
    }
    public void uploadFileToDrive(String path){

        UploadFileToDriveTask task = new UploadFileToDriveTask();
        Uri uri = Uri.parse(path);
        task.execute(uri);

    }

    private class UploadFileToDriveTask extends AsyncTask<Uri, Integer, Boolean> {
        protected Boolean doInBackground(Uri... uris) {

            //compruebo si el archivo existe.
            if(uris.length <= 0)    return false;
            try {
                DriveFile file = fileExistInDriveAppFolder(uris[0].getLastPathSegment());
                if (file != null) {
                    updateFileInDriveAppFolder(file, uris[0]);
                } else {
                    createFileInDriveAppFolder(uris[0]);
                }
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            // TODO: 11/11/15 show in snackbar con true y false;
        }
    }

    private DriveFile fileExistInDriveAppFolder(String name) throws Exception{
        Query query = new Query.Builder().addFilter(Filters.and(
                Filters.eq(SearchableField.MIME_TYPE, "application/xml"),
                Filters.eq(SearchableField.TITLE, name))).build();
        DriveApi.MetadataBufferResult mdResultSet = Drive.DriveApi.getRootFolder(getGoogleApiClient()).queryChildren(getGoogleApiClient(), query).await();
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

    private void createFileInDriveAppFolder(Uri path) throws Exception {
        DriveApi.DriveContentsResult resultContent = Drive.DriveApi.newDriveContents(getGoogleApiClient()).await();
        if (!resultContent.getStatus().isSuccess()) {
            throw ( new Exception("RukiaSoft: error creating file in Drive"));
        }
        final DriveContents driveContents = resultContent.getDriveContents();

        FileInputStream fileInputStream;
        OutputStream outputStream = driveContents.getOutputStream();
        try {
            fileInputStream = new FileInputStream(new File(path.getPath()));
            IOUtils.copy(fileInputStream, outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw ( new Exception("RukiaSoft: error creating file in Drive"));
        } catch (IOException e) {
            e.printStackTrace();
            throw ( new Exception("RukiaSoft: error creating file in Drive"));
        }
        String name = path.getLastPathSegment();
        Tools mTools = new Tools();
        String date = mTools.getCurrentDate(mActivity);
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(name)
                .setMimeType("application/xml")
                .setCustomProperty(KEY_MODIFIED, date)
                .setLastViewedByMeDate(new Date())
                .build();

        // create a file on root folder
        DriveFolder.DriveFileResult result2 = Drive.DriveApi.getRootFolder(getGoogleApiClient())
                .createFile(getGoogleApiClient(), changeSet, driveContents)
                .await();
        if (!result2.getStatus().isSuccess()) {
            throw ( new Exception("RukiaSoft: error creating file in Drive"));
        }
    }

    private void updateFileInDriveAppFolder(DriveFile file, Uri path) throws Exception{
        DriveApi.DriveContentsResult contentsResult = file.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).await();
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
            String date = mTools.getCurrentDate(mActivity);
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setCustomProperty(KEY_MODIFIED, date)
                    .setLastViewedByMeDate(new Date())
                    .build();

            Status status =
                    driveContents.commit(getGoogleApiClient(), changeSet).await();
            if (!status.getStatus().isSuccess()){
                throw ( new Exception("RukiaSoft: error creating file in Drive"));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw ( new Exception("RukiaSoft: error creating file in Drive"));
        } catch (IOException e) {
            e.printStackTrace();
            throw ( new Exception("RukiaSoft: error creating file in Drive"));
        }

    }

    boolean mHasMore = true; // initial request assumes there are files results.
    private String mNextPageToken;

    public void getRecipesInDrive(){
        getRecipesInDriveTask task = new getRecipesInDriveTask();
        task.execute();
    }

    private class getRecipesInDriveTask extends AsyncTask<Void, Integer, Void> {
        protected Void doInBackground(Void... params) {
            while (mHasMore) {
                retrieveNextPage();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            // TODO: 11/11/15 show in snackbar con true y false;
        }
    }

    private void retrieveNextPage() {
        // if there are no more results to retrieve,
        // return silently.
        if (!mHasMore) {
            return;
        }
        // retrieve the results for the next page.
        Query query = new Query.Builder()
                .setPageToken(mNextPageToken)
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, "application/xml"))
                .build();
        DriveApi.MetadataBufferResult result = Drive.DriveApi.query(getGoogleApiClient(), query).await();
        if (!result.getStatus().isSuccess()) {
            mHasMore = false;
            return;
        }
        List<String> files = new ArrayList<>();
        for(int i = 0; i< result.getMetadataBuffer().getCount(); i++){
            Metadata metadata = result.getMetadataBuffer().get(i);
            if(!metadata.isTrashed()) {
                files.add(metadata.getTitle());
            }
        }
        mNextPageToken = result.getMetadataBuffer().getNextPageToken();
        mHasMore = mNextPageToken != null;
        return;
    }



}

