package com.rukiasoft.androidapps.cocinaconroll.wifi;


import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.rukiasoft.androidapps.cocinaconroll.services.DownloadAndUnzipIntentService;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;

//to check if there are new recipes that need to be downloaded
public class WifiReceiver extends WakefulBroadcastReceiver {

    static public boolean serviceStarted = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            if (WifiHandler.IsWifiConnected(context) && !serviceStarted) {
                serviceStarted = true;
                Intent second_intent = new Intent(context, DownloadAndUnzipIntentService.class);
                second_intent.putExtra(Constants.KEY_TYPE, Constants.FILTER_LATEST_RECIPES);
                context.startService(second_intent);

            }
        }
    }
}
