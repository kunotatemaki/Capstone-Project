package com.rukiasoft.androidapps.cocinaconroll;



import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.GoogleApiClient;


/**
 * Created by Ruler on 2015.
 */
public class CocinaConRollApplication  extends /*MultiDexApplication*/Application {
    /**
     * The Analytics singleton. The field is set in onCreate method override when the application
     * class is initially created.
     */
    private GoogleAnalytics analytics;

    /**
     * The default app tracker. The field is from onCreate callback when the application is
     * initially created.
     */
    private Tracker tracker;

    /**
     * Access to the global Analytics singleton. If this method returns null you forgot to either
     * set android:name="&lt;this.class.name&gt;" attribute on your application element in
     * AndroidManifest.xml or you are not setting this.analytics field in onCreate method override.
     */
    public GoogleAnalytics analytics() {
        return analytics;
    }

    /**
     * The default app tracker. If this method returns null you forgot to either set
     * android:name="&lt;this.class.name&gt;" attribute on your application element in
     * AndroidManifest.xml or you are not setting this.tracker field in onCreate method override.
     */
    public Tracker getTracker() {
        return tracker;
    }

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Getter for the {@code GoogleApiClient}.
     */
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        mGoogleApiClient = googleApiClient;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        analytics = GoogleAnalytics.getInstance(this);

        //analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
        tracker = analytics.newTracker(R.xml.track_app);

        // Provide unhandled exceptions reports. Do that first after creating the tracker
        if(BuildConfig.DEBUG) {
            tracker.enableExceptionReporting(false);
        }else{
            tracker.enableExceptionReporting(true);
        }

        // Enable Remarketing, Demographics & Interests reports
        // https://developers.google.com/analytics/devguides/collection/android/display-features
        tracker.enableAdvertisingIdCollection(true);

        // Enable automatic activity tracking for your app
        tracker.enableAutoActivityTracking(true);
         /*Thread.UncaughtExceptionHandler myHandler = new ExceptionReporter(
                tracker,                                        // Currently used Tracker.
                Thread.getDefaultUncaughtExceptionHandler(),      // Current default uncaught exception handler.
                this);                                         // Context of the application.

// Make myHandler the new default uncaught exception handler.
        Thread.setDefaultUncaughtExceptionHandler(myHandler);*/
    }

    /*@Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if(BuildConfig.DEBUG) {
            MultiDex.install(this);
        }
    }*/

    int nActivityConnected = 0;
    public void addActivity(){
        nActivityConnected++;
    }
    public void popActivity(){
        nActivityConnected--;
        if(nActivityConnected < 0){
            nActivityConnected = 0;
        }
        if(nActivityConnected == 0){
            if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
                mGoogleApiClient.disconnect();
            }
        }
    }

}
