package com.rukiasoft.androidapps.cocinaconroll.services;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.classes.ZipItem;
import com.rukiasoft.androidapps.cocinaconroll.database.DatabaseRelatedTools;
import com.rukiasoft.androidapps.cocinaconroll.ui.RecipeListActivity;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;
import com.rukiasoft.androidapps.cocinaconroll.utilities.ReadWriteTools;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;
import com.rukiasoft.androidapps.cocinaconroll.wifi.WifiReceiver;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class DownloadAndUnzipIntentService extends IntentService {


    private static final String TAG = LogHelper.makeLogTag(DownloadAndUnzipIntentService.class);
    //DataBase
    private final long[] mVibratePattern = {0, 200, 200, 300};
    // Notification Count
    static private int mNotificationCount;
    private static final int NOTIF_ALERTA_ID = 1;


    public DownloadAndUnzipIntentService() {
        super("DownloadAndUnzipIntentService");
    }

    private OkHttpClient client;
    private ReadWriteTools rwTools;
    private Tools mTools;

    @Override
    protected void onHandleIntent(Intent intent) {

        DatabaseRelatedTools dbTools = new DatabaseRelatedTools();
        rwTools = new ReadWriteTools();
        mTools = new Tools();
        client = new OkHttpClient();

        List<ZipItem> list = dbTools.getZipsByState(getApplicationContext(), Constants.STATE_NOT_DOWNLOADED);
        Integer newRecipes = 0;

        boolean check;
        for (int i = 0; i < list.size(); i++) {
            check = downloadZip(list.get(i).getName(), list.get(i).getLink());

            if (!check) {
                Log.e(TAG, "Error downloading data from server");
                continue;
            }
            try {
                dbTools.updateZipState(getApplicationContext(), list.get(i).getName(),
                        Constants.STATE_DOWNLOADED_NOT_UNZIPED);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        list = dbTools.getZipsByState(getApplicationContext(), Constants.STATE_DOWNLOADED_NOT_UNZIPED);
        for (int i = 0; i < list.size(); i++) {
            if(!list.get(i).getName().contains(".zip")){
                continue;
            }
            check = rwTools.unzipRecipesInOriginal(getApplicationContext(), list.get(i).getName());
            if (!check) {
                Log.e(TAG, "Data downladed is corrupt");
                continue;
            }
            try {
                dbTools.updateZipState(getApplicationContext(), list.get(i).getName(),
                        Constants.STATE_DOWNLOADED_UNZIPED_NOT_ERASED);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            //update variable to load new files
            mTools.savePreferences(this, Constants.PROPERTY_RELOAD_NEW_ORIGINALS, true);
            //ya se han descomprimido, aumento contador
            newRecipes++;
        }
        list = dbTools.getZipsByState(getApplicationContext(),
                Constants.STATE_DOWNLOADED_UNZIPED_NOT_ERASED);
        for (int i = 0; i < list.size(); i++) {
            if (rwTools.deleteFile(rwTools.getZipsStorageDir(getApplicationContext()) + list.get(i).getName())) {
                try {
                    dbTools.updateZipState(getApplicationContext(), list.get(i).getName(),
                            Constants.STATE_DOWNLOADED_UNZIPED_ERASED);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        rwTools.loadNewFilesAndInsertInDatabase(getApplicationContext());
        mTools.savePreferences(this, Constants.PROPERTY_RELOAD_NEW_ORIGINALS, false);
        String type = Constants.FILTER_ALL_RECIPES;
        if(intent.hasExtra(Constants.KEY_TYPE)) {
            type = intent.getExtras().getString(Constants.KEY_TYPE);
        }
        if(newRecipes > 0){
            showNotifications(type);
        }
        WifiReceiver.serviceStarted = false;
    }

    private boolean downloadZip(String name, String url){
        try {
            Boolean ret = rwTools.isExternalStorageWritable();
            if (!ret) {
                LogHelper.e(TAG, "no hay external storage in downloadzip");
                return false;
            }
            File dir = new File(rwTools.getZipsStorageDir(getApplicationContext()));
            if (!dir.exists()) {
                ret = dir.mkdirs();
                if (!ret) {
                    LogHelper.e(TAG, "no crea dir in external storage in downloadzip");
                    return false;
                }
            }

            File file = new File(dir, name);


            ResponseBody response = run(url);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(response.bytes());
            fos.flush();
            fos.close();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            return false;
        }
    }

    private ResponseBody run(String url) throws IOException, IllegalArgumentException {
        Request request = new Request.Builder()
                    .url(url)
                    .build();

        Response response = client.newCall(request).execute();
        return response.body();
    }


    private void showNotifications(String type) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int stringId = getApplicationContext().getApplicationInfo().labelRes;
        String title = getApplicationContext().getString(stringId);

        Intent notIntent = new Intent(this, RecipeListActivity.class);
        notIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notIntent.putExtra(Constants.KEY_TYPE, type);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification_large);

        String tone = mTools.getStringFromPreferences(getApplicationContext(), "option_notification");
        Uri uri;
        if (tone.compareTo("") == 0)
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        else
            uri = Uri.parse(tone);
        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setTicker(getResources().getString(R.string.ticker))//getApplicationContext().getString(R.string.gcm_notification_ticker))
                                .setSmallIcon(R.drawable.ic_notification_olla)
                                .setLargeIcon(bm)
                                .setAutoCancel(true)
                                .setContentTitle(title)
                                .setSound(uri)
                                .setContentText(getResources().getString(R.string.new_recipes)).setNumber(++mNotificationCount);

        if (mTools.getBooleanFromPreferences(getApplicationContext(), "option_vibrate")) {
            mBuilder.setVibrate(mVibratePattern);
        }


        PendingIntent contIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notIntent, PendingIntent.FLAG_ONE_SHOT);


        mBuilder.setContentIntent(contIntent);

        mNotificationManager.notify(NOTIF_ALERTA_ID, mBuilder.build());

    }

}

