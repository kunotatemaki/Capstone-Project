/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rukiasoft.androidapps.cocinaconroll.ui;

import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.TextView;

import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;

import java.lang.reflect.Field;

/**
 * Base activity for activities that need to show a Refresh Layout and a Custom Toolbar
 */
public abstract class ToolbarAndRefreshActivity extends AppCompatActivity {

    private static final String TAG = LogHelper.makeLogTag(ToolbarAndRefreshActivity.class);

    private SwipeRefreshLayout refreshLayout;
    public Boolean needToShowRefresh = false;

    public SwipeRefreshLayout getRefreshLayout() {
        return refreshLayout;
    }



    protected void setDefaultValuesForOptions(int id){
        PreferenceManager.setDefaultValues(this, id, false);
    }

    public void setToolbar(Toolbar toolbar){
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        try {
            if(toolbar.getClass() != null) {
                Field f = toolbar.getClass().getDeclaredField("mTitleTextView");
                f.setAccessible(true);
                TextView titleTextView = (TextView) f.get(toolbar);
                titleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                titleTextView.setFocusable(true);
                titleTextView.setFocusableInTouchMode(true);
                titleTextView.requestFocus();
                titleTextView.setSingleLine(true);
                titleTextView.setSelected(true);
                titleTextView.setMarqueeRepeatLimit(-1);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void setRefreshLayout(SwipeRefreshLayout _refreshLayout){
        refreshLayout = _refreshLayout;
        if (refreshLayout == null) {
            return;
        }
        //configure swipeRefreshLayout
        setRefreshLayoutColorScheme(ContextCompat.getColor(this, R.color.color_refresh_1),
                ContextCompat.getColor(this, R.color.color_refresh_2),
                ContextCompat.getColor(this, R.color.color_refresh_3),
                ContextCompat.getColor(this, R.color.color_refresh_4));
    }

    /**
     * It shows the SwipeRefreshLayout progress
     */
    public void showRefreshLayoutSwipeProgress() {
        if (refreshLayout == null) {
            return;
        }
        needToShowRefresh = true;
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(needToShowRefresh) {
                    refreshLayout.setRefreshing(true);
                }
            }
        });
    }



    /**
     * It shows the SwipeRefreshLayout progress
     */
    public void hideRefreshLayoutSwipeProgress() {
        if (refreshLayout == null) {
            return;
        }
        refreshLayout.setRefreshing(false);
    }

    /**
     * Enables swipe gesture
     */
    public void enableRefreshLayoutSwipe() {
        if (refreshLayout == null) {
            return;
        }
        refreshLayout.setEnabled(true);
    }

    /**
     * Disables swipe gesture. It prevents manual gestures but keeps the option tu show
     * refreshing programatically.
     */
    public  void disableRefreshLayoutSwipe() {
        if (refreshLayout == null) {
            return;
        }
        refreshLayout.setEnabled(false);
    }

    /**
     * Set colors of refreshlayout
     */
    private void setRefreshLayoutColorScheme(int colorRes1, int colorRes2, int colorRes3, int colorRes4) {
        if (refreshLayout == null) {
            return;
        }
        refreshLayout.setColorSchemeColors(colorRes1, colorRes2, colorRes3, colorRes4);
    }

}
