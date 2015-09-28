package com.rukiasoft.androidapps.cocinaconroll;


import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
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
    //private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;

    @Bind(R.id.recipe_pic) ImageView mPhotoView;
    @Bind(R.id.recipe_pic_protection) ImageView mPhotoViewProtection;
    private int mScrollY;
    private int mStatusBarFullOpacityBottom;
    @Bind(R.id.appbarlayout_recipe_details) AppBarLayout mAppBarLayout;
    @Bind(R.id.toolbar_recipe_details)Toolbar toolbarRecipeDetails;
    @Bind(R.id.recipe_name_recipe_details) TextView recipeName;
    @Bind(R.id.share_fab)
    FloatingActionButton floatingActionButton;
    @Bind(R.id.collapsing_toolbar_recipe_details)
    CollapsingToolbarLayout collapsingToolbarLayout;
    private RecipeItem recipe;
    boolean recipeLoaded;
    private ActionBar actionBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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

        mScrollView = (ObservableScrollView) mRootView.findViewById(R.id.scrollview_recipe_details);
        mScrollView.setCallbacks(new ObservableScrollView.Callbacks() {
            @Override
            public void onScrollChanged() {
                mScrollY = mScrollView.getScrollY();
                //getActivityCast().onUpButtonFloorChanged(mItemId, ArticleDetailFragment.this);
                //mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
                updateStatusBar();
            }
        });

        mStatusBarColorDrawable = new ColorDrawable(0);

        if(floatingActionButton != null) {
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO - hacer aquí lo del latiod
                    /*startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                            .setType("text/plain")
                            .setText("Some sample text")
                            .getIntent(), getString(R.string.action_share)));*/
                }
            });
        }

        updateStatusBar();
        if(recipe != null){
            loadRecipe();
        }
        return mRootView;
    }

    private void updateStatusBar() {
        int color = 0;
        /*if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
            float f = progress(mScrollY,
                    mStatusBarFullOpacityBottom - mTopInset * 3,
                    mStatusBarFullOpacityBottom - mTopInset);
            color = Color.argb((int) (255 * f),
                    (int) (Color.red(mMutedColor) * 0.9),
                    (int) (Color.green(mMutedColor) * 0.9),
                    (int) (Color.blue(mMutedColor) * 0.9));
        }*/
        mStatusBarColorDrawable.setColor(color);
        //mDrawInsetsFrameLayout.setInsetBackground(mStatusBarColorDrawable);
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


    /*private void handleAlphaOnTitle(float percentage) {

        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {

            if(mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }*/

    public static void startAlphaAnimation (View v, long duration, int visibility) {

        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
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
                    .into(new BitmapImageViewTarget(mPhotoView) {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                            super.onResourceReady(bitmap, anim);
                            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    mVibrantColor = palette.getMutedColor(ContextCompat.getColor(getActivity(), R.color.ColorPrimary));
                                    mVibrantDarkColor = palette.getDarkMutedColor(ContextCompat.getColor(getActivity(), R.color.ColorPrimaryDark));
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        Window window = getActivity().getWindow();
                                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                                        window.setStatusBarColor(mVibrantDarkColor);
                                    }
                                    collapsingToolbarLayout.setContentScrim(new ColorDrawable(mVibrantColor));
                                }
                            });
                        }
                    });

        }

        recipeLoaded = true;
    }


}
