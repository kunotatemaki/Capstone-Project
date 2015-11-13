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

import android.content.DialogInterface;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.rukiasoft.androidapps.cocinaconroll.CocinaConRollApplication;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;



/**
 * Minimal activity demonstrating basic Google Sign-In.
 */
public class ShowSigningActivity extends SigningDriveActivity {

    private static final String TAG = "ShowSigningActivity";

    /* View to display current status (signed-in, signed-out, disconnected, etc) */
    TextView mStatus;
    //SignInButton signInButton;
    Button discardButton;

    private CocinaConRollApplication getMyApplication(){
        return (CocinaConRollApplication)getApplication();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signing);

        mStatus = (TextView) findViewById(R.id.sign_status);
        //signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        discardButton = (Button) findViewById(R.id.sign_discard_button);
        // Restore from saved instance state

        // [END restore_saved_instance_state]

        // Set up button click listeners
        ((SignInButton) findViewById(R.id.sign_in_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // User clicked the sign-in button, so begin the sign-in process and automatically
                // attempt to resolve any errors that occur.
                mStatus.setText(R.string.signing_in);
                // [START sign_in_clicked]
                mShouldResolve = true;
                getMyApplication().getGoogleApiClient().connect();
                // [END sign_in_clicked]
            }
        });
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Large sign-in
        ((SignInButton) findViewById(R.id.sign_in_button)).setSize(SignInButton.SIZE_WIDE);

        // Start with sign-in button disabled until sign-in either succeeds or fails
        ((SignInButton) findViewById(R.id.sign_in_button)).setEnabled(false);


    }

    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            // Show signed-in user's name
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(getMyApplication().getGoogleApiClient());
            if (currentPerson != null) {
                String name = currentPerson.getDisplayName();
                mStatus.setText(getString(R.string.signed_in_fmt, name));
                Tools mTools = new Tools();
                mTools.savePreferences(this, "option_cloud_backup", true);
            } else {
                Log.w(TAG, getString(R.string.error_null_person));
                mStatus.setText(getString(R.string.signed_in_err));
            }

            // Set button visibility
            ((SignInButton) findViewById(R.id.sign_in_button)).setVisibility(View.GONE);
            discardButton.setText(getString(R.string.exit));
        } else {
            // Show signed-out message
            mStatus.setText(R.string.signed_out);

            // Set button visibility
            ((SignInButton) findViewById(R.id.sign_in_button)).setEnabled(true);
            ((SignInButton) findViewById(R.id.sign_in_button)).setVisibility(View.VISIBLE);
        }
    }

    // [START on_start_on_stop]
    /*@Override
    protected void onStart() {
        super.onStart();
        getMyApplication().getGoogleApiClient().connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getMyApplication().getGoogleApiClient().disconnect();
    }
    // [END on_start_on_stop]*/





    @Override
    public void onStart(){
        super.onStart();
        connectToDrive(false);
    }

    @Override public void onDestroy() {
        super.onDestroy();
    }

    @Override
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
    }

    // [START on_connection_failed]
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
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
        }
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
