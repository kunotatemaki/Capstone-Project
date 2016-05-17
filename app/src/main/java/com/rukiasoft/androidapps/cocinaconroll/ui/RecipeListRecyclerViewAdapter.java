/*
 * Copyright (C) 2015 Antonio Leiva
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rukiasoft.androidapps.cocinaconroll.ui;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rukiasoft.androidapps.cocinaconroll.classes.LikeButtonView;
import com.rukiasoft.androidapps.cocinaconroll.utilities.CommonRecipeOperations;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.database.DatabaseRelatedTools;
import com.rukiasoft.androidapps.cocinaconroll.classes.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.utilities.ReadWriteTools;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class RecipeListRecyclerViewAdapter extends RecyclerView.Adapter<RecipeListRecyclerViewAdapter.RecipeViewHolder>
        implements View.OnClickListener, View.OnLongClickListener {

    private final List<RecipeItem> mItems;
    private OnCardClickListener onCardClickListener;
    private final Context mContext;
    private View frontCard = null;
    private View backCard = null;


    public RecipeListRecyclerViewAdapter(Context context, List<RecipeItem> items) {
        this.mItems = new ArrayList<>(items);
        this.mContext = context;
    }

    public void setOnCardClickListener(OnCardClickListener onCardClickListener) {
        this.onCardClickListener = onCardClickListener;
    }


    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_recycler_item, parent, false);
        RecipeViewHolder recipeViewHolder = new RecipeViewHolder(v);
        recipeViewHolder.cardView.setOnClickListener(this);
        recipeViewHolder.cardView.setOnLongClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if(recipeViewHolder.backCardView != null){
                recipeViewHolder.backCardView.setRotationY(180);
            }
        }
        /*recipeViewHolder.favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onBackFavoriteClickListener != null) {
                    final RecipeItem recipe = getRecipeFromParent(v);
                    if (v instanceof ImageView) {
                        ((ImageView) v).setImageDrawable(
                                (!recipe.getFavourite()) ? ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_white_48dp) :
                                        ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_outline_white_48dp)
                        );
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onBackFavoriteClickListener.onBackFavoriteClick(recipe);
                        }
                    }, 200);
                }
            }
        });*/


        return recipeViewHolder;
        /*v.setOnClickListener(this);
        return new RecipeViewHolder(v);*/
    }

    private RecipeItem getRecipeFromParent(View v){
        RecipeItem recipe = null;
        View aux = v;
        ViewParent parent;
        while((parent = aux.getParent()) != null){
            if(parent instanceof CardView){
                recipe = (RecipeItem) ((View)parent).getTag();
                break;
            }else{
                aux = (View) parent;
            }
        }
        if(recipe != null){
            CommonRecipeOperations commonRecipeOperations = new CommonRecipeOperations(mContext, recipe);
            recipe = commonRecipeOperations.loadRecipeDetailsFromRecipeCard();
        }
        return recipe;
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        RecipeItem item = mItems.get(position);
        holder.bindRecipe(mContext, item);
        holder.itemView.setTag(item);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (holder.cardView.getRotationY() != 0) {
                setFrontAndBack(holder.cardView);
                flipCard(holder.cardView);
            }
        }

        /*holder.favoriteButton.setImageDrawable(
                (item.getFavourite())? ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_white_48dp) :
                        ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_outline_white_48dp)
        );*/

    }

    @Override public int getItemCount() {
        return mItems.size();
    }

    @Override public long getItemId(int position){
        return mItems.get(position).hashCode();
    }

    @Override
    public void onClick(final View v) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setFrontAndBack(v);
            if (v.getRotationY() != 0) {
                return;
            }
        }

        // Give some time to the ripple to finish the effect
        if (onCardClickListener != null) {
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    onCardClickListener.onCardClick(v, (RecipeItem) v.getTag());
                }
            }, 200);
        }
    }

    @Override
    public boolean onLongClick(View v) {


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setFrontAndBack(v);
            if (frontCard != null && backCard != null) {
                flipCard(v);
            }
        }

        //setLeftIn.start();
        return true;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setFrontAndBack(View v){
        View faceA = null;
        View faceB = null;
        for(int i=0; i<((ViewGroup)v).getChildCount(); ++i) {
            View nextChild = ((ViewGroup)v).getChildAt(i);
            int id = nextChild.getId();
            if(id == R.id.back_cardview_recipe_item){
                faceB = nextChild;
            }else if(id == R.id.front_cardview_recipe_item) {
                faceA = nextChild;
            }
        }
       if (v.getRotationY() != 0) {
            frontCard = faceB;
            backCard = faceA;
        } else {
            frontCard = faceA;
            backCard = faceB;
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void flipCard(final View card){
        final View front = frontCard;
        final View back = backCard;
        final AnimatorSet flipCard = (card.getRotationY() == 0)?
                (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.animator.card_flip_rotate_half) :
                (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.animator.card_flip_rotate_full);
        final AnimatorSet disappear = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.animator.view_disappear);
        //final AnimatorSet flipPositive = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.animator.card_flip_rotate_full);
        final AnimatorSet appear = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.animator.view_appear);
        flipCard.setTarget(card);
        disappear.setTarget(front);
        //flipPositive.setTarget(card);
        appear.setTarget(back);
        //setLeftIn.setTarget(backView);
        flipCard.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                disappear.start();
                appear.start();
                back.setVisibility(View.VISIBLE);
                back.setAlpha(0);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                front.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        flipCard.start();
    }

    protected static class RecipeViewHolder extends RecyclerView.ViewHolder {
        public @BindView(R.id.recipe_pic_cardview) ImageView recipeThumbnail;
        public @BindView(R.id.recipe_title_cardview) TextView recipeTitle;
        public @BindView(R.id.recipe_pic_protection_cardview) ImageView backgroundProtection;
        public @BindView(R.id.recipe_item_favorite_icon) ImageView favoriteIcon;
        public @BindView(R.id.recipe_item_own_recipe_icon) ImageView ownRecipeIcon;
        public @BindView(R.id.recipe_item_type_icon) ImageView typeIcon;
        public @BindView(R.id.recipe_item_vegetarian_recipe_icon) ImageView vegetarianIcon;
        public @BindView(R.id.cardview_recipe_item)
        CardView cardView;
        public @BindView(R.id.front_cardview_recipe_item)
        LinearLayout frontCardView;
        public @Nullable @BindView(R.id.back_cardview_recipe_item)
        RelativeLayout backCardView;
        public @Nullable @BindView(R.id.recipe_item_favorite_button)
        LikeButtonView favoriteButton;
        ReadWriteTools rwTools;
        DatabaseRelatedTools dbTools;
        private Unbinder unbinder;

        public RecipeViewHolder(View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);

        }

        public void bindRecipe(Context context, RecipeItem item) {
            if(dbTools == null) dbTools = new DatabaseRelatedTools();
            if(rwTools == null) rwTools = new ReadWriteTools();
            recipeTitle.setText(item.getName());
            int visibilityProtection = View.GONE;
            if(favoriteButton != null) {
                favoriteButton.init(item, favoriteIcon);
            }
            if(item.getFavourite()){
                visibilityProtection = View.VISIBLE;
                favoriteIcon.setVisibility(View.VISIBLE);
            }else{
                favoriteIcon.setVisibility(View.GONE);
            }
            if((item.getState() & (Constants.FLAG_OWN | Constants.FLAG_EDITED)) !=0){
                visibilityProtection = View.VISIBLE;
                ownRecipeIcon.setVisibility(View.VISIBLE);
            }else{
                ownRecipeIcon.setVisibility(View.GONE);
            }
            if(item.getVegetarian()){
                visibilityProtection = View.VISIBLE;
                vegetarianIcon.setVisibility(View.VISIBLE);
            }else{
                vegetarianIcon.setVisibility(View.GONE);
            }
            backgroundProtection.setVisibility(visibilityProtection);

            switch (item.getType()) {
                case Constants.TYPE_DESSERTS:
                    typeIcon.setImageDrawable(ContextCompat.getDrawable(context, (R.drawable.ic_dessert_18)));
                    break;
                case Constants.TYPE_MAIN:
                    typeIcon.setImageDrawable(ContextCompat.getDrawable(context, (R.drawable.ic_main_18)));
                    break;
                case Constants.TYPE_STARTERS:
                    typeIcon.setImageDrawable(ContextCompat.getDrawable(context, (R.drawable.ic_starters_18)));
                    break;
            }
            rwTools.loadImageFromPath(context, recipeThumbnail, item.getPathPicture(),
                    R.drawable.default_dish_thumb, item.getVersion());
        }

    }

    public interface OnCardClickListener {
        void onCardClick(View view, RecipeItem recipeItem);
    }


}
