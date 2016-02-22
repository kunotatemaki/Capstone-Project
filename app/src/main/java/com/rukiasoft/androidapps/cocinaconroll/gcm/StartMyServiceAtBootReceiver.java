package com.rukiasoft.androidapps.cocinaconroll.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

public class StartMyServiceAtBootReceiver extends BroadcastReceiver {
    //static String TAG = "StartMyServiceAtBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Tools mTools = new Tools();
            //check for zips
            long expirationTime =
                    mTools.getLongFromPreferences(context, Constants.PROPERTY_EXPIRATION_TIME);
            if (expirationTime == Long.MIN_VALUE || System.currentTimeMillis() > expirationTime) {
                GetZipsAsyncTask getZipsAsyncTask = new GetZipsAsyncTask(context.getApplicationContext());
                getZipsAsyncTask.execute();
            }

            //send token to server
            Intent intentGCM = new Intent(context, RegistrationIntentService.class);
            context.startService(intentGCM);
        }
    }
}
