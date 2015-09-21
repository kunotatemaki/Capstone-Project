package com.rukiasoft.androidapps.cocinaconroll.loader;

/**
 * Created by Raúl Feliz Alonso on 21/09/15.
 */


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.rukiasoft.androidapps.cocinaconroll.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;
import com.rukiasoft.androidapps.cocinaconroll.utilities.ReadWriteTools;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a custom Loader to read all recipes from all xml files
 */
public class RecipeListLoader extends AsyncTaskLoader<List<RecipeItem>> {
    private static final String TAG = LogHelper.makeLogTag(RecipeListLoader.class);


    // We hold a reference to the Loader's data here.
    private List<RecipeItem> mRecipes;

    public RecipeListLoader(Context ctx) {
        // Loaders may be used across multiple Activitys (assuming they aren't
        // bound to the LoaderManager), so NEVER hold a reference to the context
        // directly. Doing so will cause you to leak an entire Activity's context.
        // The superclass constructor will store a reference to the Application
        // Context instead, and can be retrieved with a call to getContext().
        super(ctx);
    }

    /****************************************************/
    /** (1) A task that performs the asynchronous load **/
    /****************************************************/

    /**
     * This method is called on a background thread and generates a List of
     * {@link RecipeItem} objects. Each entry corresponds to a single installed
     * application on the device.
     */
    @Override
    public List<RecipeItem> loadInBackground() {
        Log.d(TAG, "+++ loadInBackground() called! +++");

        // Create corresponding array of entries and load their labels.
        List<RecipeItem> recipes = loadRecipes();

        return recipes;
    }

    /*******************************************/
    /** (2) Deliver the results to the client **/
    /*******************************************/

    /**
     * Called when there is new data to deliver to the client. The superclass will
     * deliver it to the registered listener (i.e. the LoaderManager), which will
     * forward the results to the client through a call to onLoadFinished.
     */
    @Override
    public void deliverResult(List<RecipeItem> recipes) {
        if (isReset()) {
            Log.w(TAG, "+++ Warning! An async query came in while the Loader was reset! +++");
            // The Loader has been reset; ignore the result and invalidate the data.
            // This can happen when the Loader is reset while an asynchronous query
            // is working in the background. That is, when the background thread
            // finishes its work and attempts to deliver the results to the client,
            // it will see here that the Loader has been reset and discard any
            // resources associated with the new data as necessary.
            if (recipes != null) {
                releaseResources(recipes);
                return;
            }
        }

        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        List<RecipeItem> oldRecipes = mRecipes;
        mRecipes = recipes;

        if (isStarted()) {
            Log.i(TAG, "+++ Delivering results to the LoaderManager for" +
                    " the ListFragment to display! +++");
            // If the Loader is in a started state, have the superclass deliver the
            // results to the client.
            super.deliverResult(recipes);
        }

        // Invalidate the old data as we don't need it any more.
        if (oldRecipes != null && oldRecipes != recipes) {
            Log.i(TAG, "+++ Releasing any old data associated with this Loader. +++");
            releaseResources(oldRecipes);
        }
    }

    /*********************************************************/
    /** (3) Implement the Loader�s state-dependent behavior **/
    /*********************************************************/

