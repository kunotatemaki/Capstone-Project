package com.rukiasoft.androidapps.cocinaconroll;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RecipeListActivity extends ToolbarAndRefreshActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = LogHelper.makeLogTag(RecipeListActivity.class);
    public static final String KEY_RECIPE = Constants.PACKAGE_NAME + ".recipe";


    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.navview)
    NavigationView navigationView;

    MenuItem searchMenuItem;
    private RecipeListFragment mRecipeListFragment;
    private SearchView mSearchView;
    int magnifyingX, magnifyingY, openCircleRevealX, openCircleRevealY;

    ToolbarAndRefreshActivity mActivity;
    private boolean animate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        ButterKnife.bind(this);
        mActivity = this;

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            //TODO - comprobar el service tanto con el endpoint como con el servidor raspberry
            //Intent intent = new Intent(this, RegistrationIntentService.class);
            //startService(intent);
            //new GcmRegistrationAsyncTask(this).execute();
        }

        //Set default values for preferences
        Tools tools = new Tools();
        if (tools.hasVibrator(getApplicationContext())) {
            setDefaultValuesForOptions(R.xml.options);
        }else{
            setDefaultValuesForOptions(R.xml.options_not_vibrate);
        }

        setupDrawerLayout();
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        mRecipeListFragment = (RecipeListFragment) getFragmentManager().findFragmentById(R.id.list_recipes_fragment);
        if(mRecipeListFragment != null && intent.hasExtra(KEY_RECIPE)){
            String name = intent.getStringExtra(KEY_RECIPE);
            //TODO - buscar la receta sabiendo el nombre y llamar a showRecipeDetails
            //mRecipeListFragment.showRecipeDetails();
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
                mRecipeListFragment = (RecipeListFragment) getFragmentManager().findFragmentById(R.id.list_recipes_fragment);
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
                mRecipeListFragment = (RecipeListFragment) getFragmentManager().findFragmentById(R.id.list_recipes_fragment);
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
        mSearchView = (SearchView) searchMenuItem.getActionView();
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //the searchable is in another activity, so instead of getcomponentname(), create a new one for that activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchableActivity.class)));


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
            default:
                return super.onOptionsItemSelected(item);
        }
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
                        mRecipeListFragment = (RecipeListFragment) getFragmentManager().findFragmentById(R.id.list_recipes_fragment);
                        switch (menuItem.getItemId()) {
                            case R.id.menu_all_recipes:
                                mRecipeListFragment.filterRecipes(Constants.FILTER_ALL_RECIPES);
                                break;
                            case R.id.menu_starters:
                                mRecipeListFragment.filterRecipes(Constants.FILTER_STARTER_RECIPES);
                                break;
                            case R.id.menu_main_courses:
                                mRecipeListFragment.filterRecipes(Constants.FILTER_MAIN_COURSES_RECIPES);
                                break;
                            case R.id.menu_desserts:
                                mRecipeListFragment.filterRecipes(Constants.FILTER_DESSERT_RECIPES);
                                break;
                            case R.id.menu_vegetarians:
                                mRecipeListFragment.filterRecipes(Constants.FILTER_VEGETARIAN_RECIPES);
                                break;
                            case R.id.menu_favorites:
                                mRecipeListFragment.filterRecipes(Constants.FILTER_FAVOURITE_RECIPES);
                                break;
                            case R.id.menu_own_recipes:
                                mRecipeListFragment.filterRecipes(Constants.FILTER_OWN_RECIPES);
                                break;
                            case R.id.menu_last_downloaded:
                                mRecipeListFragment.filterRecipes(Constants.FILTER_LATEST_RECIPES);
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
                    magnifyingX = location[0] + menuButton.getWidth()/2;
                    magnifyingY = location[1];
                    // Now you can get rid of this listener
                    if(magnifyingX != 0 && magnifyingY != 0 && viewTreeObserver.isAlive()) {
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

    }


    public void closeSearchView(){
        animate = false;
        if(searchMenuItem != null){
            searchMenuItem.collapseActionView();
        }
    }
}
