package com.rukiasoft.androidapps.cocinaconroll;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeItem;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail2);
        RecipeItem recipeItem = new RecipeItem();
        Intent intent = getIntent();
        if(getIntent().hasExtra(RecipeListActivity.KEY_RECIPE))
            recipeItem = getIntent().getExtras().getParcelable(RecipeListActivity.KEY_RECIPE);
        String text = recipeItem.getName();

    }
}
