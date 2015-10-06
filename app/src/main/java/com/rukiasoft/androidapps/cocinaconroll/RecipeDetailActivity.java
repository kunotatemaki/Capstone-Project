package com.rukiasoft.androidapps.cocinaconroll;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.utilities.ReadWriteTools;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RecipeDetailActivity extends AppCompatActivity {

    public static final int KEY_RESULT_UPDATE_RECIPE = 999;
    public static final int REQUEST_EDIT = 998;

    @Bind(R.id.adview_details)
    AdView mAdViewDetails;
    RecipeDetailsFragment recipeDetailsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        ButterKnife.bind(this);
        RecipeItem recipeItem = new RecipeItem();
        Intent intent = getIntent();
        if(intent != null && intent.hasExtra(RecipeListActivity.KEY_RECIPE))
            recipeItem = getIntent().getExtras().getParcelable(RecipeListActivity.KEY_RECIPE);
        else{
            finish();
        }
        RecipeDetailsFragment recipeDetailsFragment = (RecipeDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.details_recipes_fragment);
        if(recipeDetailsFragment != null){
            recipeDetailsFragment.setRecipe(recipeItem);
        }else{
            finish();
        }
        //set up advertises
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("B29C1F71528C79C864D503360C5225C0")  // My Xperia Z3 test device
                .setGender(AdRequest.GENDER_FEMALE)
                .build();

        mAdViewDetails.loadAd(adRequest);


    }

    @Override
    public void onResume() {

        super.onResume();
        Tools tools = new Tools();
        tools.setScreenOnIfSettingsAllowed(this, true);
    }

    @Override
    public void onPause() {
        super.onPause();
        Tools tools = new Tools();
        tools.setScreenOnIfSettingsAllowed(this, false);
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intentData) {
        if(requestCode == REQUEST_EDIT){
            if(resultCode == RecipeListActivity.RESULT_UPDATE_RECIPE && intentData != null && intentData.hasExtra(RecipeListActivity.KEY_RECIPE)){
                RecipeItem recipe = intentData.getParcelableExtra(RecipeListActivity.KEY_RECIPE);
                recipeDetailsFragment = (RecipeDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.details_recipes_fragment);

                if(recipeDetailsFragment != null)
                    recipeDetailsFragment.updateRecipe(recipe);
                //grabo la receta
                if(recipe.getPicture().equals(Constants.DEFAULT_PICTURE_NAME))
                    recipe.setPath(Constants.DEFAULT_PICTURE_NAME);
                ReadWriteTools readWriteTools = new ReadWriteTools(this);
                readWriteTools.saveRecipeOnEditedPath(recipe);
                Intent returnIntent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putParcelable(RecipeListActivity.KEY_RECIPE, recipe);
                returnIntent.putExtras(bundle);

                setResult(RecipeListActivity.RESULT_UPDATE_RECIPE, returnIntent);
            }
        }
    }
}
