package com.rukiasoft.androidapps.cocinaconroll.classes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.database.DatabaseRelatedTools;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LikeButtonView extends FrameLayout {
    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

    @BindView(R.id.ivStar)
    ImageView ivStar;
    @BindView(R.id.vDotsView)
    DotsView vDotsView;
    @BindView(R.id.vCircle)
    CircleView vCircle;


    Context mContext;
    private RecipeItem recipeItem;
    private AnimatorSet animatorSet;
    private ImageView favoriteIcon;

    public LikeButtonView(Context context) {
        super(context);
        mContext = context;
    }

    public LikeButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public LikeButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LikeButtonView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    public void init(final RecipeItem recipe, ImageView favorite) {
        LayoutInflater.from(getContext()).inflate(R.layout.view_like_button, this, true);
        ButterKnife.bind(this);
        recipeItem = recipe;
        favoriteIcon = favorite;
        ivStar.setImageResource(recipeItem.getFavourite() ? R.drawable.ic_favorite_white_36dp : R.drawable.ic_favorite_outline_white_36dp);
        ivStar.setOnClickListener(new OnClickListener() {
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            @Override
            public void onClick(View v) {
                recipeItem.setFavourite(!recipeItem.getFavourite());
                DatabaseRelatedTools dbTools = new DatabaseRelatedTools();
                dbTools.updateFavoriteById(mContext, recipeItem.get_id(), recipeItem.getFavourite());
                favoriteIcon.setVisibility(recipeItem.getFavourite()? VISIBLE : GONE);
                //updateRecipe(recipeItem);
                //isChecked = !isChecked;
                ivStar.setImageResource(recipeItem.getFavourite() ? R.drawable.ic_favorite_white_36dp : R.drawable.ic_favorite_outline_white_36dp);

                if (animatorSet != null) {
                    animatorSet.cancel();
                }

                if (recipeItem.getFavourite()) {
                    ivStar.animate().cancel();
                    ivStar.setScaleX(0);
                    ivStar.setScaleY(0);
                    vCircle.setInnerCircleRadiusProgress(0);
                    vCircle.setOuterCircleRadiusProgress(0);
                    vDotsView.setCurrentProgress(0);

                    animatorSet = new AnimatorSet();

                    ObjectAnimator outerCircleAnimator = ObjectAnimator.ofFloat(vCircle, CircleView.OUTER_CIRCLE_RADIUS_PROGRESS, 0.1f, 1f);
                    outerCircleAnimator.setDuration(250);
                    outerCircleAnimator.setInterpolator(DECCELERATE_INTERPOLATOR);

                    ObjectAnimator innerCircleAnimator = ObjectAnimator.ofFloat(vCircle, CircleView.INNER_CIRCLE_RADIUS_PROGRESS, 0.1f, 1f);
                    innerCircleAnimator.setDuration(200);
                    innerCircleAnimator.setStartDelay(200);
                    innerCircleAnimator.setInterpolator(DECCELERATE_INTERPOLATOR);

                    ObjectAnimator starScaleYAnimator = ObjectAnimator.ofFloat(ivStar, ImageView.SCALE_Y, 0.2f, 1f);
                    starScaleYAnimator.setDuration(350);
                    starScaleYAnimator.setStartDelay(250);
                    starScaleYAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);

                    ObjectAnimator starScaleXAnimator = ObjectAnimator.ofFloat(ivStar, ImageView.SCALE_X, 0.2f, 1f);
                    starScaleXAnimator.setDuration(350);
                    starScaleXAnimator.setStartDelay(250);
                    starScaleXAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);

                    ObjectAnimator dotsAnimator = ObjectAnimator.ofFloat(vDotsView, DotsView.DOTS_PROGRESS, 0, 1f);
                    dotsAnimator.setDuration(900);
                    dotsAnimator.setStartDelay(50);
                    dotsAnimator.setInterpolator(ACCELERATE_DECELERATE_INTERPOLATOR);

                    animatorSet.playTogether(
                            outerCircleAnimator,
                            innerCircleAnimator,
                            starScaleYAnimator,
                            starScaleXAnimator,
                            dotsAnimator
                    );

                    animatorSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationCancel(Animator animation) {
                            vCircle.setInnerCircleRadiusProgress(0);
                            vCircle.setOuterCircleRadiusProgress(0);
                            vDotsView.setCurrentProgress(0);
                            ivStar.setScaleX(1);
                            ivStar.setScaleY(1);
                        }
                    });

                    animatorSet.start();
                }
            }
        });
        //setOnClickListener(this);
        ivStar.setOnTouchListener(new OnTouchListener() {
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ivStar.animate().scaleX(0.7f).scaleY(0.7f).setDuration(150).setInterpolator(DECCELERATE_INTERPOLATOR);
                        setPressed(true);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float x = event.getX();
                        float y = event.getY();
                        boolean isInside = (x > 0 && x < getWidth() && y > 0 && y < getHeight());
                        if (isPressed() != isInside) {
                            setPressed(isInside);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        ivStar.animate().scaleX(1).scaleY(1).setInterpolator(DECCELERATE_INTERPOLATOR);
                        if (isPressed()) {
                            v.performClick();
                            setPressed(false);
                        }
                        break;
                }
                return true;
            }
        });


    }


}
