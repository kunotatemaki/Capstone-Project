package com.rukiasoft.androidapps.cocinaconroll;


import android.animation.Animator;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.rukiasoft.androidapps.cocinaconroll.classes.ObservableScrollView;
import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;

import butterknife.Bind;
import butterknife.ButterKnife;


public class RecipeDetailsFragment extends Fragment implements
        AppBarLayout.OnOffsetChangedListener{
    private static final String TAG = LogHelper.makeLogTag(RecipeDetailsFragment.class);
    private static final float PERCENTAGE_TO_ELLIPSIZE_TITLE  = 0.1f;

    private static final String KEY_SAVE_RECIPE = Constants.PACKAGE_NAME + "." + RecipeDetailsFragment.class.getSimpleName();

    //private View mRootView;
    private int mVibrantColor;
    private int mVibrantDarkColor;
    @Bind(R.id.scrollview_recipe_details) ObservableScrollView mScrollView;

    @Bind(R.id.recipe_pic) ImageView mPhotoView;
    @Bind(R.id.recipe_pic_protection) ImageView mPhotoViewProtection;
    @Bind(R.id.appbarlayout_recipe_details) AppBarLayout mAppBarLayout;
    @Bind(R.id.toolbar_recipe_details)Toolbar toolbarRecipeDetails;
    @Bind(R.id.recipe_name_recipe_details) TextView recipeName;
    @Bind(R.id.favorite_fab)
    FloatingActionButton favoriteActionButton;
    @Bind(R.id.collapsing_toolbar_recipe_details)
    CollapsingToolbarLayout collapsingToolbarLayout;
    private RecipeItem recipe;
    boolean recipeLoaded;
    private ActionBar actionBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        getActivity().supportPostponeEnterTransition();
        addTransitionListener();
        //setHasOptionsMenu(true);
        if(savedInstanceState != null && savedInstanceState.containsKey(KEY_SAVE_RECIPE)){
            recipe = (RecipeItem)savedInstanceState.getParcelable(KEY_SAVE_RECIPE);
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

    /*public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }*/

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

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


        if(favoriteActionButton != null) {
            favoriteActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO - hacer aquí lo del latido
                    /*startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                            .setType("text/plain")
                            .setText("Some sample text")
                            .getIntent(), getString(R.string.action_share)));*/
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
        if(recipeLoaded == true) return;
        if(recipeName != null){
            recipeName.setText(recipe.getName());
        }
        if(actionBar != null){
            actionBar.setTitle(recipe.getName());
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
                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_dish);
                            applyPalette(bitmap);
                        }
                    });

        }

        recipeLoaded = true;
    }

    private void applyPalette(Bitmap bitmap){
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                mVibrantColor = palette.getVibrantColor(ContextCompat.getColor(getActivity(), R.color.ColorPrimary));
                mVibrantDarkColor = palette.getDarkVibrantColor(ContextCompat.getColor(getActivity(), R.color.ColorPrimaryDark));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getActivity().getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(mVibrantDarkColor);
                }
                collapsingToolbarLayout.setContentScrim(new ColorDrawable(mVibrantColor));
                getActivity().supportStartPostponedEnterTransition();

            }
        });

    }

    /**
     * Try and add a {@link Transition.TransitionListener} to the entering shared element
     * {@link Transition}. We do this so that we can load the full-size image after the transition
     * has completed.
     *
     * @return true if we were successful in adding a listener to the enter transition
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean addTransitionListener() {

        final Transition transition = getActivity().getWindow().getSharedElementEnterTransition();
        //final Transition transition = getSharedElementEnterTransition();

        if (transition != null) {
            // There is an entering shared element transition so add a listener to it
            transition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    // As the transition has ended, we can now load the full-size image
                    //loadFullSizeImage();
                    Animator animator = ViewAnimationUtils.createCircularReveal(
                            mAppBarLayout,
                            0,
                            0,
                            0,
                            (float) Math.hypot(mAppBarLayout.getWidth(), mAppBarLayout.getHeight()));
                    // Set a natural ease-in/ease-out interpolator.
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());

                    // make the view visible and start the animation
                    animator.start();
                    // Make sure we remove ourselves as a listener
                    transition.removeListener(this);
                }

                @Override
                public void onTransitionStart(Transition transition) {
                    LogHelper.d(TAG, "onTransitionStart");

                }

                @Override
                public void onTransitionCancel(Transition transition) {
                    // Make sure we remove ourselves as a listener
                    LogHelper.d(TAG, "onTransitionStart");
                    transition.removeListener(this);
                }

                @Override
                public void onTransitionPause(Transition transition) {
                    LogHelper.d(TAG, "onTransitionStart");
                }

                @Override
                public void onTransitionResume(Transition transition) {
                    LogHelper.d(TAG, "onTransitionStart");
                }
            });
            return true;
        }

        // If we reach here then we have not added a listener
        return false;
    }

}
