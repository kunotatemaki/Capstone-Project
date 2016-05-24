package com.rukiasoft.androidapps.cocinaconroll.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import com.rukiasoft.androidapps.cocinaconroll.classes.ZipItem;
import com.rukiasoft.androidapps.cocinaconroll.database.DatabaseRelatedTools;
import com.rukiasoft.androidapps.cocinaconroll.gcm.GetZipsAsyncTask;
import com.rukiasoft.androidapps.cocinaconroll.gcm.QuickstartPreferences;
import com.rukiasoft.androidapps.cocinaconroll.gcm.RegistrationIntentService;
import com.rukiasoft.androidapps.cocinaconroll.utilities.CommonRecipeOperations;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;
import com.rukiasoft.androidapps.cocinaconroll.utilities.ReadWriteTools;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class RecipeListActivity extends SigningDriveActivity implements RecipeListFragment.TaskCallback{

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = LogHelper.makeLogTag(RecipeListActivity.class);
    private static final int REQUEST_CODE_SETTINGS = 20;
    private static final int REQUEST_CODE_ANIMATION = 21;
    private static final int REQUEST_CODE_DRIVE = 22;
    private static final String KEY_DRIVE_RECIPES_CHECKED = Constants.PACKAGE_NAME + ".drive_recipes_checked";
    private static final String KEY_STARTED = Constants.PACKAGE_NAME + ".started";
    //private static final String KEY_NEED_TO_SEND_RECIPES_TO_DRIVE = Constants.PACKAGE_NAME + ".need_to_send_recipes_to_drive";


    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.navview)
    NavigationView navigationView;
    @BindView(R.id.adview_list)
    AdView mAdViewList;
    private Unbinder unbinder;

    private MenuItem searchMenuItem;
    private int magnifyingX;
    private int magnifyingY;
    private int openCircleRevealX;
    private int openCircleRevealY;
    private boolean started = false;
    private boolean animate;
    private String lastFilter;
    private boolean driveRecipesChecked = false;
    DriveServiceReceiver driveServiceReceiver;

    private final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
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
    public void onInitDatabasePostExecute() {
        restartLoader();
    }

    // Broadcast receiver for receiving status updates from the IntentService
    private class DriveServiceReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Constants.ACTION_BROADCAST_UPLOADED_RECIPE)){
                if(intent.hasExtra(Constants.KEY_RECIPE)){
                    RecipeItem recipeItem = intent.getParcelableExtra(Constants.KEY_RECIPE);
                    recipeItem.removeState(Constants.FLAG_PENDING_UPLOAD_TO_DRIVE);
                    recipeItem.setState(Constants.FLAG_SINCRONIZED_WITH_DRIVE);
                    DatabaseRelatedTools dbTools = new DatabaseRelatedTools();
                    dbTools.updateStateById(getApplicationContext(), recipeItem.get_id(), recipeItem.getState());
                }
            }else if(intent.getAction().equals(Constants.ACTION_BROADCAST_DELETED_RECIPE)){
                if(intent.hasExtra(Constants.KEY_RECIPE)){
                    RecipeItem recipeItem = intent.getParcelableExtra(Constants.KEY_RECIPE);
                    removeRecipeFromDiskAndDatabase(recipeItem);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        unbinder = ButterKnife.bind(this);

        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(KEY_STARTED)) {
                started = savedInstanceState.getBoolean(KEY_STARTED);
            }
            if(savedInstanceState.containsKey(Constants.KEY_TYPE)) {
                lastFilter = savedInstanceState.getString(Constants.KEY_TYPE);
            }
            if(savedInstanceState.containsKey(KEY_DRIVE_RECIPES_CHECKED)) {
                driveRecipesChecked = savedInstanceState.getBoolean(KEY_DRIVE_RECIPES_CHECKED);
            }
           // shownToAllowDrive = savedInstanceState.getBoolean(KEY_ALLOWED_DRIVE);
        }

        Tools mTools = new Tools();

        if(mTools.getAppVersion(getApplication()) > mTools.getIntegerFromPreferences(this, Constants.PROPERTY_APP_VERSION_STORED)){
            //first instalation, or recently updated app
            mTools.savePreferences(this, QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
            mTools.savePreferences(this, Constants.PROPERTY_APP_VERSION_STORED, mTools.getAppVersion(getApplication()));
        }

        //start animation if needed
        if(!started){
            Intent animationIntent = new Intent(this, AnimationActivity.class);
            //Intent animationIntent = new Intent(this, ShowSigningActivity.class);
            startActivityForResult(animationIntent, REQUEST_CODE_ANIMATION);
        }


        lastFilter = Constants.FILTER_ALL_RECIPES;
        if(getIntent() != null && getIntent().hasExtra(Constants.KEY_TYPE)){
            lastFilter = getIntent().getStringExtra(Constants.KEY_TYPE);
        }
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
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

        //check if we need to check for new recipes
        long expirationTime =
                mTools.getLongFromPreferences(this, Constants.PROPERTY_EXPIRATION_TIME);
        if (mTools.getBooleanFromPreferences(this, QuickstartPreferences.SENT_TOKEN_TO_SERVER) &&
                (expirationTime == Long.MIN_VALUE || System.currentTimeMillis() > expirationTime)) {
            GetZipsAsyncTask getZipsAsyncTask = new GetZipsAsyncTask(this);
            getZipsAsyncTask.execute();
        }

        // The filter's action is BROADCAST_ACTION
        IntentFilter mStatusIntentFilter = new IntentFilter();
        mStatusIntentFilter.addAction(Constants.ACTION_BROADCAST_UPLOADED_RECIPE);
        mStatusIntentFilter.addAction(Constants.ACTION_BROADCAST_DELETED_RECIPE);


        // Instantiates a new DownloadStateReceiver
        driveServiceReceiver =
                new DriveServiceReceiver();
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                driveServiceReceiver,
                mStatusIntentFilter);
        if(savedInstanceState == null) {
            clearGarbage();
        }

    }


    @Override
    public void onSaveInstanceState(Bundle bundle){
        bundle.putBoolean(KEY_STARTED, true);
        bundle.putBoolean(KEY_DRIVE_RECIPES_CHECKED, driveRecipesChecked);
        bundle.putString(Constants.KEY_TYPE, lastFilter);
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onDestroy(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(driveServiceReceiver);
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        RecipeListFragment mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().findFragmentById(R.id.list_recipes_fragment);
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
        Tools tools = new Tools();
        switch (requestCode) {
            case Constants.REQUEST_DETAILS:
                //return from RecipeDetailsActivity
                if (resultCode == Constants.RESULT_DELETE_RECIPE && intentData != null && intentData.hasExtra(Constants.KEY_RECIPE)) {
                    RecipeItem recipe = intentData.getParcelableExtra(Constants.KEY_RECIPE);
                    if (recipe != null) {
                        if((recipe.getState() & Constants.FLAG_SINCRONIZED_WITH_DRIVE) != 0){
                            deleteRecipeFromDrive(recipe);
                        }
                        removeRecipeFromDiskAndDatabase(recipe);
                    }
                }else if(resultCode == Constants.RESULT_UPDATE_RECIPE){
                    if (intentData != null && intentData.hasExtra(Constants.KEY_RECIPE)) {
                        RecipeItem recipe = intentData.getParcelableExtra(Constants.KEY_RECIPE);
                        RecipeListFragment mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().findFragmentById(R.id.list_recipes_fragment);
                        if (mRecipeListFragment != null) {
                            mRecipeListFragment.updateRecipe(recipe);
                        }
                    }
                }
                break;
            case Constants.REQUEST_EDIT_RECIPE:
                if(resultCode == Constants.RESULT_UPDATE_RECIPE && intentData != null && intentData.hasExtra(Constants.KEY_RECIPE)) {
                    RecipeItem recipe = intentData.getParcelableExtra(Constants.KEY_RECIPE);
                    CommonRecipeOperations commonRecipeOperations = new CommonRecipeOperations(this, recipe);
                    String oldPicture = "";
                    if (intentData.hasExtra(Constants.KEY_DELETE_OLD_PICTURE)) {
                        oldPicture = intentData.getStringExtra(Constants.KEY_DELETE_OLD_PICTURE);
                    }
                    commonRecipeOperations.updateRecipe(oldPicture);
                    RecipeListFragment mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().findFragmentById(R.id.list_recipes_fragment);
                    if (mRecipeListFragment != null) {
                        mRecipeListFragment.updateRecipe(recipe);
                    }
                }
                break;
            case Constants.REQUEST_CREATE_RECIPE:
                if (resultCode == Constants.RESULT_UPDATE_RECIPE && intentData != null && intentData.hasExtra(Constants.KEY_RECIPE)) {
                    RecipeItem recipe = intentData.getParcelableExtra(Constants.KEY_RECIPE);
                    RecipeListFragment mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().findFragmentById(R.id.list_recipes_fragment);
                    ReadWriteTools readWriteTools = new ReadWriteTools();
                    String path = readWriteTools.saveRecipeOnEditedPath(getApplicationContext(), recipe);
                    recipe.setPathRecipe(path);
                    uploadRecipeToDrive(recipe);
                    if (mRecipeListFragment != null) {
                        mRecipeListFragment.createRecipe(recipe);
                    }
                }
                break;
            case REQUEST_CODE_RESOLUTION:
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    connectToDrive(true);
                }
                break;
            case REQUEST_CODE_ANIMATION:

                if(!tools.getBooleanFromPreferences(this, Constants.PROPERTY_AVOID_FIRST_CHECK_GOOGLE_ACCOUNT)) {
                    tools.savePreferences(this, Constants.PROPERTY_AVOID_FIRST_CHECK_GOOGLE_ACCOUNT, true);
                    Intent intent = new Intent(this, ShowSigningActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_DRIVE);
                }else{
                    if(!tools.getBooleanFromPreferences(this, Constants.PROPERTY_INIT_DATABASE_WITH_EDITED_PATH)) {
                        askForPermissionAndLoadEditedRecipes();
                    }
                }
                break;
            case REQUEST_CODE_DRIVE:
                if(!tools.getBooleanFromPreferences(this, Constants.PROPERTY_INIT_DATABASE_WITH_EDITED_PATH)) {
                    askForPermissionAndLoadEditedRecipes();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, intentData);
        }
    }

    private void askForPermissionAndLoadEditedRecipes(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                android.support.v7.app.AlertDialog.Builder builder =
                        new android.support.v7.app.AlertDialog.Builder(this);

                builder.setMessage(getResources().getString(R.string.read_external_explanation))
                        .setTitle(getResources().getString(R.string.permissions_title))
                        .setPositiveButton(getResources().getString(R.string.accept),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        ActivityCompat.requestPermissions(RecipeListActivity.this,
                                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                Constants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                                    }
                                });
                builder.create().show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        Constants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }else{
            RecipeListFragment fragment = (RecipeListFragment)getSupportFragmentManager().findFragmentById(R.id.list_recipes_fragment);
            if(fragment != null) {
                fragment.loadEditedRecipes();
            }
        }
    }

    private void removeRecipeFromDiskAndDatabase(RecipeItem recipe){
        ReadWriteTools rwTools = new ReadWriteTools();
        rwTools.deleteRecipe(recipe);
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools();
        dbTools.removeRecipefromDatabase(getApplicationContext(), recipe.get_id());
        restartLoader();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipe_list, menu);
        searchMenuItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                RecipeListFragment mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().findFragmentById(R.id.list_recipes_fragment);
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
                RecipeListFragment mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().findFragmentById(R.id.list_recipes_fragment);
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
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
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
                startActivityForResult(finalIntent, REQUEST_CODE_SETTINGS);
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
        if(getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        }
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        RecipeListFragment mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().findFragmentById(R.id.list_recipes_fragment);
                        if(mRecipeListFragment != null) {
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

        if(!driveRecipesChecked && checkIfCloudBackupAllowed()){
            driveRecipesChecked = getRecipesFromDrive();
        }
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
            MenuItemCompat.collapseActionView(searchMenuItem);
        }
    }

    public void performClickInDrawerIfNecessary() {
        if(lastFilter.equals(Constants.FILTER_LATEST_RECIPES)){
            navigationView.setCheckedItem(R.id.menu_last_downloaded);
            RecipeListFragment mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().findFragmentById(R.id.list_recipes_fragment);
            if(mRecipeListFragment != null) {
                mRecipeListFragment.filterRecipes(Constants.FILTER_LATEST_RECIPES);
            }
        } else {
            navigationView.setCheckedItem(R.id.menu_all_recipes);
        }
    }

    public void restartLoader(){
        RecipeListFragment mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().findFragmentById(R.id.list_recipes_fragment);
        if(mRecipeListFragment != null) {
            getSupportLoaderManager().restartLoader(Constants.LOADER_ID, null, mRecipeListFragment);
        }
    }

    private void clearGarbage(){
        //creo fichero nomedia
        ReadWriteTools rwTools = new ReadWriteTools();
        File file = new File(rwTools.getEditedStorageDir() + ".nomedia");
        try {
            if(!file.exists())
                file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //borro temporales de la cámara
        List<String> list = rwTools.loadFiles(getApplicationContext(), null, true);
        for(int i=0; i<list.size(); i++) {
            if(list.get(i).contains(Constants.TEMP_CAMERA_NAME)){
                file = new File(rwTools.getEditedStorageDir() + list.get(i));
                if(file.exists())
                    file.delete();
            }
        }

        //veo si hay algún zip en la base de datos que no tenga el formato correcto
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools();
        List<ZipItem> listZips = dbTools.getAllZips(getApplicationContext());
        for(ZipItem zip : listZips){
            if(!zip.getName().contains(".zip")){
                dbTools.removeZipfromDatabase(getApplicationContext(), zip.getId());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    RecipeListFragment mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().
                            findFragmentById(R.id.list_recipes_fragment);
                    if(mRecipeListFragment != null) {
                        mRecipeListFragment.createRecipe();
                    }
                } else {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(this);

                    builder.setMessage(getResources().getString(R.string.write_external_denied))
                            .setTitle(getResources().getString(R.string.permissions_title))
                            .setPositiveButton(getResources().getString(R.string.accept),
                                    new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    builder.create().show();
                }
            }
            case Constants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    RecipeListFragment mRecipeListFragment = (RecipeListFragment) getSupportFragmentManager().
                            findFragmentById(R.id.list_recipes_fragment);
                    if(mRecipeListFragment != null) {
                        mRecipeListFragment.loadEditedRecipes();
                    }
                } else {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(this);

                    builder.setMessage(getResources().getString(R.string.read_external_denied))
                            .setTitle(getResources().getString(R.string.permissions_title))
                            .setPositiveButton(getResources().getString(R.string.accept),
                                    new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    builder.create().show();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }


    }

}
