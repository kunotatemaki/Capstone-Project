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

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.gcm.GcmListenerService;
import com.rukiasoft.androidapps.cocinaconroll.CocinaConRollApplication;
import com.rukiasoft.androidapps.cocinaconroll.database.DatabaseRelatedTools;
import com.rukiasoft.androidapps.cocinaconroll.services.DownloadAndUnzipIntentService;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = LogHelper.makeLogTag(MyGcmListenerService.class);

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String name, link;
        Log.v(TAG, "recibo notificación");
        if(data != null && data.containsKey("name") && data.containsKey("link")){
            name = data.getString("name");
            link = data.getString("link");
        }else{
            return;
        }

        DatabaseRelatedTools dbTools = new DatabaseRelatedTools(getApplicationContext());
        Uri uri = dbTools.insertNewZip(name, link);
        if(uri == null){
            Log.d(TAG, "Null uri");
            return;
        }
        Log.d(TAG, "Uri: " + uri.toString());
        try {
            long id = ContentUris.parseId(uri);
            if (id != -1) {
                Tools mTools = new Tools();
                if (mTools.hasPermissionForDownloading(getApplicationContext())) {
                    Intent intent = new Intent(this, DownloadAndUnzipIntentService.class);
                    intent.putExtra(Constants.KEY_TYPE, Constants.FILTER_LATEST_RECIPES);
                    startService(intent);
                }
            }
        }catch (NumberFormatException e){
            e.printStackTrace();
            Tracker t = ((CocinaConRollApplication) getApplication()).getTracker();
            // Build and send exception.
            t.send(new HitBuilders.ExceptionBuilder()
                    .setDescription(MyGcmListenerService.class.getSimpleName() + ":" + "error parsing Uri")
                    .setFatal(true)
                    .build());
        }
    }
    // [END receive_message]
}
