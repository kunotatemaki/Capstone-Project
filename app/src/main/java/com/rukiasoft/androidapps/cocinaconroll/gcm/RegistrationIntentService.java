/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rukiasoft.androidapps.cocinaconroll.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;
import com.rukiasoft.androidapps.cocinaconroll.BuildConfig;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.classes.RegistrationClass;
import com.rukiasoft.androidapps.cocinaconroll.classes.RegistrationResponse;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;
import com.squareup.okhttp.Response;

import java.net.HttpURLConnection;


public class RegistrationIntentService extends IntentService {

    private static final String TAG = LogHelper.makeLogTag(RegistrationIntentService.class);


    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            LogHelper.i(TAG, "GCM Registration Token: " + token);


            if(sendRegistrationToServer(token)) {

                // Subscribe to topic channels
                //subscribeTopics(token);

                // You should store a boolean that indicates whether the generated token has been
                // sent to your server. If the boolean is false, send the token to your server,
                // otherwise your server should have already received the token.
                sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
                // [END register_for_gcm]
            }else{
                Log.i(TAG, "token no enviado");
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        //Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private boolean sendRegistrationToServer(String token) {
        // this will register device for testing porposes.
        /*if (regService == null) {
            //uncomment for testing local registration for emulators
            Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // Need setRootUrl and setGoogleClientRequestInitializer only for local testing,
                    // otherwise they can be skipped
                    .setRootUrl("http://10.0.2.2:8080/_ah/api/")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
                                throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
            // end of optional local run code

            //uncomment for testing appEngine with real device. URL: http://hardy-binder-89508.appspot.com/
            //Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
            //        .setRootUrl("https://hardy-binder-89508.appspot.com/_ah/api/");

            regService = builder.build();
        }*/

        RegistrationResponse error = new RegistrationResponse();

        Tools mTools = new Tools();

        RegistrationClass registrationClass = new RegistrationClass(this);

        registrationClass.setGcm_regid(token);
        registrationClass.setVersion(mTools.getAppVersion(getApplication()));
        registrationClass.setEmail(mTools.getStringFromPreferences(this, Constants.PROPERTY_DEVICE_OWNER_EMAIL));
        registrationClass.setName(mTools.getStringFromPreferences(this, Constants.PROPERTY_DEVICE_OWNER_NAME));



        String urlBase = BuildConfig.RASPBERRY_IP + getResources().getString(R.string.server_url_tomcat);
        String method = getResources().getString(R.string.registration_method);

        RestTools restTools = new RestTools();
        Response response = restTools.doRestRequest(urlBase, method, mTools.getJsonString(registrationClass));

        if(response != null && response.code() == HttpURLConnection.HTTP_OK) {
            LogHelper.i(TAG, "comprobación Registrado correctamente");
            Gson gResponse = new Gson();
            error = gResponse.fromJson(response.body().charStream(), RegistrationResponse.class);
        }else{
            LogHelper.i(TAG, "comprobación NO registrado");
        }

        return error.getError() == 0;
    }



}
