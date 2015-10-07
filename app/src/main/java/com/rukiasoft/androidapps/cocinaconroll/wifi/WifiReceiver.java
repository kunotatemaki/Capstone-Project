package com.rukiasoft.androidapps.cocinaconroll.wifi;


import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;

public class WifiReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            if (WifiHandler.IsWifiConnected(context)) {
                Intent second_intent = new Intent(Constants.START_DOWNLOAD_ACTION_INTENT);
                context.sendBroadcast(second_intent);
            }
        }
    }
}
