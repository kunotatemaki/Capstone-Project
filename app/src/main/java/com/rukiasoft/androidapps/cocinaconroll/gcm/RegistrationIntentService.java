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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.classes.RegistrationClass;
import com.rukiasoft.androidapps.cocinaconroll.classes.RegistrationResponse;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;


public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";


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
            Log.i(TAG, "GCM Registration Token: " + token);


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
        try {

            Tools mTools = new Tools();

            RegistrationClass registrationClass = new RegistrationClass(this);

            registrationClass.setGcm_regid(token);
            registrationClass.setVersion(mTools.getAppVersion(getApplication()));
            registrationClass.setEmail(mTools.getStringFromPreferences(this, Constants.PROPERTY_DEVICE_OWNER_EMAIL));
            registrationClass.setName(mTools.getStringFromPreferences(this, Constants.PROPERTY_DEVICE_OWNER_NAME));


            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            OkHttpClient client = new OkHttpClient();

            String urlBase = getResources().getString(R.string.server_url);
            String method = getResources().getString(R.string.registration_method);
            String url = urlBase.concat(method);

            RequestBody body = RequestBody.create(JSON, mTools.getJsonString(registrationClass));

            /////////////////////////////////////////
            client.setAuthenticator(new Authenticator() {
                @Override
                public Request authenticate(Proxy proxy, Response response) throws IOException {
                    String credential = Credentials.basic("ruler", "rukia");
                    return response.request().newBuilder().header("Authorization", credential).build();
                }

                @Override
                public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                    return null;
                }
            });
            url = "http://comunioelpuntal.no-ip.biz:8080/comunio-server/rest/comunio_server_secure/get_page_data";
            JsonObject jObject = new JsonObject();
            jObject.addProperty("name", "all");
            jObject.addProperty("type", "null");
            JsonObject object = new JsonObject();
            object.add("Names", jObject);
            body = RequestBody.create(JSON, mTools.getJsonString(jObject));
            /////////////////////////////////////////////



            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            if(response.code() == HttpURLConnection.HTTP_OK) {
                JsonParser jsonParser = new JsonParser();
                JsonObject jo = (JsonObject) jsonParser.parse(response.body().charStream());
                jo.addProperty("hola", true);
            }else{
                Log.d("cretino", "tacatá");
            }
            Gson gResponse = new Gson();
            error = gResponse.fromJson(response.body().charStream(), RegistrationResponse.class);


            //regService.register(regId).execute();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return error.getError() == 0;
    }



}
