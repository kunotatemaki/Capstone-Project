package com.rukiasoft.androidapps.cocinaconroll.ui;


import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.rukiasoft.androidapps.cocinaconroll.CocinaConRollApplication;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.classes.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.database.DatabaseRelatedTools;
import com.rukiasoft.androidapps.cocinaconroll.utilities.CommonRecipeOperations;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.ReadWriteTools;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;
import com.squareup.leakcanary.RefWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class RecipeDetailsFragment extends Fragment implements
        AppBarLayout.OnOffsetChangedListener{
    //private static final String TAG = LogHelper.makeLogTag(RecipeDetailsFragment.class);
    private static final float PERCENTAGE_TO_ELLIPSIZE_TITLE  = 0.1f;

    private static final String KEY_SAVE_RECIPE = Constants.PACKAGE_NAME + "." + RecipeDetailsFragment.class.getSimpleName() + ".saverecipe";
    private static final String KEY_ANIMATED = Constants.PACKAGE_NAME + "." + RecipeDetailsFragment.class.getSimpleName() + ".animate";


    @BindView(R.id.recipe_details_icon_minutes) ImageView iconMinutes;
    @BindView(R.id.recipe_details_icon_portions) ImageView iconPortions;
    @BindView(R.id.recipe_details_text_minutes) TextView textMinutes;
    @BindView(R.id.recipe_details_text_portions) TextView textPortions;
    @BindView(R.id.tip_body_cardview) TextView tip;
    @BindView(R.id.card_tip)
    CardView cardTip;
    @BindView(R.id.recipe_pic) ImageView mPhotoView;
    @Nullable@BindView(R.id.appbarlayout_recipe_details) AppBarLayout mAppBarLayout;
    @Nullable@BindView(R.id.photo_container_recipe_details)
    RelativeLayout photoContainer;
    @BindView(R.id.toolbar_recipe_details)Toolbar toolbarRecipeDetails;
    @BindView(R.id.recipe_name_recipe_details) TextView recipeName;
    @BindView(R.id.recipe_description_fab)
    FloatingActionButton recipeDescriptionFAB;
    @Nullable@BindView(R.id.collapsing_toolbar_recipe_details)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.listview_ingredients_cardview)
    LinearLayout ingredientsList;
    @BindView(R.id.listview_steps_cardview)
    LinearLayout stepsList;
    private Unbinder unbinder;
    private RecipeItem recipe;
    private boolean recipeLoaded = false;
    private ActionBar actionBar;
    @BindView(R.id.cardview_link_textview) TextView author;
    private boolean own;
    private boolean land;
    private boolean animated;
    private View viewToReveal;
    private DatabaseRelatedTools dbTools;
    private ReadWriteTools rwTools;


    private final DialogInterface.OnClickListener editDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    rwTools.share(getActivity(), recipe);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };
    private boolean collapsed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().supportPostponeEnterTransition();
        }
        setHasOptionsMenu(true);
        Tools mTools = new Tools();
        dbTools = new DatabaseRelatedTools();
        rwTools = new ReadWriteTools();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save recipe
        recipeLoaded = false;
        if (recipe != null) {
            savedInstanceState.putParcelable(KEY_SAVE_RECIPE, recipe);
        }
        savedInstanceState.putBoolean(KEY_ANIMATED, animated);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.recipe_description_menu, menu);
        menu.findItem(R.id.menu_item_remove).setVisible(own);
        menu.findItem(R.id.menu_item_share_recipe).setVisible(own);

        if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            onPrepareOptionsMenu(menu);
    }

    private final DialogInterface.OnClickListener removeDialogClickListener = new DialogInterface.OnClickListener() {


        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(Constants.KEY_RECIPE, recipe);
                    getActivity().setResult(Constants.RESULT_DELETE_RECIPE, resultIntent);
                    getActivity().finish();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };

    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_edit_recipe:
                editRecipe();
                return true;
            case R.id.menu_item_remove:
                AlertDialog.Builder removeBuilder = new AlertDialog.Builder(getActivity());
                String message;
                if((recipe.getState() & (Constants.FLAG_EDITED|Constants.FLAG_EDITED_PICTURE))!=0){
                    message = getActivity().getResources().getString(R.string.restore_recipe_confirmation);
                }else if((recipe.getState() & Constants.FLAG_OWN)!=0){
                    message = getActivity().getResources().getString(R.string.delete_recipe_confirmation);
                }else{
                    return false;
                }

                removeBuilder.setMessage(message)
                        .setPositiveButton((getActivity().getResources().getString(R.string.Yes)), removeDialogClickListener)
                        .setNegativeButton((getActivity().getResources().getString(R.string.No)), removeDialogClickListener);
                removeBuilder.show();
                return true;
            case R.id.menu_item_share_recipe:
                AlertDialog.Builder shareBuilder = new AlertDialog.Builder(getActivity());
                message = getResources().getString(R.string.share_confirmation);
                shareBuilder.setMessage(message)
                        .setPositiveButton((getResources().getString(R.string.Yes)), editDialogClickListener)
                        .setNegativeButton((getResources().getString(R.string.No)), editDialogClickListener);
                shareBuilder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void editRecipe(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                android.support.v7.app.AlertDialog.Builder builder =
                        new android.support.v7.app.AlertDialog.Builder(getActivity());

                builder.setMessage(getResources().getString(R.string.write_external_explanation))
                        .setTitle(getResources().getString(R.string.permissions_title))
                        .setPositiveButton(getResources().getString(R.string.accept),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        ActivityCompat.requestPermissions(getActivity(),
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                                    }
                                });
                builder.create().show();
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }else{
            CommonRecipeOperations commonRecipeOperations = new CommonRecipeOperations(getActivity(), recipe);
            commonRecipeOperations.editRecipe();
        }
    }

    private final Runnable scaleIn = new Runnable() {
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

    private final Runnable scaleOut = new Runnable() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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
        unbinder = ButterKnife.bind(this, mRootView);
        land = getResources().getBoolean(R.bool.land);



        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbarRecipeDetails);
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(!land);
        }

        if(mAppBarLayout != null){
            mAppBarLayout.addOnOffsetChangedListener(this);
        }

        if(recipeDescriptionFAB != null) {

            recipeDescriptionFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                clickOnHeartButton();
                            }
                        }, 150);
                    }else{
                        clickOnHeartButton();
                    }
                }
            });
        }

        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(KEY_SAVE_RECIPE)) {
                recipe = savedInstanceState.getParcelable(KEY_SAVE_RECIPE);
            }
            animated = false;
            if(savedInstanceState.containsKey(KEY_ANIMATED)) {
                animated = savedInstanceState.getBoolean(KEY_ANIMATED);
            }
        }

        if(recipe != null){
            loadRecipe();
        }
        if(animated){
            return mRootView;
        }
        //create de reveal effect either for landscape and portrait
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!land && mAppBarLayout != null) {
                viewToReveal = mAppBarLayout;
            } else if (land && photoContainer != null) {
                viewToReveal = photoContainer;
                collapsed = false;
            }
            viewToReveal.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    Animator animator = ViewAnimationUtils.createCircularReveal(
                            viewToReveal,
                            viewToReveal.getWidth() / 2,
                            viewToReveal.getHeight() / 2,
                            0,
                            (float) Math.hypot(viewToReveal.getWidth(), viewToReveal.getHeight()) / 2);
                    // Set a natural ease-in/ease-out interpolator.
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());

                    // make the view visible and start the animation
                    if (!collapsed) {
                        animator.start();
                        animated = true;
                    }
                }
            });

        }
        return mRootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = CocinaConRollApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

    private void clickOnHeartButton(){
        if (recipe.getFavourite()) {
            recipeDescriptionFAB.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                    R.drawable.ic_favorite_outline_white_24dp));
        } else {
            recipeDescriptionFAB.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                    R.drawable.ic_favorite_white_24dp));
        }
        //lo almaceno en la base de datos
        recipe.setFavourite(!recipe.getFavourite());
        dbTools.updateFavoriteById(getActivity().getApplicationContext(), recipe.get_id(), recipe.getFavourite());
        Intent returnIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.KEY_RECIPE, recipe);
        returnIntent.putExtras(bundle);
        getActivity().setResult(Constants.RESULT_UPDATE_RECIPE, returnIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            scaleIn.run();
        }
    }


    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {

        if(land){
            collapsed = false;
            return;
        }
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;
        collapsed = percentage == 1;
        handleTitleBehavior(percentage);
        //handleToolbarTitleVisibility(percentage);

    }

    private void handleTitleBehavior(float percentage) {
        if (percentage >= PERCENTAGE_TO_ELLIPSIZE_TITLE) {
            recipeName.setVisibility(View.GONE);
        }else{
            recipeName.setVisibility(View.VISIBLE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                recipeName.setAlpha(1 - percentage / PERCENTAGE_TO_ELLIPSIZE_TITLE);
            }
        }
    }

    public void setRecipe(RecipeItem recipe) {
        this.recipe = recipe;
        loadRecipe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void loadRecipe(){
        if(recipeLoaded) return;
        own = (recipe.getState() & (Constants.FLAG_OWN|Constants.FLAG_EDITED)) != 0;

        if(recipeName != null){
            recipeName.setText(recipe.getName());
        }
        if(recipe.getMinutes()>0){
            textMinutes.setText(String.valueOf(recipe.getMinutes()));
            textMinutes.setVisibility(View.VISIBLE);
            iconMinutes.setVisibility(View.VISIBLE);
        }else{
            textMinutes.setVisibility(View.GONE);
            iconMinutes.setVisibility(View.GONE);
        }
        if(recipe.getPortions()>0){
            textPortions.setText(String.valueOf(recipe.getPortions()));
            textPortions.setVisibility(View.VISIBLE);
            iconPortions.setVisibility(View.VISIBLE);
        }else{
            textPortions.setVisibility(View.GONE);
            iconPortions.setVisibility(View.GONE);
        }
        if(actionBar != null){
            actionBar.setTitle(recipe.getName());
        }
        if(recipeDescriptionFAB != null){
            if (recipe.getFavourite()) {
                recipeDescriptionFAB.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite_white_24dp));
            } else {
                recipeDescriptionFAB.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite_outline_white_24dp));
            }
        }
        if(mPhotoView != null){
            BitmapImageViewTarget bitmapImageViewTarget = new BitmapImageViewTarget(mPhotoView) {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                    super.onResourceReady(bitmap, anim);
                    applyPalette(bitmap);
                }

                @Override
                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                    super.onLoadFailed(e, errorDrawable);
                    Bitmap bitmap = ((BitmapDrawable) errorDrawable).getBitmap();
                    //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_dish);
                    applyPalette(bitmap);
                }
            };
            rwTools.loadImageFromPath(getActivity().getApplicationContext(),
                    bitmapImageViewTarget, recipe.getPathPicture(),
                    R.drawable.default_dish, recipe.getVersion());
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

        //set tip
        if (recipe.getTip() != null && !recipe.getTip().isEmpty()) {
            cardTip.setVisibility(View.VISIBLE);
            tip.setText(recipe.getTip());
        }else{
            cardTip.setVisibility(View.GONE);
        }

        recipeLoaded = true;
    }

    private void applyPalette(Bitmap bitmap){
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                if(palette == null)
                    return;
                try {
                    int mVibrantColor = palette.getVibrantColor(ContextCompat.getColor(getActivity(), R.color.ColorPrimary));
                    //int mVibrantDarkColor = palette.getDarkVibrantColor(mVibrantColor);
                    int mMutedColor = palette.getMutedColor(ContextCompat.getColor(getActivity(), R.color.ColorAccent));
                    int mMutedDarkColor = palette.getDarkMutedColor(mMutedColor);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getActivity().getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(mMutedDarkColor);
                    }
                    if (collapsingToolbarLayout != null) {
                        collapsingToolbarLayout.setContentScrim(new ColorDrawable(mMutedColor));
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        recipeDescriptionFAB.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{0}}, new int[]{mVibrantColor}));
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if(getActivity() != null) {
                        getActivity().supportStartPostponedEnterTransition();
                    }
                }

            }
        });

    }


    @SuppressLint("NewApi")
    public void updateRecipe(RecipeItem recipe) {
        this.recipe = recipe;
        loadRecipe();
        Boolean compatRequired = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
        if(!compatRequired)
            getActivity().invalidateOptionsMenu();// creates call to onPrepareOptionsMenu()
        else
            getActivity().supportInvalidateOptionsMenu();
    }
}