    @Override
    protected void onStartLoading() {
        Log.i(TAG, "+++ onStartLoading() called! +++");

        if (mRecipes != null) {
            // Deliver any previously loaded data immediately.
            Log.i(TAG, "+++ Delivering previously loaded data to the client...");
            deliverResult(mRecipes);
        }

        //TODO - handle the observer
        // Register the observers that will notify the Loader when changes are made.
        /*if (mRecipesObserver == null) {
            mRecipesObserver = new InstalledRecipesObserver(this);
        }*/



        if (takeContentChanged()) {
            // When the observer detects a new installed application, it will call
            // onContentChanged() on the Loader, which will cause the next call to
            // takeContentChanged() to return true. If this is ever the case (or if
            // the current data is null), we force a new load.
            Log.i(TAG, "+++ A content change has been detected... so force load! +++");
            forceLoad();
        } else if (mRecipes == null) {
            // If the current data is null... then we should make it non-null! :)
            Log.i(TAG, "+++ The current data is data is null... so force load! +++");
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        Log.i(TAG, "+++ onStopLoading() called! +++");

        // The Loader has been put in a stopped state, so we should attempt to
        // cancel the current load (if there is one).
        cancelLoad();

        // Note that we leave the observer as is; Loaders in a stopped state
        // should still monitor the data source for changes so that the Loader
        // will know to force a new load if it is ever started again.
    }

    @Override
    protected void onReset() {
        Log.i(TAG, "+++ onReset() called! +++");

        // Ensure the loader is stopped.
        onStopLoading();

        // At this point we can release the resources associated with 'recipes'.
        if (mRecipes != null) {
            releaseResources(mRecipes);
            mRecipes = null;
        }

        // The Loader is being reset, so we should stop monitoring for changes.
//        if (mRecipesObserver != null) {
//            //TODO - handle the observer
//            // getContext().unregisterReceiver(mRecipesObserver);
//            mRecipesObserver = null;
//        }

    }

    @Override
    public void onCanceled(List<RecipeItem> recipes) {
        Log.i(TAG, "+++ onCanceled() called! +++");

        // Attempt to cancel the current asynchronous load.
        super.onCanceled(recipes);

        // The load has been canceled, so we should release the resources
        // associated with 'mRecipes'.
        releaseResources(recipes);
    }

    @Override
    public void forceLoad() {
        Log.i(TAG, "+++ forceLoad() called! +++");
        super.forceLoad();
    }

    /**
     * Helper method to take care of releasing resources associated with an
     * actively loaded data set.
     */
    private void releaseResources(List<RecipeItem> recipes) {
        recipes.clear();
        // For a simple List, there is nothing to do. For something like a Cursor,
        // we would close it in this method. All resources associated with the
        // Loader should be released here.
    }

    /*********************************************************************/
    /** (4) Observer which receives notifications when the data changes **/
    /*********************************************************************/

    //TODO - Loader here
    // An observer to notify the Loader when new recipes are installed/updated.
    //private InstalledRecipesObserver mRecipesObserver;


    /**************************/
    /** (5) Everything else! **/
    /**************************/



    private List<RecipeItem> loadRecipes() {
        List<RecipeItem> recipes = new ArrayList<>();
        MyFileFilter filter = new MyFileFilter();
        List<String> listEdited = ReadWriteTools.loadFiles(getContext(), filter, true);
        List<String> listOriginal = ReadWriteTools.loadFiles(getContext(), filter, false);
        List<String> listAssets = ReadWriteTools.loadRecipesFromAssets(getContext());

        for(int i=0; i<listEdited.size(); i++) {

            RecipeItem recipeItem= ReadWriteTools.readRecipe(getContext(), listEdited.get(i),
                    Constants.PATH_TYPE_EDITED);
            if(recipeItem != null) {
                recipes.add(recipeItem);
            }
        }

        for(int i=0; i<listOriginal.size(); i++) {
            if(listEdited.contains(listOriginal.get(i)))
                continue;
            RecipeItem recipeItem= ReadWriteTools.readRecipe(getContext(), listOriginal.get(i),
                    Constants.PATH_TYPE_ORIGINAL);
            if(recipeItem != null) {
                recipes.add(recipeItem);
                //CocinaConRollTools.deleteRecipe(getApplicationContext(), recipeItem, CocinaConRollConstants.FLAG_ORIGINAL);
            }
        }

        for(int i=0; i<listAssets.size(); i++) {

            RecipeItem recipeItem;
            if(listOriginal.contains(listAssets.get(i))){
                recipeItem = ReadWriteTools.readRecipe(getContext(), listAssets.get(i),
                        Constants.PATH_TYPE_ORIGINAL);
                if(Tools.isInTimeframe(recipeItem))
                    continue;
                else {
                    ReadWriteTools.deleteRecipe(getContext(), recipeItem, Constants.FLAG_ORIGINAL);
                    for (int j = 0; j < recipes.size(); j++) {
                        if (recipes.get(j).getFileName().compareTo(listAssets.get(i)) == 0) {
                            recipes.remove(j);
                            break;
                        }
                    }
                }
            }
            if(listEdited.contains(listAssets.get(i)))
                continue;
            recipeItem = ReadWriteTools.readRecipe(getContext(), listAssets.get(i),
                    Constants.PATH_TYPE_ASSETS);
            if(recipeItem != null) {
                recipes.add(recipeItem);
            }
        }
        //Log.d(TAG, "leido assets");
        return recipes;
    }



    public class MyFileFilter implements FilenameFilter {

        @Override
        public boolean accept(File directory, String fileName) {
            return fileName.endsWith(".xml");
        }
    }
}