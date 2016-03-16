package com.rukiasoft.androidapps.cocinaconroll.ui;

import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.rukiasoft.androidapps.cocinaconroll.CocinaConRollApplication;
import com.rukiasoft.androidapps.cocinaconroll.classes.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.services.DriveService;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

/**
 * Created by iRuler on 10/11/15.
 */
public abstract class SigningDriveActivity extends ToolbarAndRefreshActivity implements /*GoogleApiClient.ConnectionCallbacks,*/
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
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

    protected boolean checkIfCloudBackupAllowed(){
        Tools tools = new Tools();
        return tools.getBooleanFromPreferences(this, Constants.PROPERTY_CLOUD_BACKUP);
    }

    protected void initializeConnection(){
        if (getMyApplication().getGoogleApiClient() == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
                    .build();

            getMyApplication().setGoogleApiClient(new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .addApi(Drive.API)
                    .build());
        }else{
            //getMyApplication().getGoogleApiClient().registerConnectionCallbacks(this);
            if(!getMyApplication().getGoogleApiClient().isConnectionFailedListenerRegistered(this)) {
                getMyApplication().getGoogleApiClient().registerConnectionFailedListener(this);
            }

        }
    }

    protected boolean connectToDrive(boolean check){
        if(check && !checkIfCloudBackupAllowed()) {
            return false;
        }
        initializeConnection();
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


    /**
     * Called when activity gets visible. A connection to Drive services need to
     * be initiated as soon as the activity is visible. Registers
     * {@code ConnectionCallbacks} and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if(getMyApplication() != null){
        //if(getMyApplication() != null) {
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
                //getMyApplication().getGoogleApiClient().unregisterConnectionCallbacks(this);
                getMyApplication().getGoogleApiClient().unregisterConnectionFailedListener(this);
            }
        }
    }

    public void uploadRecipeToDrive(RecipeItem recipeItem){
        if(getMyApplication().getGoogleApiClient() == null){
            //initializeConnection();
            connectToDrive(true);
        }else{
            if(!getMyApplication().getGoogleApiClient().isConnected()) {
                connectToDrive(true);
            }
            DriveService.startActionUploadRecipe(getApplicationContext(), recipeItem);
        }
    }

    protected void deleteRecipeFromDrive(RecipeItem recipeItem){
        if(getMyApplication().getGoogleApiClient() == null){
            //initializeConnection();
            connectToDrive(true);
        }else{
            if(!getMyApplication().getGoogleApiClient().isConnected()) {
                connectToDrive(true);
            }
            DriveService.startActionDeleteRecipe(getApplicationContext(), recipeItem);
        }
    }

    protected boolean getRecipesFromDrive(){
        if(getMyApplication().getGoogleApiClient() == null){
            connectToDrive(true);
        }else {
            if(!getMyApplication().getGoogleApiClient().isConnected()) {
                connectToDrive(true);
            }
            DriveService.startActionGetRecipesFromDrive(getApplicationContext());
            return true;
        }
        return false;
    }
}

