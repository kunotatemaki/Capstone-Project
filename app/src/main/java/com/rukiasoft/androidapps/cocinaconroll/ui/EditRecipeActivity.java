package com.rukiasoft.androidapps.cocinaconroll.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.classes.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;
import com.rukiasoft.androidapps.cocinaconroll.utilities.ReadWriteTools;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import java.lang.reflect.Field;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class EditRecipeActivity extends AppCompatActivity {

    private EditRecipePhotoFragment editRecipePhotoFragment;
    private EditRecipeIngredientsFragment editRecipeIngredientsFragment;
    private EditRecipeStepsFragment editRecipeStepsFragment;
    private RecipeItem recipe;
    private final static String TAG = LogHelper.makeLogTag(EditRecipeActivity.class);
    private final static String KEY_FRAGMENT = Constants.PACKAGE_NAME + ".fragment";
    private final static String KEY_TITLE = Constants.PACKAGE_NAME + ".title";
    private String title;
    private Tools mTools;
    @BindView(R.id.standard_toolbar) Toolbar mToolbar;
    private String oldPicture;
    private Unbinder unbinder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "onCreate");
        mTools = new Tools();
        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(Constants.KEY_RECIPE)) {
                recipe = savedInstanceState.getParcelable(Constants.KEY_RECIPE);
            }
            if(savedInstanceState.containsKey(KEY_TITLE)) {
                title = savedInstanceState.getString(KEY_TITLE);
            }
            oldPicture = savedInstanceState.getString(Constants.KEY_DELETE_OLD_PICTURE);
        }else if(getIntent() != null && getIntent().hasExtra(Constants.KEY_RECIPE)) {
            recipe = getIntent().getExtras().getParcelable(Constants.KEY_RECIPE);
            //check if the picture is previosly edited, to delete the old picture
            if(recipe == null){
                recipe = new RecipeItem();
                setResult(RESULT_CANCELED);
                finish();
            }
            if((recipe.getState()&Constants.FLAG_EDITED_PICTURE)!=0){
                oldPicture = recipe.getPathPicture();
            }
            title = getResources().getString(R.string.edit_recipe);
            recipe.setState(Constants.FLAG_EDITED);
            recipe.setState(Constants.FLAG_PENDING_UPLOAD_TO_DRIVE);
        }else{
            title = getResources().getString(R.string.create_recipe);
            recipe = new RecipeItem();
            recipe.setState(Constants.FLAG_OWN);
            recipe.setState(Constants.FLAG_PENDING_UPLOAD_TO_DRIVE);
        }

        super.onCreate(savedInstanceState);

        if(savedInstanceState != null/* && !CocinaConRollTools.isForTablet(this)*/) {
            editRecipeIngredientsFragment = (EditRecipeIngredientsFragment) getSupportFragmentManager().findFragmentByTag(EditRecipeIngredientsFragment.class.getSimpleName());
            editRecipeStepsFragment = (EditRecipeStepsFragment) getSupportFragmentManager().findFragmentByTag(EditRecipeStepsFragment.class.getSimpleName());
            editRecipePhotoFragment = (EditRecipePhotoFragment) getSupportFragmentManager().findFragmentByTag(EditRecipePhotoFragment.class.getSimpleName());
        }

        setContentView(R.layout.activity_edit_recipe);
        unbinder = ButterKnife.bind(this);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            if(getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setTitle(title);
            }
        }

        try {
            Field f;
            if (mToolbar != null) {
                f = mToolbar.getClass().getDeclaredField("mTitleTextView");
                f.setAccessible(true);
                TextView titleTextView = (TextView) f.get(mToolbar);
                titleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                titleTextView.setFocusable(true);
                titleTextView.setFocusableInTouchMode(true);
                titleTextView.requestFocus();
                titleTextView.setSingleLine(true);
                titleTextView.setSelected(true);
                titleTextView.setMarqueeRepeatLimit(-1);
            }

        } catch (NoSuchFieldException e){
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        if (savedInstanceState == null) {
            if(editRecipePhotoFragment == null) {
                editRecipePhotoFragment = new EditRecipePhotoFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.edit_recipe_container, editRecipePhotoFragment, EditRecipePhotoFragment.class.getSimpleName())
                        .commit();
            }
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(recipe != null) {
            outState.putParcelable(Constants.KEY_RECIPE, recipe);
        }
        outState.putString(KEY_TITLE, title);
        outState.putString(Constants.KEY_DELETE_OLD_PICTURE, oldPicture);
        super.onSaveInstanceState(outState);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Tools tools = new Tools();
        tools.hideSoftKeyboard(this);
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
            case R.id.menu_edit_recipe:
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.edit_recipe_container);
                if (f instanceof EditRecipePhotoFragment) {
                    if (!(((EditRecipePhotoFragment) f).checkInfoOk())) {
                        return super.onOptionsItemSelected(item);
                    }
                    editRecipeIngredientsFragment = (EditRecipeIngredientsFragment) getSupportFragmentManager().findFragmentByTag(EditRecipeIngredientsFragment.class.getSimpleName());
                    if(editRecipeIngredientsFragment == null) {
                        editRecipeIngredientsFragment = new EditRecipeIngredientsFragment();
                    }
                    //editRecipePhotoFragment = null;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.edit_recipe_container, editRecipeIngredientsFragment, EditRecipeIngredientsFragment.class.getSimpleName())
                            .addToBackStack(null)
                            .commit();
                    getSupportFragmentManager().executePendingTransactions();
                } else if (f instanceof EditRecipeIngredientsFragment) {
                    //editRecipeStepsFragment = (EditRecipeStepsFragment) getSupportFragmentManager().findFragmentByTag(EditRecipeStepsFragment.class.getSimpleName());
                    editRecipeIngredientsFragment.saveData();
                    editRecipeIngredientsFragment = null;
                    if(editRecipeStepsFragment == null) {
                        editRecipeStepsFragment = new EditRecipeStepsFragment();
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.edit_recipe_container, editRecipeStepsFragment, EditRecipeStepsFragment.class.getSimpleName())
                            .addToBackStack(null)
                            .commit();
                    getSupportFragmentManager().executePendingTransactions();
                } else if (f instanceof EditRecipeStepsFragment) {
                    editRecipeStepsFragment.saveData();
                    setResultData();
                }
                Boolean compatRequired = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
                if(!compatRequired) {
                    invalidateOptionsMenu();// creates call to onPrepareOptionsMenu()
                }else {
                    supportInvalidateOptionsMenu();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem icon = menu.findItem(R.id.menu_edit_recipe);
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.edit_recipe_container);
        //Log.d(TAG, f.getClass().toString());
        if(f instanceof EditRecipeStepsFragment) {
            icon.setTitle(getResources().getString(R.string.menu_save_text));
        }else {
            icon.setTitle(getResources().getString(R.string.next));
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_recipe_menu, menu);
        Boolean ret = super.onCreateOptionsMenu(menu);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            onPrepareOptionsMenu(menu);
        return ret;
    }

    @Override
    public void onBackPressed() {
        checkBack();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private void finishWithoutSave(){
        if(!editRecipePhotoFragment.getNameOfNewImage().isEmpty()){
            ReadWriteTools rwTools = new ReadWriteTools();
            rwTools.deleteImageFromEditedPath(editRecipePhotoFragment.getNameOfNewImage());
        }
        Intent resultIntent = new Intent();
        setResult(RESULT_CANCELED, resultIntent);
        finish();
    }

    private void checkBack() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.exit_edit_title));
        builder.setMessage(getResources().getString(R.string.exit_edit)).setPositiveButton((getResources().getString(R.string.Yes)), dialogClickListener)
                .setNegativeButton((getResources().getString(R.string.No)), dialogClickListener);
        builder.show();
    }
    private void setResultData(){
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Constants.KEY_RECIPE, recipe);
        if(oldPicture != null && !oldPicture.isEmpty() && !oldPicture.equals(recipe.getPathPicture())){
            resultIntent.putExtra(Constants.KEY_DELETE_OLD_PICTURE, oldPicture);
        }
        setResult(Constants.RESULT_UPDATE_RECIPE, resultIntent);
        finish();
    }

    private final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    performPressBack();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };

    public RecipeItem getRecipe() {
        return recipe;
    }

    private void performPressBack(){
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.edit_recipe_container);
        if(f instanceof EditRecipePhotoFragment){
            finishWithoutSave();
        }else{
            Boolean compatRequired = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
            if(!compatRequired)
                invalidateOptionsMenu();
            else
                supportInvalidateOptionsMenu();
            if(f instanceof EditRecipeIngredientsFragment)
                editRecipeIngredientsFragment = null;
            else if(f instanceof EditRecipeStepsFragment)
                editRecipeStepsFragment = null;
            super.onBackPressed();
            f = getSupportFragmentManager().findFragmentById(R.id.edit_recipe_container);
            if(f instanceof EditRecipeIngredientsFragment)
                editRecipeIngredientsFragment = (EditRecipeIngredientsFragment) f;
            else if(f instanceof EditRecipeStepsFragment)
                editRecipeStepsFragment = (EditRecipeStepsFragment) f;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_CAMERA: {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.edit_recipe_container);
                Boolean cameraAllowed = (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED);
                if (f instanceof EditRecipePhotoFragment) {
                    ((EditRecipePhotoFragment) f).selectPhoto(cameraAllowed);
                }
            }
        }
    }
}

