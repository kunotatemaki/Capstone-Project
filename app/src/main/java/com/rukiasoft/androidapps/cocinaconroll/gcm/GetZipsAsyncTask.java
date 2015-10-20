package com.rukiasoft.androidapps.cocinaconroll.gcm;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.classes.RegistrationResponse;
import com.rukiasoft.androidapps.cocinaconroll.classes.ZipItem;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.XMLFormatter;

/**
 * Created by iRuler on 20/10/15.
 */
public class GetZipsAsyncTask extends AsyncTask<Void, Void, List<ZipItem>> {
    private final Context myContext;

    public GetZipsAsyncTask(Context context){
        myContext = context;
    }
    protected List<ZipItem> doInBackground(Void... params) {
        List<ZipItem> list = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();

        String urlBase = myContext.getResources().getString(R.string.server_url);
        String method = myContext.getResources().getString(R.string.get_zips_method);
        String url = urlBase.concat(method);

        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            JsonParser jsonParser = new JsonParser();
            JsonObject jo = (JsonObject)jsonParser.parse(response.body().charStream());

            JsonArray jsonArr = jo.getAsJsonArray("zips");
            for(JsonElement gson : jsonArr ) {
                ZipItem zipItem = new Gson().fromJson(gson.getAsString(), ZipItem.class);
                list.add(zipItem);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;

    }

    protected void onPostExecute(List<ZipItem> result) {
        // TODO: 21/10/15 meter los zips en la base de datos
    }
}
