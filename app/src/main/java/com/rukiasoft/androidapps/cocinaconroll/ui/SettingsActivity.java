package com.rukiasoft.androidapps.cocinaconroll.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.rukiasoft.androidapps.cocinaconroll.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * Created by Ruler in 2014.
 */
public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.standard_toolbar) Toolbar mToolbar;
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        unbinder = ButterKnife.bind(this);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            if(getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                //getSupportActionBar().setTitle(Html.fromHtml("<b>" + getSupportActionBar().getTitle() + "</b>"));
            }
        }

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_fragment_container, new SettingsFragmentSupport())
                    .commit();
        }else{
            getFragmentManager().beginTransaction()
                    .replace(R.id.settings_fragment_container, new SettingsFragment())
                    .commit();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //Log.d(TAG, "presiono back y vuelvo");
        setResult(RESULT_OK);
        finish();
    }
}
