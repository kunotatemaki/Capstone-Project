package com.rukiasoft.androidapps.cocinaconroll;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.rukiasoft.androidapps.cocinaconroll.database.SearchableActivity;
import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;
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

    private RecipeListFragment mRecipeListFragment;
    private SearchView mSearchView;
    int cx;
    int cy;
    ToolbarAndRefreshActivity mActivity;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipe_list, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mRecipeListFragment = (RecipeListFragment) getFragmentManager().findFragmentById(R.id.list_recipes_fragment);
                final Toolbar toolbar = mRecipeListFragment.getToolbar();
                if (toolbar == null)
                    return true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = mActivity.getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(ContextCompat.getColor(mActivity, R.color.ColorPrimaryDark));
                    toolbar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            v.removeOnLayoutChangeListener(this);
                            // get the top right corner of the view for the clipping circle
                            cx = toolbar.getLeft() + toolbar.getRight();
                            cy = toolbar.getTop() + toolbar.getBottom();

                            Animator animator = ViewAnimationUtils.createCircularReveal(
                                    toolbar,
                                    cx,
                                    cy,
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
                } else {
                    toolbar.setBackgroundResource(R.color.ColorPrimary);
                }

                //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(mActivity, R.color.ColorPrimary)));
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mRecipeListFragment = (RecipeListFragment) getFragmentManager().findFragmentById(R.id.list_recipes_fragment);
                final Toolbar toolbar = mRecipeListFragment.getToolbar();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = mActivity.getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(ContextCompat.getColor(mActivity, R.color.ColorPrimarySearchDark));
                    toolbar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            v.removeOnLayoutChangeListener(this);
                            // get the top right corner of the view for the clipping circle
                            cx = toolbar.getLeft() + toolbar.getRight();
                            cy = toolbar.getTop() + toolbar.getBottom();

                            Animator animator = ViewAnimationUtils.createCircularReveal(
                                    toolbar,
                                    cx,
                                    cy,
                                    0,
                                    (float) Math.hypot(toolbar.getWidth(), toolbar.getHeight()));

                            // Set a natural ease-in/ease-out interpolator.
                            animator.setInterpolator(new AccelerateDecelerateInterpolator());

                            // make the view visible and start the animation
                            animator.start();
                        }
                    });
                }
                toolbar.setBackgroundResource(R.color.ColorPrimarySearch);
                //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(mActivity, R.color.ColorPrimarySearch)));
                return true;
            }

        });
        mSearchView = (SearchView) searchMenuItem.getActionView();
        cx = (int) mSearchView.getX();
        cy = (int) mSearchView.getY();//mSearchView.setOnQueryTextListener(listener);
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
                        String title = menuItem.getTitle().toString();

                        switch (menuItem.getItemId()) {
                            case R.id.menu_all_recipes:
                                Tools tools = new Tools();
                                title = tools.getApplicationName(getApplicationContext());
                                break;
                            case R.id.menu_starters:
                                //showTitle(getResources().getString(R.string.starters));
                                break;
                            case R.id.menu_main_courses:
                                //showTitle(getResources().getString(R.string.main_courses));
                                break;
                            case R.id.menu_desserts:
                                //showTitle(getResources().getString(R.string.desserts));
                                break;
                            case R.id.menu_vegetarians:
                                //showTitle(getResources().getString(R.string.vegetarians));
                                break;
                            case R.id.menu_favorites:
                                //showTitle(getResources().getString(R.string.favourites));
                                break;
                            case R.id.menu_own_recipes:
                                //showTitle(getResources().getString(R.string.own_recipes));
                                break;
                            case R.id.menu_last_downloaded:
                                //showTitle(getResources().getString(R.string.last_downloaded));
                                break;
                        }

                        //menuItem.setChecked(true);
                        showTitle(title);

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

    }
    /**
     * Show the selected text in the supportActionbar
     */
    private void showTitle(String title) {
        if(getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);
    }

    /*private void getFilteredRecipes(String filter){
        if(mRecipeListFragment == null){
            mRecipeListFragment = (RecipeListFragment) getFragmentManager().findFragmentById(R.id.list_recipes_fragment);
        }
        mRecipeListFragment.getFilteredRecipes(filter);
    }*/

}
