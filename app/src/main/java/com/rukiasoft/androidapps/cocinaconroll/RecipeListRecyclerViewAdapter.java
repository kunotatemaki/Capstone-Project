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

package com.rukiasoft.androidapps.cocinaconroll;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rukiasoft.androidapps.cocinaconroll.loader.RecipeItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RecipeListRecyclerViewAdapter extends RecyclerView.Adapter<RecipeListRecyclerViewAdapter.RecipeViewHolder>
        implements View.OnClickListener {

    private List<RecipeItem> mItems;
    //private List<RecipeItem> filteredItems;
    //private List<RecipeItem> shownItems;
    private OnItemClickListener onItemClickListener;
    private Context mContext;


    public RecipeListRecyclerViewAdapter(Context context, List<RecipeItem> items) {
        this.mItems = new ArrayList<>(items);
        //this.filteredItems = new ArrayList<>(items);
        //this.shownItems = new ArrayList<>(items);
        //Collections.sort( this.mItems, comparatorName);
        this.mContext = context;
    }

    /*public void setData(List<RecipeItem> items){
        this.shownItems = new ArrayList<>(items);
        Collections.sort( this.shownItems, comparatorName);
        notifyDataSetChanged();
    }*/

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_recycler_item, parent, false);
        v.setOnClickListener(this);
        return new RecipeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        RecipeItem item = mItems.get(position);
        holder.bindRecipe(mContext, item);
        holder.itemView.setTag(item);
    }

    @Override public int getItemCount() {
        return mItems.size();
    }

    /*public void animateTo(List<RecipeItem> recipes) {
        applyAndAnimateRemovals(recipes);
        applyAndAnimateAdditions(recipes);
        applyAndAnimateMovedItems(recipes);
    }

    private void applyAndAnimateRemovals(List<RecipeItem> newModels) {
        for (int i = mItems.size() - 1; i >= 0; i--) {
            final RecipeItem model = mItems.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<RecipeItem> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final RecipeItem model = newModels.get(i);
            if (!mItems.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<RecipeItem> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final RecipeItem model = newModels.get(toPosition);
            final int fromPosition = mItems.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public RecipeItem removeItem(int position) {
        final RecipeItem model = mItems.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, RecipeItem model) {
        mItems.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final RecipeItem model = mItems.remove(fromPosition);
        mItems.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }*/
    
    @Override public void onClick(final View v) {
        // Give some time to the ripple to finish the effect
        if (onItemClickListener != null) {
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    onItemClickListener.onItemClick(v, (RecipeItem) v.getTag());
                }
            }, 200);
        }
    }

    protected static class RecipeViewHolder extends RecyclerView.ViewHolder {
        public @Bind(R.id.recipe_item_thumbnail) ImageView recipeThumbnail;
        public @Bind(R.id.recipe_title) TextView recipeTitle;
        public @Bind(R.id.recipe_item_background_protection) ImageView backgroundProtection;
        public @Bind(R.id.recipe_item_favorite_icon) ImageView favoriteIcon;
        public @Bind(R.id.recipe_item_own_recipe_icon) ImageView ownRecipeIcon;
        public @Bind(R.id.recipe_item_type_icon) ImageView typeIcon;
        public @Bind(R.id.recipe_item_vegetarian_recipe_icon) ImageView vegetarianIcon;

        public RecipeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindRecipe(Context context, RecipeItem item) {
            recipeTitle.setText(item.getName());
            int visibilityProtection = View.GONE;
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

            if(item.getType().equals(Constants.TYPE_DESSERTS)){
                typeIcon.setImageDrawable(ContextCompat.getDrawable(context, (R.drawable.ic_dessert_18)));
            }else if(item.getType().equals(Constants.TYPE_MAIN)){
                typeIcon.setImageDrawable(ContextCompat.getDrawable(context, (R.drawable.ic_main_18)));
            }else if(item.getType().equals(Constants.TYPE_STARTERS)){
                typeIcon.setImageDrawable(ContextCompat.getDrawable(context, (R.drawable.ic_starters_18)));
            }
            //TODO - ver cómo lee las recetas de los otros sitios
            Glide.with(context)
                    .load(Uri.parse(item.getPath()))
                    .centerCrop()
                    .error(R.drawable.default_dish)
                    .into(recipeThumbnail);
        }
    }

    /*private void refresh(){
        Collections.sort(filteredItems, comparatorName);
        notifyDataSetChanged();
    }*/

    public interface OnItemClickListener {

        void onItemClick(View view, RecipeItem recipeItem);

    }
}
