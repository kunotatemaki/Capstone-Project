package com.rukiasoft.androidapps.cocinaconroll;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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

    ToolbarAndRefreshActivity mActivity;

    //Listener for the searchview widget
    SearchView.OnQueryTextListener listener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            //getFilteredRecipes(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String query) {
            mRecipeListFragment = (RecipeListFragment) getFragmentManager().findFragmentById(R.id.list_recipes_fragment);
            if(mRecipeListFragment != null){
                mRecipeListFragment.onQueryTextChange(query);
            }
            return true;
        }
    };

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
        mSearchView = (SearchView) searchMenuItem.getActionView();
        //mSearchView.setOnQueryTextListener(listener);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo prueba = searchManager.getSearchableInfo(new ComponentName(this, SearchableActivity.class));
        //the searchable is in another activity, so instead of getcomponentname(), create a new one for that activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchableActivity.class)));
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {

                return false;
            }
        });
        mSearchView.setOnSearchClickListener(new SearchView.OnClickListener() {

            @Override
            public void onClick(View v) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#95CDBA")));
                return;
            }
        });

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
