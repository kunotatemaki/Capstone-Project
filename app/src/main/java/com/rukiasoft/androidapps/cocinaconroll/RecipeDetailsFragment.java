package com.rukiasoft.androidapps.cocinaconroll;


import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import butterknife.Bind;
import butterknife.ButterKnife;


public class RecipeDetailsFragment extends Fragment implements
        AppBarLayout.OnOffsetChangedListener{
    private static final String TAG = LogHelper.makeLogTag(RecipeDetailsFragment.class);
    private static final float PERCENTAGE_TO_ELLIPSIZE_TITLE  = 0.1f;

    private static final String KEY_SAVE_RECIPE = Constants.PACKAGE_NAME + "." + RecipeDetailsFragment.class.getSimpleName();



    @Bind(R.id.recipe_pic) ImageView mPhotoView;
    @Bind(R.id.appbarlayout_recipe_details) AppBarLayout mAppBarLayout;
    @Bind(R.id.toolbar_recipe_details)Toolbar toolbarRecipeDetails;
    @Bind(R.id.recipe_name_recipe_details) TextView recipeName;
    @Bind(R.id.recipe_description_fab)
    FloatingActionButton recipeDescriptionFAB;
    @Bind(R.id.collapsing_toolbar_recipe_details)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.listview_ingredients_cardview)
    LinearLayout ingredientsList;
    @Bind(R.id.listview_steps_cardview)
    LinearLayout stepsList;
    private RecipeItem recipe;
    boolean recipeLoaded;
    private ActionBar actionBar;
    @Bind(R.id.cardview_link_textview) TextView author;
    private boolean own;

    public DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    Integer flags = Constants.FLAG_EDITED;
                    if((recipe.getState() & Constants.FLAG_EDITED_PICTURE) != 0)
                        flags = flags|Constants.FLAG_EDITED_PICTURE;
                    Tools tools = new Tools();
                    /*tools.deleteRecipe(getActivity().getApplicationContext(), recipe, flags);
                    ((MainActivity)getActivity()).loadRecipes(CocinaConRollConstants.ALL_RECIPES_FILTER);
*/
                    // TODO: 29/9/15 hacer lo de borrar la receta
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        getActivity().supportPostponeEnterTransition();
        setHasOptionsMenu(true);
        if(savedInstanceState != null && savedInstanceState.containsKey(KEY_SAVE_RECIPE)){
            recipe = savedInstanceState.getParcelable(KEY_SAVE_RECIPE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save recipe
        if (recipe != null) {
            savedInstanceState.putParcelable(KEY_SAVE_RECIPE, recipe);
        }
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.recipe_description_menu, menu);
        menu.findItem(R.id.menu_item_remove).setVisible(own);

        if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            onPrepareOptionsMenu(menu);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.delete_recipe_confirmation))
                .setPositiveButton((getResources().getString(R.string.Yes)), dialogClickListener)
                .setNegativeButton((getResources().getString(R.string.No)), dialogClickListener);
        Boolean compatRequired = android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
        switch (item.getItemId()) {
            case R.id.menu_item_edit_recipe:
                /*Intent intent = new Intent(getActivity(), EditRecipeActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("recipe", recipe);
                intent.putExtras(bundle);
                getActivity().startActivityForResult(intent, MainActivity.RESULT_EDIT_RECIPE);*/
                // TODO: 29/9/15 hacer lo de edit
                return true;
            case R.id.menu_item_remove:
                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    Runnable scaleIn = new Runnable() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void run() {
            recipeDescriptionFAB.animate().setDuration(250)
                    .setInterpolator(new AnticipateOvershootInterpolator())
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .withEndAction(scaleOut);
        }
    };

    Runnable scaleOut = new Runnable() {
        @Override
        public void run() {
            recipeDescriptionFAB.animate().setDuration(250)
                    .setInterpolator(new AnticipateOvershootInterpolator())
                    .scaleX(1.0f)
                    .scaleY(1.0f);
        }

    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_recipe_details, container, false);
        ButterKnife.bind(this, mRootView);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbarRecipeDetails);
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        if(mAppBarLayout != null){
            mAppBarLayout.addOnOffsetChangedListener(this);
        }


        if(recipeDescriptionFAB != null) {
            recipeDescriptionFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO - hacer aquí lo del latido
                    new Handler().postDelayed(new Runnable() {
                        @Override public void run() {
                            Tools tools = new Tools();
                            if(!own) {
                                if (recipe.getFavourite()) {
                                    recipeDescriptionFAB.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite_outline_white_24dp));
                                } else {
                                    recipeDescriptionFAB.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite_white_24dp));
                                }
                                recipe.setFavourite(!recipe.getFavourite());


                                // TODO: 29/9/15 grabar recete y actualizar tools.saveRecipeOnEditedPath(getActivity(), recipe);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    scaleIn.run();
                                }
                            }else{
                                // TODO: 29/9/15  tools.sendEmail(getActivity(), recipe); comprobar con resolveActivity

                            }
                        }
                    }, 150);
                }
            });
        }

        if(recipe != null){
            loadRecipe();
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mAppBarLayout != null) {
            mAppBarLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    Animator animator = ViewAnimationUtils.createCircularReveal(
                            mAppBarLayout,
                            mAppBarLayout.getWidth()/2,
                            mAppBarLayout.getHeight()/2,
                            0,
                            (float) Math.hypot(mAppBarLayout.getWidth(), mAppBarLayout.getHeight())/2);
                    // Set a natural ease-in/ease-out interpolator.
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());

                    // make the view visible and start the animation
                    animator.start();
                }
            });
        }


        return mRootView;
    }


    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {

        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleTitleBehavior(percentage);
        //handleToolbarTitleVisibility(percentage);

    }

    private void handleTitleBehavior(float percentage) {
        if (percentage >= PERCENTAGE_TO_ELLIPSIZE_TITLE) {
            recipeName.setVisibility(View.GONE);
        }else{
            recipeName.setVisibility(View.VISIBLE);
            recipeName.setAlpha(1-percentage/PERCENTAGE_TO_ELLIPSIZE_TITLE);
        }
    }

    public void setRecipe(RecipeItem recipe) {
        this.recipe = recipe;
        loadRecipe();
    }

    private void loadRecipe(){
        if(recipeLoaded) return;
        own = (recipe.getState() & (Constants.FLAG_OWN|Constants.FLAG_EDITED)) != 0;

        if(recipeName != null){
            recipeName.setText(recipe.getName());
        }
        if(actionBar != null){
            actionBar.setTitle(recipe.getName());
        }
        if(recipeDescriptionFAB != null){
            if(own){
                recipeDescriptionFAB.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_share_white_24dp));
            }else {
                if (recipe.getFavourite()) {
                    recipeDescriptionFAB.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite_white_24dp));
                } else {
                    recipeDescriptionFAB.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite_outline_white_24dp));
                }
            }
        }
        if(mPhotoView != null){
            Glide.with(this)
                    .load(Uri.parse(recipe.getPath()))
                    .asBitmap()
                    .error(R.drawable.default_dish)
                    .into(new BitmapImageViewTarget(mPhotoView) {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                            super.onResourceReady(bitmap, anim);
                            applyPalette(bitmap);
                        }
                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable){
                            super.onLoadFailed(e, errorDrawable);
                            Bitmap bitmap = ((BitmapDrawable)errorDrawable).getBitmap();
                            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_dish);
                            applyPalette(bitmap);
                        }
                    });

        }

        //Set the author
        String sAuthor = getResources().getString(R.string.default_author);
        if(recipe.getAuthor().equals(sAuthor))
            author.setText(sAuthor);
        else {
            String link = getResources().getString(R.string.original_link).concat(" ").concat(recipe.getAuthor());
            author.setText(Html.fromHtml(link));
            author.setMovementMethod(LinkMovementMethod.getInstance());
        }

        //set ingredients and steps
        ingredientsList.removeAllViews();
        for(String ingredient : recipe.getIngredients()){
            LayoutInflater inflater;
            inflater = (LayoutInflater) getActivity()
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View ingredientItem = inflater.inflate(R.layout.recipe_description_item, null);
            TextView textView = (TextView) ingredientItem.findViewById(R.id.recipe_description_item_description);
            textView.setText(ingredient);
            ImageView icon = (ImageView) ingredientItem.findViewById(R.id.recipe_description_item_icon);
            icon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_bone));
            ingredientsList.addView(ingredientItem);
        }
        stepsList.removeAllViews();
        for(String step : recipe.getSteps()){
            LayoutInflater inflater;
            inflater = (LayoutInflater) getActivity()
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View stepItem = inflater.inflate(R.layout.recipe_description_item, null);
            TextView textView = (TextView) stepItem.findViewById(R.id.recipe_description_item_description);
            textView.setText(step);
            ImageView icon = (ImageView) stepItem.findViewById(R.id.recipe_description_item_icon);
            icon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_dog_foot));
            stepsList.addView(stepItem);
        }

        recipeLoaded = true;
    }

    private void applyPalette(Bitmap bitmap){
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int mVibrantColor = palette.getVibrantColor(ContextCompat.getColor(getActivity(), R.color.ColorPrimary));
                int mVibrantDarkColor = palette.getDarkVibrantColor(mVibrantColor);
                int mMutedColor = palette.getMutedColor(ContextCompat.getColor(getActivity(), R.color.ColorAccent));
                int mMutedDarkColor = palette.getDarkMutedColor(mMutedColor);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getActivity().getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(mMutedDarkColor);
                }
                collapsingToolbarLayout.setContentScrim(new ColorDrawable(mMutedColor));
                recipeDescriptionFAB.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{0}}, new int[]{mVibrantColor}));
                getActivity().supportStartPostponedEnterTransition();

            }
        });

    }



}
