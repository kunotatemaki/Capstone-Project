package com.rukiasoft.androidapps.cocinaconroll.ui;


import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.classes.RecipesListNameComparator;
import com.rukiasoft.androidapps.cocinaconroll.database.DatabaseRelatedTools;
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
        LoaderManager.LoaderCallbacks<List<RecipeItem>>, RecipeListRecyclerViewAdapter.OnItemClickListener,
        AppBarLayout.OnOffsetChangedListener{

    private static final String KEY_SCROLL_POSITION = Constants.PACKAGE_NAME + "." + RecipeListFragment.class.getSimpleName() + ".scrollposition";
    public static final String KEY_RECIPE_LIST = Constants.PACKAGE_NAME + "." + RecipeListFragment.class.getSimpleName() + ".recipelist";


    @Nullable
    @Bind(R.id.toolbar_recipe_list_fragment) Toolbar mToolbarRecipeListFragment;
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.swipe_refresh_layout)
    protected SwipeRefreshLayout refreshLayout;
    @Bind((R.id.fastscroller))
    FastScroller fastScroller;
    @Bind(R.id.appbar_layout)
    AppBarLayout mAppBarLayout;
    @Bind(R.id.recipe_list_number_recipes)
    TextView nRecipesInRecipeList;
    @Bind(R.id.recipe_list_type_recipes)
    TextView typeRecipesInRecipeList;
    @Bind(R.id.recipe_list_type_icon)
    ImageView typeIconInRecipeList;
    @Bind(R.id.numberandtype_recipes_bar)
    RelativeLayout numberAndTypeBar;
    @Bind(R.id.add_recipe_fab)
    FloatingActionButton addRecipeButtonFAB;

    private SlideInBottomAnimationAdapter slideAdapter;
    private RecipeListRecyclerViewAdapter adapter;
    List<RecipeItem> mRecipes;
    int scrollPosition = 0;
    private int columnCount = 10;
    private String lastFilter;
    private InterstitialAd mInterstitialAd;
    private RecipeItem recipeToShow;

    public RecipeListFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.banner_ad_unit_id_intersticial));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                if(recipeToShow != null) {
                    launchActivityDetails();
                    recipeToShow = null;
                }
            }
        });

        requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("B29C1F71528C79C864D503360C5225C0")  // My Xperia Z3 test device
                .setGender(AdRequest.GENDER_FEMALE)
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }
     @Override
     public void onDetach(){
         super.onDetach();
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
        if(savedInstanceState != null && savedInstanceState.containsKey(KEY_RECIPE_LIST)){
            mRecipes = savedInstanceState.getParcelableArrayList(KEY_RECIPE_LIST);
        }

        if(mAppBarLayout != null){
            mAppBarLayout.addOnOffsetChangedListener(this);
        }

        typeRecipesInRecipeList.setText(getResources().getString(R.string.all_recipes));
        typeIconInRecipeList.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_all_24));
        lastFilter = Constants.FILTER_ALL_RECIPES;

        if(addRecipeButtonFAB != null) {
            addRecipeButtonFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Handler().postDelayed(new Runnable() {
                        @Override public void run() {
                            Intent intent = new Intent(getActivity(), EditRecipeActivity.class);
                            getActivity().startActivityForResult(intent, Constants.REQUEST_CREATE_RECIPE);
                        }
                    }, 150);
                }
            });
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
        if(mRecipes == null || mRecipes.size() == 0) {
            getLoaderManager().initLoader(Constants.LOADER_ID, null, this);
        }else{
            setData();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        if (mRecyclerView.getLayoutManager() != null) {
            int[] scrollPosition = new int[columnCount];
            scrollPosition = ((StaggeredGridLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPositions(scrollPosition);
            savedInstanceState.putSerializable(KEY_SCROLL_POSITION, scrollPosition[0]);
        }
        if(mRecipes != null) {
            savedInstanceState.putParcelableArrayList(KEY_RECIPE_LIST, (ArrayList<RecipeItem>) mRecipes);
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
        return new RecipeListLoader(getActivity().getApplicationContext());
    }

    @Override
    public void onLoadFinished(Loader<List<RecipeItem>> loader, List<RecipeItem> data) {
        Log.i("", "+++ onLoadFinished() called! +++");
        mRecipes = data;
        setData();
    }

    private void orderRecipesByName(){
        Comparator<RecipeItem> comparatorName = new RecipesListNameComparator();
        Collections.sort(mRecipes, comparatorName);
        for(int i=0; i<mRecipes.size(); i++){
            mRecipes.get(i).setPosition(i);
        }
    }

    private void setData(){
        orderRecipesByName();
        ((ToolbarAndRefreshActivity) getActivity()).needToShowRefresh = false;
        if(isResumed()) {
            Tools tools = new Tools();
            tools.hideRefreshLayout(getActivity());
        }

        adapter = new RecipeListRecyclerViewAdapter(getActivity(), mRecipes);
        adapter.setHasStableIds(true);
        adapter.setOnItemClickListener(this);
        mRecyclerView.setHasFixedSize(true);

        slideAdapter = wrapAdapter(adapter);

        mRecyclerView.setAdapter(slideAdapter);
        //mRecyclerView.setAdapter(adapter);
        columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);


        mRecyclerView.setLayoutManager(sglm);
        mRecyclerView.scrollToPosition(scrollPosition);
        //Set the fast Scroller
        fastScroller.setRecyclerView(mRecyclerView);

        //set the number of recipes
        String nrecipes = String.format(getResources().getString(R.string.recipes), mRecipes.size());
        nRecipesInRecipeList.setText(nrecipes);
    }

    private SlideInBottomAnimationAdapter wrapAdapter(RecipeListRecyclerViewAdapter adapter){
        SlideInBottomAnimationAdapter slideAdapter = new SlideInBottomAnimationAdapter(adapter);
        slideAdapter.setInterpolator(new OvershootInterpolator(2.0f));
        slideAdapter.setDuration(2000);
        return slideAdapter;
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
        //interstitial
        Tools tools = new Tools();
        int number = tools.getIntegerFromPreferences(getActivity().getApplicationContext(), Constants.PREFERENCE_INTERSTITIAL);
        if(number<0 || number>Constants.N_RECIPES_TO_INTERSTICIAL){
            number = 0;
        }
        recipeToShow = recipeItem;
        if(number != Constants.N_RECIPES_TO_INTERSTICIAL) {
            launchActivityDetails();
        }else if(mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            number = 0;
        }else{
            launchActivityDetails();
            requestNewInterstitial();
            return;
        }
        tools.savePreferences(getActivity(), Constants.PREFERENCE_INTERSTITIAL, ++number);

    }

    private void launchActivityDetails(){
        //int i = 1/0;
        Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.KEY_RECIPE, recipeToShow);
        intent.putExtras(bundle);
        //TODO probar transiciones sencillas para pre-lollipop
        ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
        // Now we can start the Activity, providing the activity options as a bundle
        ActivityCompat.startActivityForResult(getActivity(), intent, Constants.REQUEST_DETAILS, activityOptions.toBundle());

        recipeToShow = null;

    }


    public void filterRecipes(String filter) {
        lastFilter = filter;
        Tools mTools = new Tools();
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools(getActivity());
        List<RecipeItem> filteredModelList = new ArrayList<>();
        String type = "";
        int iconResource = 0;

        if(filter.compareTo(Constants.FILTER_ALL_RECIPES) == 0) {
            filteredModelList = new ArrayList<>(mRecipes);
            type = getResources().getString(R.string.all_recipes);
            iconResource = R.drawable.ic_all_24;
        }else if(filter.compareTo(Constants.FILTER_MAIN_COURSES_RECIPES) == 0){
            for (RecipeItem item : mRecipes) {
                if (item.getType().equals(Constants.TYPE_MAIN)) {
                    filteredModelList.add(item);
                }
            }
            type = getResources().getString(R.string.main_courses);
            iconResource = R.drawable.ic_main_24;
        }else if(filter.compareTo(Constants.FILTER_STARTER_RECIPES) == 0){
            for (RecipeItem item : mRecipes) {
                if (item.getType().equals(Constants.TYPE_STARTERS)) {
                    filteredModelList.add(item);
                }
            }
            type = getResources().getString(R.string.starters);
            iconResource = R.drawable.ic_starters_24;
        }else if(filter.compareTo(Constants.FILTER_DESSERT_RECIPES) == 0){
            for (RecipeItem item : mRecipes) {
                if (item.getType().equals(Constants.TYPE_DESSERTS)) {
                    filteredModelList.add(item);
                }
            }
            type = getResources().getString(R.string.desserts);
            iconResource = R.drawable.ic_dessert_24;
        }else if(filter.compareTo(Constants.FILTER_VEGETARIAN_RECIPES) == 0){
            for (RecipeItem item : mRecipes) {
                if (item.getVegetarian()) {
                    filteredModelList.add(item);
                }
            }
            type = getResources().getString(R.string.vegetarians);
            iconResource = R.drawable.ic_vegetarians_24;
        }else if(filter.compareTo(Constants.FILTER_FAVOURITE_RECIPES) == 0){
            for (RecipeItem item : mRecipes) {
                if (dbTools.isFavorite(item.getName())) {
                    filteredModelList.add(item);
                }
            }
            type = getResources().getString(R.string.favourites);
            iconResource = R.drawable.ic_favorite_black_24dp;
        }else if(filter.compareTo(Constants.FILTER_OWN_RECIPES) == 0){
            for(RecipeItem item : mRecipes) {
                if ((item.getState() & (Constants.FLAG_OWN | Constants.FLAG_EDITED)) != 0) {
                    filteredModelList.add(item);
                }
            }
            type = getResources().getString(R.string.own_recipes);
            iconResource = R.drawable.ic_own_24;
        }else if(filter.compareTo(Constants.FILTER_LATEST_RECIPES) == 0){
            for(RecipeItem item : mRecipes) {
                if (mTools.isInTimeframe(item)) {
                    filteredModelList.add(item);
                }
            }
            type = getResources().getString(R.string.last_downloaded);
            iconResource = R.drawable.ic_latest_24;
        }
        typeRecipesInRecipeList.setText(type);
        String nrecipes = String.format(getResources().getString(R.string.recipes), filteredModelList.size());
        nRecipesInRecipeList.setText(nrecipes);
        typeIconInRecipeList.setImageDrawable(ContextCompat.getDrawable(getActivity(), iconResource));
        //Change the adapter
        RecipeListRecyclerViewAdapter newAdapter = new RecipeListRecyclerViewAdapter(getActivity(), filteredModelList);
        newAdapter.setHasStableIds(true);
        newAdapter.setOnItemClickListener(this);
        mRecyclerView.setHasFixedSize(true);

        SlideInBottomAnimationAdapter newSlideAdapter = wrapAdapter(newAdapter);

        mRecyclerView.swapAdapter(newSlideAdapter, false);
        //mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);

        adapter = newAdapter;
        slideAdapter = newSlideAdapter;
        mRecyclerView.setLayoutManager(sglm);
        mRecyclerView.scrollToPosition(0);
        //Set the fast Scroller
        fastScroller.setRecyclerView(mRecyclerView);
    }


    @Nullable
    public Toolbar getToolbarRecipeListFragment() {
        return mToolbarRecipeListFragment;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;
        if(percentage > 0.5f){
            if(getActivity() instanceof RecipeListActivity){
                ((RecipeListActivity) getActivity()).closeSearchView();
            }
        }
    }

    public void setVisibilityWithSearchWidget(int visibility){
        numberAndTypeBar.setVisibility(visibility);
        if(visibility == View.GONE) addRecipeButtonFAB.hide();
        //else addRecipeButton.show();
    }

    public void updateRecipe(RecipeItem recipe) {
        int index = recipe.getPosition();
        if(mRecipes == null || index >= mRecipes.size()){
            return;
        }
        mRecipes.remove(index);
        mRecipes.add(index, recipe);

        filterRecipes(lastFilter);
    }

    public void createRecipe(RecipeItem recipe) {
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools(getActivity());
        dbTools.addRecipeToArrayAndSuggestions(mRecipes, recipe);
        orderRecipesByName();
        filterRecipes(lastFilter);
    }

    public void searchAndShow(String name) {
        for(RecipeItem recipe : mRecipes){
            if(recipe.getName().equals(name)) {
                showRecipeDetails(recipe);
                break;
            }
        }
    }

    public void deleteRecipe(int index) {
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools(getActivity());
        dbTools.removeRecipeFromArrayAndSuggestions(mRecipes, index);
        orderRecipesByName();
        filterRecipes(lastFilter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view.findViewById(R.id.recycler_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ThanksActivity.class));
            }
        });
    }
}



