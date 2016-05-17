package com.rukiasoft.androidapps.cocinaconroll.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * Created by Ruler in 2014.
 */
public class ThanksActivity extends AppCompatActivity {

    @BindView(R.id.standard_toolbar) Toolbar mToolbar;
    @BindView(R.id.textView_support_recipes) TextView support;
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_thanks);
        unbinder = ButterKnife.bind(this);


        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            if(getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        //TextView support = (TextView)getWindow().getDecorView().findViewById(R.id.textView_support_recipes);
        Tools tools = new Tools();
        String sSupport = String.format(getResources().getString(R.string.support_recipes),
                tools.getApplicationName(getApplicationContext()), Constants.EMAIL);

        //sSupport = sSupport.replace("_app_name_", appName );
        support.setText(sSupport);


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
        finish();
    }
}
