package com.rukiasoft.androidapps.cocinaconroll.ui;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.database.CocinaConRollContentProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SearchableActivity extends ToolbarAndRefreshActivity implements LoaderCallbacks<Cursor> {
	
	private ListView mLVRecipes;
	private SimpleCursorAdapter mCursorAdapter;
	@BindView(R.id.standard_toolbar)
	Toolbar mToolbarSearchActivity;
	private Unbinder unbinder;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbinder.unbind();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_searchable);
		unbinder = ButterKnife.bind(this);
		setToolbar(mToolbarSearchActivity);
		// Getting reference to Country List
		mLVRecipes = (ListView)findViewById(R.id.lv_recipes);
		
		// Setting item click listener		
		mLVRecipes.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView txt1 = (TextView) view.findViewById(android.R.id.text1);
                sendRecipeName(txt1.getText().toString());

            }
        });

		// Defining CursorAdapter for the ListView		
		mCursorAdapter = new SimpleCursorAdapter(getBaseContext(),
				android.R.layout.simple_list_item_1,
	            null,
	            new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1},
	            new int[] { android.R.id.text1}, 0);
		
		// Setting the cursor adapter for the country listview
		mLVRecipes.setAdapter(mCursorAdapter);
		
		// Getting the intent that invoked this activity
		Intent intent = getIntent();		
		
		// If this activity is invoked by selecting an item from Suggestion of Search dialog or 
		// from listview of SearchActivity
		if(intent.getAction().equals(Intent.ACTION_VIEW)){
            Uri uri = intent.getData();
            sendRecipeName(uri.getLastPathSegment());

		}else if(intent.getAction().equals(Intent.ACTION_SEARCH)){ // If this activity is invoked, when user presses "Go" in the Keyboard of Search Dialog
			String query = intent.getStringExtra(SearchManager.QUERY);
			doSearch(query);
		}		
	}

    private void sendRecipeName(String recipeName){
        Intent detailIntent = new Intent(this, RecipeListActivity.class);
        detailIntent.putExtra(Constants.KEY_RECIPE, recipeName);
		detailIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(detailIntent);
        finish();
    }
	
	private void doSearch(String query){
		Bundle data = new Bundle();
		data.putString("query", query);
		
		// Invoking onCreateLoader() in non-ui thread
		getSupportLoaderManager().initLoader(1, data, this);		
	}


	/** This method is invoked by initLoader() */
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle data) {
		Uri uri = CocinaConRollContentProvider.CONTENT_URI_SUGGESTIONS_WHEN_KEYBOARD_GO;
		return new CursorLoader(getBaseContext(), uri, null, null , new String[]{data.getString("query")}, null);
	}

	/** This method is executed in ui thread, after onCreateLoader() */
	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
		mCursorAdapter.swapCursor(c);		
	}


	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
	}


}