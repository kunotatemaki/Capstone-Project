package com.rukiasoft.androidapps.cocinaconroll.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.dragandswipehelper.OnStartDragListener;
import com.rukiasoft.androidapps.cocinaconroll.dragandswipehelper.SimpleItemTouchHelperCallback;
import com.rukiasoft.androidapps.cocinaconroll.classes.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import butterknife.Bind;
import butterknife.ButterKnife;


public class EditRecipeStepsFragment extends Fragment implements OnStartDragListener {

    private static final String KEY_ITEM_TO_ADD = Constants.PACKAGE_NAME + ".itemtoadd";
    private RecipeItem recipeItem;
    //private static final String TAG = "EditRecipeIngredientsFragment";
    @Bind(R.id.edit_recipe_add_item)EditText addItem;
    @Bind(R.id.edit_recipe_add_fab)FloatingActionButton fab;
    @Bind(R.id.edit_recipe_recycler_view) RecyclerView recyclerView;
    @Bind(R.id.edit_recipe_tip_text) EditText tip;

    private EditRecipeRecyclerViewAdapter mAdapter;

    private ItemTouchHelper mItemTouchHelper;
    private Tools mTools;


    public EditRecipeStepsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTools = new Tools();
        //setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_recipe_items_steps, container, false);
        ButterKnife.bind(this, view);

        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!addItem.getText().toString().isEmpty()) {
                    mAdapter.addItem(addItem.getText().toString());
                    mTools.hideSoftKeyboard(getActivity());
                    addItem.setText("");
                }
            }
        });

        setRecipe();

        if(savedInstanceState != null && savedInstanceState.containsKey(KEY_ITEM_TO_ADD))
            addItem.setText(savedInstanceState.getString(KEY_ITEM_TO_ADD));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tip.setText(recipeItem.getTip());

        mAdapter = new EditRecipeRecyclerViewAdapter(getActivity(), recipeItem.getSteps(), this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    private void setRecipe(){
        if(getActivity() instanceof EditRecipeActivity){
            recipeItem = ((EditRecipeActivity) getActivity()).getRecipe();
        }
    }

    public void saveData(){
        recipeItem.setTip(tip.getText().toString());
        recipeItem.setSteps(mAdapter.getItems());
    }

}

///https://github.com/iPaulPro/Android-ItemTouchHelper-Demo/blob/master/app/src/main/java/co/paulburke/android/itemtouchhelperdemo/RecyclerListFragment.java
//https://github.com/iPaulPro/Android-ItemTouchHelper-Demo