package com.rukiasoft.androidapps.cocinaconroll.ui;

import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.dragandswipehelper.ItemTouchHelperAdapter;
import com.rukiasoft.androidapps.cocinaconroll.dragandswipehelper.ItemTouchHelperViewHolder;
import com.rukiasoft.androidapps.cocinaconroll.dragandswipehelper.OnStartDragListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class EditRecipeRecyclerViewAdapter extends RecyclerView.Adapter<EditRecipeRecyclerViewAdapter.ItemViewHolder>
        implements ItemTouchHelperAdapter{

    private final List<String> mItems;
    private final OnStartDragListener mDragStartListener;


    public EditRecipeRecyclerViewAdapter(List<String> data, OnStartDragListener dragStartListener) {
        mDragStartListener = dragStartListener;
        mItems = new ArrayList<>(data);
    }


    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_edit_recipe_recycler_item, parent, false);
        return new ItemViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        holder.textView.setText(mItems.get(position));

        // Start a drag whenever the handle view it touched
        holder.handleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public void onItemDismiss(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    public void addItem(String item){
        mItems.add(item);
        notifyItemChanged(mItems.size()-1);
    }

    public List<String> getItems() {
        return mItems;
    }

    /**
     * Simple example of a view holder that implements {@link ItemTouchHelperViewHolder} and has a
     * "handle" view that initiates a drag event when touched.
     */
    public static class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder{

        @BindView(R.id.edit_recipe_item_text) TextView textView;
        @BindView(R.id.edit_recipe_item_handle) ImageView handleView;
        private Unbinder unbinder;

        public ItemViewHolder(View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }



    }
}