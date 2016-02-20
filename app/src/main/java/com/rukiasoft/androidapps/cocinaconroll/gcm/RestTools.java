package com.rukiasoft.androidapps.cocinaconroll.gcm;

import com.rukiasoft.androidapps.cocinaconroll.BuildConfig;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.Proxy;

/**
 * Created by Ruler on 20/2/16.
 *
 */
public class RestTools {


    public RestTools(){

    }

    public Response doRestRequest(String urlBase, String method, String params){
        OkHttpClient client = new OkHttpClient();
        String url = urlBase.concat(method);

        // Basic Authentication
        client.setAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) throws IOException {
                String credential = Credentials.basic(BuildConfig.REST_LOGIN_KEY, BuildConfig.REST_PASSWORD_KEY);
                return response.request().newBuilder().header("Authorization", credential).build();
            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                return null;
            }
        });

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        Request.Builder builder = new Request.Builder()
                .url(url);
        if(params != null){
            RequestBody body = RequestBody.create(JSON, params);
            builder.post(body);
        }
        Request request = builder.build();
        try {
            return client.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
