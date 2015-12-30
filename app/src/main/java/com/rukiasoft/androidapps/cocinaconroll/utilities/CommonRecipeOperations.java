package com.rukiasoft.androidapps.cocinaconroll.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.rukiasoft.androidapps.cocinaconroll.R;
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
    private Context context;

    public CommonRecipeOperations(Activity activity, RecipeItem recipeItem){
        this.activity = activity;
        this.recipe = recipeItem;
    }
    public CommonRecipeOperations(Context context, RecipeItem recipeItem){
        if(context instanceof Activity) {
            this.activity = (Activity)context;
        }
        this.recipe = recipeItem;
        this.context = context;
    }

    public void editRecipe(){
        if(activity == null)    return;
        Intent intent = new Intent(activity, EditRecipeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.KEY_RECIPE, recipe);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, Constants.REQUEST_EDIT_RECIPE);
    }

    public boolean removeRecipe(){
        if(activity == null)    return false;
        AlertDialog.Builder removeBuilder = new AlertDialog.Builder(activity);
        String message;
        if((recipe.getState() & (Constants.FLAG_EDITED|Constants.FLAG_EDITED_PICTURE))!=0){
            message = activity.getResources().getString(R.string.restore_recipe_confirmation);
        }else if((recipe.getState() & Constants.FLAG_OWN)!=0){
            message = activity.getResources().getString(R.string.delete_recipe_confirmation);
        }else{
            return false;
        }

        removeBuilder.setMessage(message)
                .setPositiveButton((activity.getResources().getString(R.string.Yes)), removeDialogClickListener)
                .setNegativeButton((activity.getResources().getString(R.string.No)), removeDialogClickListener);
        removeBuilder.show();
        return true;
    }

    private final DialogInterface.OnClickListener removeDialogClickListener = new DialogInterface.OnClickListener() {


        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(Constants.KEY_RECIPE, recipe);
                    activity.setResult(Constants.RESULT_DELETE_RECIPE, resultIntent);
                    activity.finish();

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };

    public void updateRecipe(String deleteOldPicture){
        if(activity == null)    return;
        if(recipe.getPicture().equals(Constants.DEFAULT_PICTURE_NAME))
            recipe.setPathPicture(Constants.DEFAULT_PICTURE_NAME);
        ReadWriteTools rwTools = new ReadWriteTools(activity);
        String path = rwTools.saveRecipeOnEditedPath(recipe);
        recipe.setPathRecipe(path);
        recipe.setVersion(recipe.getVersion() + 1);
        if(activity instanceof SigningDriveActivity) {
            ((SigningDriveActivity)activity).uploadRecipeToDrive(recipe);
        }
        //update database
        DatabaseRelatedTools dbTools = new DatabaseRelatedTools(activity);
        dbTools.updatePathsAndVersion(recipe);
        if(!deleteOldPicture.isEmpty()) {
            rwTools.deleteImage(deleteOldPicture);
        }
    }

    public RecipeItem loadRecipeDetailsFromRecipeCard(){
        ReadWriteTools rwTools = new ReadWriteTools(context);
        if(recipe.getIngredients() == null || recipe.getIngredients().size() == 0){
            RecipeItem item = rwTools.readRecipeInfo(recipe.getPathRecipe());
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
