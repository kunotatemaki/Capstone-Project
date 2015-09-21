package com.rukiasoft.androidapps.cocinaconroll;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeListLoader;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class RecipeListActivityFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<List<RecipeItem>>{

    private static final int LOADER_ID = 1;


    public RecipeListActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        // Initialize a Loader with id '1'. If the Loader with this id already
        // exists, then the LoaderManager will reuse the existing Loader.
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }


    @Override
    public Loader<List<RecipeItem>> onCreateLoader(int id, Bundle args) {
        return new RecipeListLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<RecipeItem>> loader, List<RecipeItem> data) {
        Log.i("", "+++ onLoadFinished() called! +++");
        /*mAdapter.setData(data);

        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }*/
    }

    @Override
    public void onLoaderReset(Loader<List<RecipeItem>> loader) {

    }
}
