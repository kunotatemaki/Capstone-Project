package com.rukiasoft.androidapps.cocinaconroll.ui;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.plus.Plus;
import com.rukiasoft.androidapps.cocinaconroll.CocinaConRollApplication;
import com.rukiasoft.androidapps.cocinaconroll.classes.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;


/**
 * Created by iRuler on 10/11/15.
 */
public class SigningDriveActivity extends ToolbarAndRefreshActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = LogHelper.makeLogTag(SigningDriveActivity.class);
    /* RequestCode for resolutions involving sign-in */
    protected static final int RC_SIGN_IN = 9001;

    /* Keys for persisting instance variables in savedInstanceState */
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_SHOULD_RESOLVE = "should_resolve";

    /* Is there a ConnectionResult resolution in progress? */
    protected boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    protected boolean mShouldResolve = false;


    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    private CocinaConRollApplication getMyApplication(){
        return (CocinaConRollApplication)getApplication();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "API client connected.");
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
        return tools.getBooleanFromPreferences(this, Constants.PROPERTY_CLOUD_BACKUP);
    }

    protected boolean connectToDrive(boolean check){
        if(check && !checkIfCloudBackupAllowed()) {
            return false;
        }
        if (getMyApplication().getGoogleApiClient() == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            getMyApplication().setGoogleApiClient(new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API)
                    .addScope(new Scope(Scopes.PROFILE))
                    .build());
        }else{
            getMyApplication().getGoogleApiClient().registerConnectionCallbacks(this);
            getMyApplication().getGoogleApiClient().registerConnectionFailedListener(this);
        }
        // Connect the client. Once connected
        if(!getMyApplication().getGoogleApiClient().isConnected()) {
            getMyApplication().getGoogleApiClient().connect();
        }
        return true;
    }

    // [START on_save_instance_state]
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
        outState.putBoolean(KEY_SHOULD_RESOLVE, mShouldResolve);
    }
    // [END on_save_instance_state]

    // [START on_activity_result]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further errors.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            getMyApplication().getGoogleApiClient().connect();
        }
    }
    // [END on_activity_result]



    /**
     * Called when activity gets visible. A connection to Drive services need to
     * be initiated as soon as the activity is visible. Registers
     * {@code ConnectionCallbacks} and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if(getMyApplication() != null) {
            connectToDrive(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getMyApplication() != null){
            getMyApplication().addActivity();
        }
        // [START restore_saved_instance_state]
        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING);
            mShouldResolve = savedInstanceState.getBoolean(KEY_SHOULD_RESOLVE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getMyApplication() != null) {
            getMyApplication().popActivity();
            if(getMyApplication().getGoogleApiClient() != null){
                getMyApplication().getGoogleApiClient().unregisterConnectionCallbacks(this);
                getMyApplication().getGoogleApiClient().unregisterConnectionFailedListener(this);
            }
        }
    }

    protected void uploadRecipeToDrive(RecipeItem recipeItem){
        if(getMyApplication().getGoogleApiClient() == null || !getMyApplication().getGoogleApiClient().isConnected()){
            connectToDrive(true);
        }else {
            DriveService.startActionUploadRecipe(this, recipeItem);
        }
    }

    protected void getRecipesFromDrive(){
        if(getMyApplication().getGoogleApiClient() == null || !getMyApplication().getGoogleApiClient().isConnected()){
            connectToDrive(true);
        }else {
            DriveService.startActionGetRecipesFromDrive(this);
        }
    }


    /*private static final String DATABASE_DIR = "databases";
    private static final String RECIPES_DIR = "recipes";*/

    /*private void createFolderTree(){
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
    }*/


}

