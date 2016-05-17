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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.rukiasoft.androidapps.cocinaconroll.CocinaConRollApplication;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.gcm.QuickstartPreferences;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * Minimal activity demonstrating basic Google Sign-In.
 */
public class ShowSigningActivity extends SigningDriveActivity {

    private static final String TAG = "ShowSigningActivity";

    /* View to display current status (signed-in, signed-out, disconnected, etc) */
    @BindView(R.id.sign_in_status) TextView mStatus;
    @BindView(R.id.sign_in_button)SignInButton signInButton;
    @BindView(R.id.sign_out_button)Button signOutButton;
    @BindView(R.id.sign_in_discard_button)Button discardButton;
    @BindView(R.id.sign_in_progressbar)ProgressBar mProgressBar;
    private Unbinder unbinder;

    private String accountName;
    private Activity mActivity;


    private CocinaConRollApplication getMyApplication(){
        return (CocinaConRollApplication)getApplication();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signing);
        unbinder = ButterKnife.bind(this);
        mActivity = this;
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
                v.setEnabled(false);
                mProgressBar.setVisibility(View.VISIBLE);
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

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Tools mTools = new Tools();
            mTools.savePreferences(this, Constants.PROPERTY_CLOUD_BACKUP, true);
            mTools.savePreferences(this, Constants.PROPERTY_DEVICE_OWNER_NAME, acct.getDisplayName());
            mTools.savePreferences(this, Constants.PROPERTY_DEVICE_OWNER_EMAIL, acct.getEmail());
            //force to send registration token to server again, with this new information
            mTools.savePreferences(this, QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
            String name = acct.getDisplayName();
            if(name == null){
                name = getResources().getString(R.string.anonymous);
            }
            accountName = getString(R.string.signed_in_fmt, name);
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
            if(result.isSuccess()){
                finish();
            }
            signInButton.setEnabled(true);
            mProgressBar.setVisibility(View.GONE);
        }
    }




    // [START signIn]
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(getMyApplication().getGoogleApiClient());
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    /*private void signOut() {
        Auth.GoogleSignInApi.signOut(getMyApplication().getGoogleApiClient()).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }*/
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        if(getMyApplication().getGoogleApiClient().isConnected()) {
            Auth.GoogleSignInApi.revokeAccess(getMyApplication().getGoogleApiClient()).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            // [START_EXCLUDE]
                            updateUI(false);
                            Tools mTools = new Tools();
                            mTools.savePreferences(mActivity, Constants.PROPERTY_DEVICE_OWNER_EMAIL, "");
                            mTools.savePreferences(mActivity, Constants.PROPERTY_DEVICE_OWNER_NAME, "");

                            // [END_EXCLUDE]
                        }
                    });
        }else{
            Log.d(TAG, "pues no está conectado");
        }
    }
    // [END revokeAccess]


    // [START on_connection_failed]
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);


    }
    // [END on_connection_failed]


}
