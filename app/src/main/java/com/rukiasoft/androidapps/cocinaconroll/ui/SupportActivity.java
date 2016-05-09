package com.rukiasoft.androidapps.cocinaconroll.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.rukiasoft.androidapps.cocinaconroll.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SupportActivity extends AppCompatActivity {

    @Nullable@Bind(R.id.support_title) TextView supportTittle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        ButterKnife.bind(this);
        supportTittle.setLinkTextColor(Color.CYAN);
        supportTittle.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @OnClick(R.id.close_support_button)
    public void closeActivity(View view) {
        finish();
    }
}
