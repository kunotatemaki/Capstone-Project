package com.rukiasoft.androidapps.cocinaconroll.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.classes.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.database.DatabaseRelatedTools;
import com.rukiasoft.androidapps.cocinaconroll.gcm.QuickstartPreferences;
import com.rukiasoft.androidapps.cocinaconroll.gcm.RegistrationIntentService;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;
import com.rukiasoft.androidapps.cocinaconroll.utilities.ReadWriteTools;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RecipeListActivity extends ToolbarAndRefreshActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = LogHelper.makeLogTag(RecipeListActivity.class);

    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.navview)
    NavigationView navigationView;
    @Bind(R.id.adview_list)
    AdView mAdViewList;



    MenuItem searchMenuItem;
    private RecipeListFragment mRecipeListFragment;
    int magnifyingX, magnifyingY, openCircleRevealX, openCircleRevealY;
    boolean started = false;
    ToolbarAndRefreshActivity mActivity;
    private boolean animate;
    private String lastFilter;


    public final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        ButterKnife.bind(this);

        if(savedInstanceState != null && savedInstanceState.containsKey(Constants.KEY_STARTED)){
            started = savedInstanceState.getBoolean(Constants.KEY_STARTED);
            lastFilter = savedInstanceState.getString(Constants.KEY_TYPE);
        }
        if(!started){
            Intent animationIntent = new Intent(this, AnimationActivity.class);
            startActivity(animationIntent);
        }
        
        mActivity = this;
        Tools mTools = new Tools();
        lastFilter = Constants.FILTER_ALL_RECIPES;
        if(getIntent() != null && getIntent().hasExtra(Constants.KEY_TYPE)){
            lastFilter = getIntent().getStringExtra(Constants.KEY_TYPE);
        }
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            //TODO - comprobar el service con el servidor raspberry
            if(!mTools.getBooleanFromPreferences(this, QuickstartPreferences.SENT_TOKEN_TO_SERVER)) {
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }

        //Set default values for preferences
        if (mTools.hasVibrator(getApplicationContext())) {
            setDefaultValuesForOptions(R.xml.options);
        }else{
            setDefaultValuesForOptions(R.xml.options_not_vibrate);
        }

        setupDrawerLayout();

        //set up advertises
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("B29C1F71528C79C864D503360C5225C0")  // My Xperia Z3 test device
                .setGender(AdRequest.GENDER_FEMALE)
                .build();

        mAdViewList.loadAd(adRequest);




    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        bundle.putBoolean(Constants.KEY_STARTED, true);
        bundle.putString(Constants.KEY_TYPE, lastFilter);
        super.onSaveInstanceState(bundle);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();

    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().findFragmentById(R.id.list_recipes_fragment);
        if(mRecipeListFragment != null){
            if(intent != null && intent.hasExtra(Constants.KEY_RECIPE)) {
                String name = intent.getStringExtra(Constants.KEY_RECIPE);
                mRecipeListFragment.searchAndShow(name);
            }
            if(intent != null && intent.hasExtra(Constants.KEY_TYPE)){
                lastFilter = intent.getStringExtra(Constants.KEY_TYPE);
                restartLoader();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intentData) {
        if(requestCode == Constants.REQUEST_DETAILS){
            //return from RecipeDetailsActivity
            if(resultCode == Constants.RESULT_DELETE_RECIPE && intentData != null && intentData.hasExtra(Constants.KEY_RECIPE)){
                RecipeItem recipe = intentData.getParcelableExtra(Constants.KEY_RECIPE);
                if(recipe != null){
                    ReadWriteTools rwTools = new ReadWriteTools(this);
                    rwTools.deleteRecipe(recipe);
                    DatabaseRelatedTools dbTools = new DatabaseRelatedTools(this);
                    dbTools.removeRecipefromDatabase(recipe.get_id());
                    restartLoader();
                }
            }else if(resultCode == Constants.RESULT_UPDATE_RECIPE && intentData != null && intentData.hasExtra(Constants.KEY_RECIPE)){
                RecipeItem recipe = intentData.getParcelableExtra(Constants.KEY_RECIPE);
                mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().findFragmentById(R.id.list_recipes_fragment);
                if(mRecipeListFragment != null){
                    mRecipeListFragment.updateRecipe(recipe);
                }
            }
        }else if(requestCode == Constants.REQUEST_CREATE_RECIPE){
            if(resultCode == Constants.RESULT_UPDATE_RECIPE && intentData != null && intentData.hasExtra(Constants.KEY_RECIPE)){
                RecipeItem recipe = intentData.getParcelableExtra(Constants.KEY_RECIPE);
                mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().findFragmentById(R.id.list_recipes_fragment);
                ReadWriteTools readWriteTools = new ReadWriteTools(this);
                String path = readWriteTools.saveRecipeOnEditedPath(recipe);
                recipe.setPathRecipe(path);
                if(mRecipeListFragment != null){
                    mRecipeListFragment.createRecipe(recipe);
                }

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipe_list, menu);
        searchMenuItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().findFragmentById(R.id.list_recipes_fragment);
                final Toolbar toolbar = mRecipeListFragment.getToolbarRecipeListFragment();
                if (toolbar == null)
                    return true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    /*Window window = mActivity.getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(ContextCompat.getColor(mActivity, R.color.ColorPrimaryDark));*/
                    if (animate) {
                        toolbar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                v.removeOnLayoutChangeListener(this);
                                Animator animator = ViewAnimationUtils.createCircularReveal(
                                        toolbar,
                                        openCircleRevealX,
                                        openCircleRevealY,
                                        (float) Math.hypot(toolbar.getWidth(), toolbar.getHeight()),
                                        0);

                                // Set a natural ease-in/ease-out interpolator.
                                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                                // make the view invisible when the animation is done
                                animator.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        toolbar.setBackgroundResource(R.color.ColorPrimary);
                                    }
                                });

                                // make the view visible and start the animation
                                animator.start();
                            }
                        });
                    } else toolbar.setBackgroundResource(R.color.ColorPrimary);
                } else {
                    toolbar.setBackgroundResource(R.color.ColorPrimary);
                }

                //show the bar and button
                mRecipeListFragment.setVisibilityWithSearchWidget(View.VISIBLE);
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().findFragmentById(R.id.list_recipes_fragment);
                final Toolbar toolbar = mRecipeListFragment.getToolbarRecipeListFragment();
                if (toolbar == null)
                    return true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    animate = true;
                    /*Window window = mActivity.getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(ContextCompat.getColor(mActivity, R.color.ColorPrimarySearchDark));*/
                    toolbar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            v.removeOnLayoutChangeListener(this);
                            Animator animator = ViewAnimationUtils.createCircularReveal(
                                    toolbar,
                                    magnifyingX,
                                    magnifyingY,
                                    0,
                                    (float) Math.hypot(toolbar.getWidth(), toolbar.getHeight()));
                            openCircleRevealX = magnifyingX;
                            openCircleRevealY = magnifyingY;
                            // Set a natural ease-in/ease-out interpolator.
                            animator.setInterpolator(new AccelerateDecelerateInterpolator());

                            // make the view visible and start the animation
                            animator.start();
                        }
                    });
                }
                toolbar.setBackgroundResource(R.color.ColorPrimarySearch);
                //hide the bar and button
                mRecipeListFragment.setVisibilityWithSearchWidget(View.GONE);
                //hide the floating button

                return true;
            }

        });
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //the searchable is in another activity, so instead of getcomponentname(), create a new one for that activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchableActivity.class)));


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_settings:
                Intent finalIntent = new Intent(this, SettingsActivity.class);
                startActivity(finalIntent);
                return true;
            case R.id.menu_thanks:
                finalIntent = new Intent(this, ThanksActivity.class);
                startActivity(finalIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            confirmExit();
        }
    }

    private void confirmExit(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int stringId = getApplicationInfo().labelRes;
        String name = getString(stringId);
        String question = String.format(getResources().getString(R.string.exit_application), name);
        builder.setTitle(getResources().getString(R.string.exit_application_title));
        builder.setMessage(question).setPositiveButton((getResources().getString(R.string.Yes)), dialogClickListener)
                .setNegativeButton((getResources().getString(R.string.No)), dialogClickListener);
        builder.show();
    }

    /**
     * Setup the drawer layout
     */
    private void setupDrawerLayout(){
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().findFragmentById(R.id.list_recipes_fragment);
                        switch (menuItem.getItemId()) {
                            case R.id.menu_all_recipes:
                                mRecipeListFragment.filterRecipes(Constants.FILTER_ALL_RECIPES);
                                lastFilter = Constants.FILTER_ALL_RECIPES;
                                break;
                            case R.id.menu_starters:
                                mRecipeListFragment.filterRecipes(Constants.FILTER_STARTER_RECIPES);
                                lastFilter = Constants.FILTER_STARTER_RECIPES;
                                break;
                            case R.id.menu_main_courses:
                                mRecipeListFragment.filterRecipes(Constants.FILTER_MAIN_COURSES_RECIPES);
                                lastFilter = Constants.FILTER_MAIN_COURSES_RECIPES;
                                break;
                            case R.id.menu_desserts:
                                mRecipeListFragment.filterRecipes(Constants.FILTER_DESSERT_RECIPES);
                                lastFilter = Constants.FILTER_DESSERT_RECIPES;
                                break;
                            case R.id.menu_vegetarians:
                                mRecipeListFragment.filterRecipes(Constants.FILTER_VEGETARIAN_RECIPES);
                                lastFilter = Constants.FILTER_VEGETARIAN_RECIPES;
                                break;
                            case R.id.menu_favorites:
                                mRecipeListFragment.filterRecipes(Constants.FILTER_FAVOURITE_RECIPES);
                                lastFilter = Constants.FILTER_FAVOURITE_RECIPES;
                                break;
                            case R.id.menu_own_recipes:
                                mRecipeListFragment.filterRecipes(Constants.FILTER_OWN_RECIPES);
                                lastFilter = Constants.FILTER_OWN_RECIPES;
                                break;
                            case R.id.menu_last_downloaded:
                                mRecipeListFragment.filterRecipes(Constants.FILTER_LATEST_RECIPES);
                                lastFilter = Constants.FILTER_LATEST_RECIPES;
                                break;
                        }

                        drawerLayout.closeDrawers();

                        return true;
                    }
                });
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void onResume(){
        super.onResume();

        if (mAdViewList != null) {
            mAdViewList.resume();
        }

        closeSearchView();
        //to start the reveal effecy from the magnifying glass
        final ViewTreeObserver viewTreeObserver = getWindow().getDecorView().getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                View menuButton = findViewById(R.id.action_search);
                // This could be called when the button is not there yet, so we must test for null
                if (menuButton != null) {
                    // Found it! Do what you need with the button
                    int[] location = new int[2];
                    menuButton.getLocationInWindow(location);
                    //Log.d(TAG, "x=" + location[0] + " y=" + location[1]);
                    magnifyingX = location[0] + menuButton.getWidth() / 2;
                    magnifyingY = location[1];
                    // Now you can get rid of this listener
                    if (magnifyingX != 0 && magnifyingY != 0 && viewTreeObserver.isAlive()) {
                        if (Build.VERSION.SDK_INT < 16) {
                            viewTreeObserver.removeGlobalOnLayoutListener(this);
                        } else {
                            viewTreeObserver.removeOnGlobalLayoutListener(this);
                        }
                    }
                }
            }
        });
    }

    public void onPause(){
        super.onPause();
        if (mAdViewList != null) {
            mAdViewList.pause();
        }
    }


    public void closeSearchView(){
        animate = false;
        if(searchMenuItem != null){
            searchMenuItem.collapseActionView();
        }
    }

    public void performClickInDrawerIfNecessary() {
        if(lastFilter.equals(Constants.FILTER_LATEST_RECIPES)){
            navigationView.setCheckedItem(R.id.menu_last_downloaded);
            mRecipeListFragment.filterRecipes(Constants.FILTER_LATEST_RECIPES);
        }else{
            navigationView.setCheckedItem(R.id.menu_all_recipes);
        }
    }

    public void restartLoader(){
        mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().findFragmentById(R.id.list_recipes_fragment);
        if(mRecipeListFragment != null) {
            getSupportLoaderManager().restartLoader(Constants.LOADER_ID, null, mRecipeListFragment);
        }
    }
}
