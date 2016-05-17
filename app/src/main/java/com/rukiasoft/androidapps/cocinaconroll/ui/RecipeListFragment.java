package com.rukiasoft.androidapps.cocinaconroll.ui;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.classes.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.database.CocinaConRollContentProvider;
import com.rukiasoft.androidapps.cocinaconroll.database.DatabaseRelatedTools;
import com.rukiasoft.androidapps.cocinaconroll.database.RecipesTable;
import com.rukiasoft.androidapps.cocinaconroll.fastscroller.FastScroller;
import com.rukiasoft.androidapps.cocinaconroll.utilities.CommonRecipeOperations;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.ReadWriteTools;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class RecipeListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, RecipeListRecyclerViewAdapter.OnCardClickListener,
        AppBarLayout.OnOffsetChangedListener{

    private static final String KEY_SCROLL_POSITION = Constants.PACKAGE_NAME + ".scrollposition";
    private static final String KEY_RECIPE_LIST = Constants.PACKAGE_NAME + ".recipelist";
    private static final String KEY_UPLOAD_TO_DRIVE = Constants.PACKAGE_NAME + ".update_to_drive";
    private static final int LOAD_ORIGINAL_PATH = 0;
    private static final int LOAD_EDITED_PATH = 1;


    @Nullable
    @BindView(R.id.toolbar_recipe_list_fragment) Toolbar mToolbarRecipeListFragment;
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout)
    protected SwipeRefreshLayout refreshLayout;
    @Nullable @BindView((R.id.fastscroller))
    FastScroller fastScroller;
    @BindView(R.id.appbar_layout)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.recipe_list_number_recipes)
    TextView nRecipesInRecipeList;
    @BindView(R.id.recipe_list_type_recipes)
    TextView typeRecipesInRecipeList;
    @BindView(R.id.recipe_list_type_icon)
    ImageView typeIconInRecipeList;
    @BindView(R.id.numberandtype_recipes_bar)
    RelativeLayout numberAndTypeBar;
    @BindView(R.id.add_recipe_fab)
    FloatingActionButton addRecipeButtonFAB;
    @BindView(R.id.init_database_text) TextView initDatabaseText;
    private Unbinder unbinder;

    interface TaskCallback {
        void onInitDatabasePostExecute();
    }

    private TaskCallback mInitDatabaseCallback;
    //private SlideInBottomAnimationAdapter slideAdapter;
    //private RecipeListRecyclerViewAdapter adapter;
    private List<RecipeItem> mRecipes;
    private int savedScrollPosition = 0;
    private int columnCount = 10;
    private String lastFilter;
    private InterstitialAd mInterstitialAd;
    private RecipeItem recipeToShow;
    private boolean uploadRecipesToDrive = true;
    private boolean readExternalPermisionDialogShown;

    private class InitDatabase extends AsyncTask<Void, Integer, Void> {
        final Context mContext;
        final int mode;

        public InitDatabase(Context context, int mode){
            this.mContext = context;
            this.mode = mode;
        }

        protected Void doInBackground(Void... data) {
            ReadWriteTools rwTools = new ReadWriteTools();
            switch (mode) {
                case LOAD_ORIGINAL_PATH:
                    rwTools.initDatabaseWithOriginalPath(getActivity().getApplicationContext());
                    break;
                case LOAD_EDITED_PATH:
                    rwTools.initDatabaseWithEditedPath(getActivity().getApplicationContext());
                    break;
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Void result) {
            Tools mTools = new Tools();
            switch (mode) {
                case LOAD_ORIGINAL_PATH:
                    mTools.savePreferences(mContext, Constants.PROPERTY_INIT_DATABASE_WITH_ORIGINAL_PATH, true);
                    break;
                case LOAD_EDITED_PATH:
                    mTools.savePreferences(mContext, Constants.PROPERTY_INIT_DATABASE_WITH_EDITED_PATH, true);
                    break;
            }
            mInitDatabaseCallback.onInitDatabasePostExecute();
        }
    }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);
        unbinder = ButterKnife.bind(this, view);

        //Set the mToolbarRecipeListFragment
        if(getActivity() instanceof ToolbarAndRefreshActivity){
            ((ToolbarAndRefreshActivity) getActivity()).setToolbar(mToolbarRecipeListFragment);
        }

        //Set the refresh layout
        Tools tools = new Tools();
        tools.setRefreshLayout(getActivity(), refreshLayout);

        savedScrollPosition = 0;
        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(KEY_SCROLL_POSITION)){
                savedScrollPosition = savedInstanceState.getInt(KEY_SCROLL_POSITION);
            }
            if(savedInstanceState.containsKey(KEY_UPLOAD_TO_DRIVE)){
                uploadRecipesToDrive = savedInstanceState.getBoolean(KEY_UPLOAD_TO_DRIVE);
            }
            if(savedInstanceState.containsKey(KEY_RECIPE_LIST)){
                mRecipes = savedInstanceState.getParcelableArrayList(KEY_RECIPE_LIST);
            }
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
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                            ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(getActivity());

                            builder.setMessage(getResources().getString(R.string.write_external_explanation))
                                    .setTitle(getResources().getString(R.string.permissions_title))
                                    .setPositiveButton(getResources().getString(R.string.accept),
                                            new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            ActivityCompat.requestPermissions(getActivity(),
                                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                    Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                                        }
                                    });
                            builder.create().show();
                        } else {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                        }
                    }else{
                        createRecipe();
                    }
                }
            });
        }

        return view;
    }

    public void createRecipe(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getActivity(), EditRecipeActivity.class);
                getActivity().startActivityForResult(intent, Constants.REQUEST_CREATE_RECIPE);
            }
        }, 150);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        addRecipeButtonFAB.setOnClickListener(null);
        unbinder.unbind();
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
        Tools mTools = new Tools();

    }

    public void loadEditedRecipes(){
        InitDatabase initDatabase = new InitDatabase(getActivity().getApplicationContext(), LOAD_EDITED_PATH);
        initDatabase.execute();
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
        try {
            if (mRecyclerView != null) {
                int[] scrollPosition = new int[columnCount];
                if (mRecyclerView.getLayoutManager() != null) {
                    scrollPosition = ((StaggeredGridLayoutManager) mRecyclerView.getLayoutManager())
                            .findFirstCompletelyVisibleItemPositions(scrollPosition);
                    savedInstanceState.putSerializable(KEY_SCROLL_POSITION, scrollPosition[0]);
                }
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        if(mRecipes != null) {
            savedInstanceState.putParcelableArrayList(KEY_RECIPE_LIST, (ArrayList<RecipeItem>) mRecipes);
        }
        savedInstanceState.putBoolean(KEY_UPLOAD_TO_DRIVE, uploadRecipesToDrive);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(getActivity() instanceof ToolbarAndRefreshActivity){
            if(isResumed()){
                Tools tools = new Tools();
                tools.showRefreshLayout(getActivity());
            }else {
                ((ToolbarAndRefreshActivity) getActivity()).needToShowRefresh = true;
            }
        }
        //return new RecipeListLoader(getActivity().getApplicationContext());
        Uri CONTENT_URI = CocinaConRollContentProvider.CONTENT_URI_RECIPES;
        String sortOrder = RecipesTable.FIELD_NAME_NORMALIZED + " asc ";
        return new CursorLoader(getActivity(), CONTENT_URI, null, null, null, sortOrder);
    }



    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i("", "+++ onLoadFinished() called! +++");
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools();
        mRecipes = dbTools.getRecipesFromCursor(data);
        Tools mTools = new Tools();
        if(mRecipes.size() == 0 || !mTools.getBooleanFromPreferences(getActivity(), Constants.PROPERTY_INIT_DATABASE_WITH_ORIGINAL_PATH)){
            initDatabaseText.setVisibility(View.VISIBLE);
            InitDatabase initDatabase = new InitDatabase(getActivity().getApplicationContext(), LOAD_ORIGINAL_PATH);
            initDatabase.execute();
            return;
        }
        if(mTools.getBooleanFromPreferences(getActivity(), Constants.PROPERTY_RELOAD_NEW_ORIGINALS)){
            ReadWriteTools rwTools = new ReadWriteTools();
            rwTools.loadNewFilesAndInsertInDatabase(getActivity().getApplicationContext());
            mTools.savePreferences(getActivity(), Constants.PROPERTY_RELOAD_NEW_ORIGINALS, false);
            ((RecipeListActivity)getActivity()).restartLoader();
            return;
        }

        setData();
        ((RecipeListActivity)getActivity()).performClickInDrawerIfNecessary();

        if(!mTools.getBooleanFromPreferences(getActivity(), Constants.PROPERTY_UPLOADED_RECIPES_ON_FIRST_BOOT)){
            for(RecipeItem recipe : mRecipes){
                if((recipe.getState() & (Constants.FLAG_EDITED | Constants.FLAG_OWN)) != 0){
                    recipe.setState(Constants.FLAG_PENDING_UPLOAD_TO_DRIVE);
                    dbTools.updateStateById(getActivity().getApplicationContext(),
                            recipe.get_id(), recipe.getState());
                    recipe.setVersion(recipe.getVersion() + 1);
                    dbTools.updatePathsAndVersion(getActivity().getApplicationContext(), recipe);

                }
            }
            mTools.savePreferences(getActivity(), Constants.PROPERTY_UPLOADED_RECIPES_ON_FIRST_BOOT, true);
        }else if(((RecipeListActivity)getActivity()).checkIfCloudBackupAllowed() && uploadRecipesToDrive){
            for(RecipeItem recipe : mRecipes){
                if((recipe.getState() & Constants.FLAG_PENDING_UPLOAD_TO_DRIVE) != 0){
                    ((RecipeListActivity)getActivity()).uploadRecipeToDrive(recipe);
                }
            }
            uploadRecipesToDrive = false;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(mRecyclerView != null) {
            mRecyclerView.setAdapter(null);
            Tools tools = new Tools();
            tools.hideRefreshLayout(getActivity());
        }
    }

    private void setData(){
        initDatabaseText.setVisibility(View.GONE);
        //orderRecipesByName();
        ((ToolbarAndRefreshActivity) getActivity()).needToShowRefresh = false;
        if(isResumed()) {
            Tools tools = new Tools();
            tools.hideRefreshLayout(getActivity());
        }

        RecipeListRecyclerViewAdapter adapter = new RecipeListRecyclerViewAdapter(getActivity(), mRecipes);
        adapter.setHasStableIds(true);
        adapter.setOnCardClickListener(this);
        mRecyclerView.setHasFixedSize(true);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            SlideInBottomAnimationAdapter slideAdapter = wrapAdapter(adapter);
            mRecyclerView.setAdapter(slideAdapter);
        }else{
            mRecyclerView.setAdapter(adapter);
        }
        //mRecyclerView.setAdapter(adapter);
        columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);


        mRecyclerView.setLayoutManager(sglm);
        mRecyclerView.scrollToPosition(savedScrollPosition);
        //Set the fast Scroller
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(fastScroller != null) {
                fastScroller.setRecyclerView(mRecyclerView);
            }
        }

        //set the number of recipes
        String nRecipes = String.format(getResources().getString(R.string.recipes), mRecipes.size());
        nRecipesInRecipeList.setText(nRecipes);
    }

    private SlideInBottomAnimationAdapter wrapAdapter(RecipeListRecyclerViewAdapter adapter){
        SlideInBottomAnimationAdapter slideAdapter = new SlideInBottomAnimationAdapter(adapter);
        slideAdapter.setInterpolator(new OvershootInterpolator(2.0f));
        slideAdapter.setDuration(2000);
        return slideAdapter;
    }

    @Override
    public void onCardClick(View view, RecipeItem recipeItem) {
        showRecipeDetails(recipeItem);
    }



    private void showRecipeDetails(RecipeItem recipeItem){
        //interstitial
        Tools tools = new Tools();
        int number = tools.getIntegerFromPreferences(getActivity().getApplicationContext(), Constants.PREFERENCE_INTERSTITIAL);
        if(number<0 || number>Constants.N_RECIPES_TO_INTERSTICIAL){
            number = 0;
        }
        CommonRecipeOperations commonRecipeOperations = new CommonRecipeOperations(getActivity(), recipeItem);
        recipeItem = commonRecipeOperations.loadRecipeDetailsFromRecipeCard();

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
        Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.KEY_RECIPE, recipeToShow);
        intent.putExtras(bundle);
        ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
        // Now we can start the Activity, providing the activity options as a bundle
        ActivityCompat.startActivityForResult(getActivity(), intent, Constants.REQUEST_DETAILS, activityOptions.toBundle());

        recipeToShow = null;

    }


    public void filterRecipes(String filter) {
        lastFilter = filter;
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools();
        String type = "";
        int iconResource = 0;
        if(filter.compareTo(Constants.FILTER_ALL_RECIPES) == 0) {
            type = getResources().getString(R.string.all_recipes);
            mRecipes = dbTools.searchRecipesInDatabase(getActivity().getApplicationContext());
            iconResource = R.drawable.ic_all_24;
        }else if(filter.compareTo(Constants.FILTER_MAIN_COURSES_RECIPES) == 0){
            type = getResources().getString(R.string.main_courses);
            mRecipes = dbTools.searchRecipesInDatabase(getActivity().getApplicationContext(), RecipesTable.FIELD_TYPE, Constants.TYPE_MAIN);
            iconResource = R.drawable.ic_main_24;
        }else if(filter.compareTo(Constants.FILTER_STARTER_RECIPES) == 0){
            type = getResources().getString(R.string.starters);
            mRecipes = dbTools.searchRecipesInDatabase(getActivity().getApplicationContext(),
                    RecipesTable.FIELD_TYPE, Constants.TYPE_STARTERS);
            iconResource = R.drawable.ic_starters_24;
        }else if(filter.compareTo(Constants.FILTER_DESSERT_RECIPES) == 0){
            type = getResources().getString(R.string.desserts);
            mRecipes = dbTools.searchRecipesInDatabase(getActivity().getApplicationContext(),
                    RecipesTable.FIELD_TYPE, Constants.TYPE_DESSERTS);
            iconResource = R.drawable.ic_dessert_24;
        }else if(filter.compareTo(Constants.FILTER_VEGETARIAN_RECIPES) == 0){
            type = getResources().getString(R.string.vegetarians);
            mRecipes = dbTools.searchRecipesInDatabase(getActivity().getApplicationContext(),
                    RecipesTable.FIELD_VEGETARIAN, 1);
            iconResource = R.drawable.ic_vegetarians_24;
        }else if(filter.compareTo(Constants.FILTER_FAVOURITE_RECIPES) == 0){
            type = getResources().getString(R.string.favourites);
            mRecipes = dbTools.searchRecipesInDatabase(getActivity().getApplicationContext(),
                    RecipesTable.FIELD_FAVORITE, 1);
            iconResource = R.drawable.ic_favorite_black_24dp;
        }else if(filter.compareTo(Constants.FILTER_OWN_RECIPES) == 0){
            type = getResources().getString(R.string.own_recipes);
            mRecipes = dbTools.searchRecipesInDatabaseByState(getActivity().getApplicationContext(),
                    Constants.FLAG_EDITED | Constants.FLAG_OWN);
            iconResource = R.drawable.ic_own_24;
        }else if(filter.compareTo(Constants.FILTER_LATEST_RECIPES) == 0){
            type = getResources().getString(R.string.last_downloaded);
            Tools mTools = new Tools();
            mRecipes = dbTools.searchRecipesInDatabase(getActivity().getApplicationContext(),
                    RecipesTable.FIELD_DATE, mTools.getTimeframe());
            iconResource = R.drawable.ic_latest_24;
        }
        typeRecipesInRecipeList.setText(type);
        String nrecipes = String.format(getResources().getString(R.string.recipes), mRecipes.size());
        nRecipesInRecipeList.setText(nrecipes);
        typeIconInRecipeList.setImageDrawable(ContextCompat.getDrawable(getActivity(), iconResource));
        //Change the adapter
        RecipeListRecyclerViewAdapter newAdapter = new RecipeListRecyclerViewAdapter(getActivity(), mRecipes);
        newAdapter.setHasStableIds(true);
        newAdapter.setOnCardClickListener(this);
        mRecyclerView.setHasFixedSize(true);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            SlideInBottomAnimationAdapter newSlideAdapter = wrapAdapter(newAdapter);
            mRecyclerView.swapAdapter(newSlideAdapter, false);
        }else{
            mRecyclerView.swapAdapter(newAdapter, false);
        }
        //mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);

        /*adapter = newAdapter;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            slideAdapter = newSlideAdapter;
        }*/
        mRecyclerView.setLayoutManager(sglm);
        mRecyclerView.scrollToPosition(0);

        //Set the fast Scroller
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && fastScroller != null) {
            fastScroller.setRecyclerView(mRecyclerView);
        }
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
        if(mRecipes == null){
            return;
        }
        for(int i=0; i<mRecipes.size(); i++){
            if(mRecipes.get(i).get_id().intValue() == recipe.get_id().intValue()){
                mRecipes.remove(i);
                mRecipes.add(i, recipe);
                filterRecipes(lastFilter);
            }
        }
    }

    public void createRecipe(RecipeItem recipe) {
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools();
        dbTools.addRecipeToArrayAndDatabase(getActivity().getApplicationContext(), mRecipes, recipe);
        filterRecipes(lastFilter);
    }

    public void searchAndShow(String name) {
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools();
        name = dbTools.getNormalizedString(name);
        List<RecipeItem> coincidences = dbTools.searchRecipesInDatabase(getActivity().getApplicationContext(),
                RecipesTable.FIELD_NAME_NORMALIZED, dbTools.getNormalizedString(name));
        if (coincidences.size() > 0) {
            showRecipeDetails(coincidences.get(0));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mInitDatabaseCallback = (TaskCallback) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mInitDatabaseCallback = null;
    }

}



