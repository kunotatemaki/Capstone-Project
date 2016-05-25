package com.rukiasoft.androidapps.cocinaconroll.gcm;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rukiasoft.androidapps.cocinaconroll.BuildConfig;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.classes.ZipItem;
import com.rukiasoft.androidapps.cocinaconroll.database.DatabaseRelatedTools;
import com.rukiasoft.androidapps.cocinaconroll.services.DownloadAndUnzipIntentService;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;
import com.squareup.okhttp.Response;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iRuler on 20/10/15.
 */
public class GetZipsAsyncTask extends AsyncTask<Void, Void, List<ZipItem>> {
    private final Context mContext;
    private final String TAG = LogHelper.makeLogTag(GetZipsAsyncTask.class);

    public GetZipsAsyncTask(Context context){
        mContext = context;
    }
    protected List<ZipItem> doInBackground(Void... params) {
        List<ZipItem> list = new ArrayList<>();

        String urlBase = BuildConfig.RASPBERRY_IP + mContext.getResources().getString(R.string.server_url_tomcat);
        String method = mContext.getResources().getString(R.string.get_zips_method);

        RestTools restTools = new RestTools();
        Response response = restTools.doRestRequest(urlBase, method, null);

        if(response != null && response.code() == HttpURLConnection.HTTP_OK){
            LogHelper.i(TAG, "comprobación zips descargados");
            JsonParser jsonParser = new JsonParser();
            JsonObject jo = (JsonObject)jsonParser.parse(response.body().charStream());
            JsonArray jsonArr = jo.getAsJsonArray("zips");
            for(JsonElement gSon : jsonArr ) {
                ZipItem zipItem = new Gson().fromJson(gSon.getAsString(), ZipItem.class);
                list.add(zipItem);
            }
        }else{
            LogHelper.i(TAG, "comprobación zips NO descargados");
        }
        return list;

    }

    protected void onPostExecute(List<ZipItem> result) {
        Tools mTools = new Tools();
        Long expirationTimeFromNow;
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools();
        boolean newZip = false;
        Integer days = mTools.getIntegerFromPreferences(mContext, Constants.PROPERTY_DAYS_TO_NEXT_UPDATE);
        if(days == Integer.MIN_VALUE){
            days = 1;
        }
        for(ZipItem zip : result) {
            //newZip = dbTools.CheckAndInsertNewZip(zip.getName(), zip.getLink()) | newZip;
            Uri uri = dbTools.insertNewZip(mContext, zip.getName(), zip.getLink());
            if(uri == null){
                continue;
            }
            try {
                long id = ContentUris.parseId(uri);
                newZip = (id != -1) | newZip;
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        if(newZip){
            if (mTools.hasPermissionForDownloading(mContext)) {
                Intent intent = new Intent(mContext, DownloadAndUnzipIntentService.class);
                intent.putExtra(Constants.KEY_TYPE, Constants.FILTER_LATEST_RECIPES);
                mContext.startService(intent);
            }
            //reset number of days to next download
            days = 1;
        }else{
            //no new updates. Double the time to next update.
            days = days * 2;
            if(days > 30) {
                days = 1;
            }
        }
        expirationTimeFromNow = Constants.TIMEFRAME_MILISECONDS_DAY * days;
        mTools.savePreferences(mContext, Constants.PROPERTY_DAYS_TO_NEXT_UPDATE, days);
        mTools.savePreferences(mContext, Constants.PROPERTY_EXPIRATION_TIME, System.currentTimeMillis() + expirationTimeFromNow);
    }
}
