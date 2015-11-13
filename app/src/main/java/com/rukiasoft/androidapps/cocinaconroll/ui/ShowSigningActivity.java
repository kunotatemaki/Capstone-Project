/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rukiasoft.androidapps.cocinaconroll.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.rukiasoft.androidapps.cocinaconroll.CocinaConRollApplication;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.ReadWriteTools;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Minimal activity demonstrating basic Google Sign-In.
 */
public class ShowSigningActivity extends SigningDriveActivity {

    private static final String TAG = "ShowSigningActivity";

    /* View to display current status (signed-in, signed-out, disconnected, etc) */
    @Bind(R.id.sign_in_status) TextView mStatus;
    @Bind(R.id.sign_in_button)SignInButton signInButton;
    @Bind(R.id.sign_out_button)Button signOutButton;
    @Bind(R.id.sign_in_discard_button)Button discardButton;
    @Bind(R.id.sign_in_icon)ImageView signInIcon;

    private ProgressDialog mProgressDialog;
    private String accountName;


    private CocinaConRollApplication getMyApplication(){
        return (CocinaConRollApplication)getApplication();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signing);
        ButterKnife.bind(this);
        // Restore from saved instance state

        // [END restore_saved_instance_state]

        // Set up button click listeners
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*// User clicked the sign-in button, so begin the sign-in process and automatically
                // attempt to resolve any errors that occur.
                mStatus.setText(R.string.signing_in);
                // [START sign_in_clicked]
                mShouldResolve = true;
                getMyApplication().getGoogleApiClient().connect();
                // [END sign_in_clicked]*/
                signIn();
            }
        });
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revokeAccess();
            }
        });

        // Large sign-in
        signInButton.setSize(SignInButton.SIZE_WIDE);
        //signOutButton.setVisibility(View.GONE);
        // Start with sign-in button disabled until sign-in either succeeds or fails
        signInButton.setEnabled(false);


    }

    private void updateUI(boolean isSignedIn) {
        /*if (isSignedIn) {
            // Show signed-in user's name
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(getMyApplication().getGoogleApiClient());
            if (currentPerson != null) {
                String name = currentPerson.getDisplayName();
                mStatus.setText(getString(R.string.signed_in_fmt, name));
                Tools mTools = new Tools();
                mTools.savePreferences(this, Constants.PROPERTY_CLOUD_BACKUP, true);
                ReadWriteTools rwTools = new ReadWriteTools(this);
                rwTools.loadImageFromPathInCircle(signInIcon, currentPerson.getImage().getUrl(), R.mipmap.ic_launcher);

            } else {
                Log.w(TAG, getString(R.string.error_null_person));
                mStatus.setText(getString(R.string.signed_in_err));
            }

            // Set button visibility
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
            discardButton.setText(getString(R.string.exit));
        } else {
            // Show signed-out message
            mStatus.setText(R.string.signed_out);

            // Set button visibility
            signInButton.setEnabled(true);
            signInButton.setVisibility(View.VISIBLE);
        }*/

        if (isSignedIn) {
            mStatus.setText(accountName);
            discardButton.setText(getString(R.string.exit));
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
        } else {
            mStatus.setText(R.string.signed_out);
            discardButton.setText(getString(R.string.sign_in_discard));
            signInButton.setVisibility(View.VISIBLE);
            signInButton.setEnabled(true);
            signOutButton.setVisibility(View.GONE);
        }
    }



    @Override
    public void onStart(){
        super.onStart();
        //connectToDrive(false);
        initializeConnection();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(getMyApplication().getGoogleApiClient());
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            updateUI(false);
            /*showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });*/
        }
    }

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Tools mTools = new Tools();
            mTools.savePreferences(this, Constants.PROPERTY_CLOUD_BACKUP, true);
            accountName = getString(R.string.signed_in_fmt, acct.getDisplayName());
            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }
    // [END onActivityResult]

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    // [START signIn]
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(getMyApplication().getGoogleApiClient());
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        Auth.GoogleSignInApi.signOut(getMyApplication().getGoogleApiClient()).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        if(getMyApplication().getGoogleApiClient().isConnected()) {
            Auth.GoogleSignInApi.revokeAccess(getMyApplication().getGoogleApiClient()).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            // [START_EXCLUDE]
                            updateUI(false);
                            // [END_EXCLUDE]
                        }
                    });
        }else{
            Log.d(TAG, "pues no está conectado");
        }
    }
    // [END revokeAccess]

    /*@Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        Log.d(TAG, "onConnected:" + bundle);

        // Show the signed-in UI
        updateUI(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost. The GoogleApiClient will automatically
        // attempt to re-connect. Any UI elements that depend on connection to Google APIs should
        // be hidden or disabled until onConnected is called again.
        Log.w(TAG, "onConnectionSuspended:" + i);
    }*/

    // [START on_connection_failed]
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        /*if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    getMyApplication().getGoogleApiClient().connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.
                showErrorDialog(connectionResult);
            }
        } else {
            // Show the signed-out UI
            updateUI(false);
        }*/
    }
    // [END on_connection_failed]

    private void showErrorDialog(ConnectionResult connectionResult) {
        int errorCode = connectionResult.getErrorCode();

        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            // Show the default Google Play services error dialog which may still start an intent
            // on our behalf if the user can resolve the issue.
            GooglePlayServicesUtil.getErrorDialog(errorCode, this, RC_SIGN_IN,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mShouldResolve = false;
                            updateUI(false);
                        }
                    }).show();
        } else {
            // No default Google Play Services error, display a message to the user.
            String errorString = getString(R.string.play_services_error_fmt, errorCode);
            Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();

            mShouldResolve = false;
            updateUI(false);
        }
    }


}
