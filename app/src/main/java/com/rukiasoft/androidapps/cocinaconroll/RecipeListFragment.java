package com.rukiasoft.androidapps.cocinaconroll;


import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.rukiasoft.androidapps.cocinaconroll.classes.RecipesListNameComparator;
import com.rukiasoft.androidapps.cocinaconroll.fastscroller.FastScroller;
import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeListLoader;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.adapters.SlideInBottomAnimationAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class RecipeListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<List<RecipeItem>>, RecipeListRecyclerViewAdapter.OnItemClickListener{

    private static final int LOADER_ID = 1;
    private static final String KEY_SCROLL_POSITION = Constants.PACKAGE_NAME + ".scrollposition";


    @Nullable
    @Bind(R.id.toolbar_recipe_list_fragment) Toolbar mToolbarRecipeListFragment;
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.swipe_refresh_layout)
    protected SwipeRefreshLayout refreshLayout;
    @Bind((R.id.fastscroller))
    FastScroller fastScroller;

    private SlideInBottomAnimationAdapter slideAdapter;
    private RecipeListRecyclerViewAdapter adapter;
    List<RecipeItem> mRecipes;
    int scrollPosition = 0;
    public RecipeListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);
        ButterKnife.bind(this, view);

        //Set the mToolbarRecipeListFragment
        if(getActivity() instanceof ToolbarAndRefreshActivity){
            ((ToolbarAndRefreshActivity) getActivity()).setToolbar(mToolbarRecipeListFragment);
        }

        //Set the refresh layout
        Tools tools = new Tools();
        tools.setRefreshLayout(getActivity(), refreshLayout);

        scrollPosition = 0;
        if(savedInstanceState != null && savedInstanceState.containsKey(KEY_SCROLL_POSITION)){
            scrollPosition = savedInstanceState.getInt(KEY_SCROLL_POSITION);
        }


        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        Tools tools = new Tools();
        if(getActivity() instanceof ToolbarAndRefreshActivity){
            if(((ToolbarAndRefreshActivity) getActivity()).needToShowRefresh){
                tools.showRefreshLayout(getActivity());
            }else{
                tools.hideRefreshLayout(getActivity());
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        // Initialize a Loader with id '1'. If the Loader with this id already
        // exists, then the LoaderManager will reuse the existing Loader.
        getLoaderManager().initLoader(LOADER_ID, null, this);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        if (mRecyclerView.getLayoutManager() != null) {
            int columnCount = getResources().getInteger(R.integer.list_column_count);
            int[] scrollPosition = new int[columnCount];
            scrollPosition = ((StaggeredGridLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPositions(scrollPosition);
            savedInstanceState.putSerializable(KEY_SCROLL_POSITION, scrollPosition[0]);
        }
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public Loader<List<RecipeItem>> onCreateLoader(int id, Bundle args) {
        if(getActivity() instanceof ToolbarAndRefreshActivity){
            if(isResumed()){
                Tools tools = new Tools();
                tools.showRefreshLayout(getActivity());
            }else {
                ((ToolbarAndRefreshActivity) getActivity()).needToShowRefresh = true;
            }
        }
        return new RecipeListLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<RecipeItem>> loader, List<RecipeItem> data) {
        Log.i("", "+++ onLoadFinished() called! +++");
        mRecipes = data;
        Comparator<RecipeItem> comparatorName = new RecipesListNameComparator();
        Collections.sort(mRecipes, comparatorName);
        ((ToolbarAndRefreshActivity) getActivity()).needToShowRefresh = false;
        if(isResumed()) {
            Tools tools = new Tools();
            tools.hideRefreshLayout(getActivity());
        }

        adapter = new RecipeListRecyclerViewAdapter(getActivity(), mRecipes);
        adapter.setHasStableIds(true);
        adapter.setOnItemClickListener(this);
        //mRecyclerView.setHasFixedSize(true);

        wrapAdapter(adapter);

        mRecyclerView.setAdapter(slideAdapter);
        //mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);


        mRecyclerView.setLayoutManager(sglm);
        mRecyclerView.scrollToPosition(scrollPosition);
        //Set the fast Scroller
        fastScroller.setRecyclerView(mRecyclerView);

    }

    private void wrapAdapter(RecipeListRecyclerViewAdapter adapter){
        slideAdapter = new SlideInBottomAnimationAdapter(adapter);
        slideAdapter.setInterpolator(new OvershootInterpolator(2.0f));
        slideAdapter.setDuration(2000);
    }

    @Override
    public void onLoaderReset(Loader<List<RecipeItem>> loader) {
        mRecyclerView.setAdapter(null);
        Tools tools = new Tools();
        tools.hideRefreshLayout(getActivity());
    }

    @Override
    public void onItemClick(View view, RecipeItem recipeItem) {
        showRecipeDetails(recipeItem);
    }

    public void showRecipeDetails(RecipeItem recipeItem){
        Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(RecipeListActivity.KEY_RECIPE, recipeItem);
        intent.putExtras(bundle);
        getActivity().startActivity(intent);
    }

    public void getFilteredRecipes(String filter) {

    }


    /*public boolean onQueryTextChange(String query) {
        final List<RecipeItem> filteredModelList = filter(mRecipes, query);
        adapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return false;
    }*/

    private List<RecipeItem> filter(List<RecipeItem> recipes, String query) {
        Tools tools = new Tools();
        query = tools.getNormalizedString(query);
        final List<RecipeItem> filteredModelList = new ArrayList<>();
        for (RecipeItem item : recipes) {
            final String name = tools.getNormalizedString(item.getName());
            if (name.contains(query)) {
                filteredModelList.add(item);
            }
        }
        return filteredModelList;
    }

    @Nullable
    public Toolbar getToolbarRecipeListFragment() {
        return mToolbarRecipeListFragment;
    }
}

//TODO - scroll listener para quitar el search
//TODO - quitar y poner FAB con searchwidget
