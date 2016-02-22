package com.rukiasoft.androidapps.cocinaconroll.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.rukiasoft.androidapps.cocinaconroll.classes.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.database.DatabaseRelatedTools;
import com.rukiasoft.androidapps.cocinaconroll.ui.EditRecipeActivity;
import com.rukiasoft.androidapps.cocinaconroll.ui.SigningDriveActivity;

/**
 * Created by iRuler on 30/12/15.
 */
public class CommonRecipeOperations {

    private RecipeItem recipe;
    private Activity activity;
    //private Context context;

    public CommonRecipeOperations(Activity activity, RecipeItem recipeItem){
        this.activity = activity;
        this.recipe = recipeItem;
    }
    public CommonRecipeOperations(Context context, RecipeItem recipeItem){
        if(context instanceof Activity) {
            this.activity = (Activity)context;
        }
        this.recipe = recipeItem;
    }

    public void editRecipe(){
        if(activity == null)    return;
        Intent intent = new Intent(activity, EditRecipeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.KEY_RECIPE, recipe);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, Constants.REQUEST_EDIT_RECIPE);
    }


    public void updateRecipe(String deleteOldPicture){
        if(activity == null)    return;
        if(recipe.getPicture().equals(Constants.DEFAULT_PICTURE_NAME))
            recipe.setPathPicture(Constants.DEFAULT_PICTURE_NAME);
        ReadWriteTools rwTools = new ReadWriteTools();
        String path = rwTools.saveRecipeOnEditedPath(activity.getApplicationContext(), recipe);
        recipe.setPathRecipe(path);
        recipe.setVersion(recipe.getVersion() + 1);
        if(activity instanceof SigningDriveActivity) {
            ((SigningDriveActivity)activity).uploadRecipeToDrive(recipe);
        }
        //update database
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools();
        dbTools.updatePathsAndVersion(activity.getApplicationContext(), recipe);
        if(!deleteOldPicture.isEmpty()) {
            rwTools.deleteImage(deleteOldPicture);
        }
    }

    public RecipeItem loadRecipeDetailsFromRecipeCard(){
        ReadWriteTools rwTools = new ReadWriteTools();
        if(recipe.getIngredients() == null || recipe.getIngredients().size() == 0){
            RecipeItem item = rwTools.readRecipeInfo(activity.getApplicationContext(), recipe.getPathRecipe());
            if(item == null)
                return null;
            recipe.setMinutes(item.getMinutes());
            recipe.setPortions(item.getPortions());
            recipe.setAuthor(item.getAuthor());
            recipe.setIngredients(item.getIngredients());
            recipe.setSteps(item.getSteps());
            recipe.setTip(item.getTip());
        }
        return recipe;
    }

}
