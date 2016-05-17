package com.rukiasoft.androidapps.cocinaconroll.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AnimationActivity extends AppCompatActivity {

    @BindView(R.id.circle1)
    ImageView circle1;
    @BindView(R.id.circle2)
    ImageView circle2;
    @BindView(R.id.circle3)
    ImageView circle3;
    @BindView(R.id.circle4)
    ImageView circle4;
    @BindView(R.id.circle5)
    ImageView circle5;
    @BindView(R.id.circle6)
    ImageView circle6;
    @BindView(R.id.layout_circles)
    LinearLayout layoutCircles;
    @BindView(R.id.text_imagine_anim)
    TextView imagineText;
    @BindView(R.id.text_create_anim)
    TextView createText;
    @BindView(R.id.text_rukia_anim)
    TextView rukiaText;
    @BindView(R.id.text_soft_anim)
    TextView softText;
    private Unbinder unbinder;

    private Animation circle1Anim;
    private Animation circle2Anim;
    private Animation circle3Anim;
    private Animation circle4Anim;
    private Animation circle5Anim;
    private Animation circle6Anim;
    private Animation imagineAnim;
    private Animation createAnim;
    private Animation rukiaAnim;
    private Animation softAnim;
    Activity activity;
    private static final int REQUEST_CODE_SUPPORT = 89;


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation);
        unbinder = ButterKnife.bind(this);
        activity = this;
        //configure the screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= Build.VERSION_CODES.JELLY_BEAN){
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        } else{
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }


        circle1Anim = AnimationUtils.loadAnimation(this, R.anim.anim_circle_1);
        circle1Anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                circle1.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        circle2Anim = AnimationUtils.loadAnimation(this, R.anim.anim_circle_2);
        circle2Anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                circle2.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        circle3Anim = AnimationUtils.loadAnimation(this, R.anim.anim_circle_3);
        circle3Anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                circle3.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        circle4Anim = AnimationUtils.loadAnimation(this, R.anim.anim_circle_4);
        circle4Anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                circle4.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        circle5Anim = AnimationUtils.loadAnimation(this, R.anim.anim_circle_5);
        circle5Anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                circle5.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        circle6Anim = AnimationUtils.loadAnimation(this, R.anim.anim_circle_6);
        circle6Anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layoutCircles.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        imagineAnim = AnimationUtils.loadAnimation(this, R.anim.anim_imagine);
        imagineAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imagineText.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        createAnim = AnimationUtils.loadAnimation(this, R.anim.anim_create);
        createAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                createText.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        rukiaAnim = AnimationUtils.loadAnimation(this, R.anim.anim_rukia);
        rukiaAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rukiaText.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        softAnim = AnimationUtils.loadAnimation(this, R.anim.anim_soft);
        softAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                softText.setVisibility(View.INVISIBLE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(showSupportScreen()) {
                            Intent supportIntent = new Intent(activity, SupportActivity.class);
                            activity.startActivityForResult(supportIntent, REQUEST_CODE_SUPPORT);
                        }else {
                            finish();
                        }
                    }
                }, 200);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CODE_SUPPORT:
                finish();
                break;
            default:
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume");

        circle1.startAnimation(circle1Anim);
        circle2.startAnimation(circle2Anim);
        circle3.startAnimation(circle3Anim);
        circle4.startAnimation(circle4Anim);
        circle5.startAnimation(circle5Anim);
        circle6.startAnimation(circle6Anim);

        imagineText.startAnimation(imagineAnim);
        createText.startAnimation(createAnim);
        rukiaText.startAnimation(rukiaAnim);
        softText.startAnimation(softAnim);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private Boolean showSupportScreen(){
        Tools mTools = new Tools();
        return !mTools.getBooleanFromPreferences(this, Constants.PROPERTY_HIDE_SUPPORT_SCREEN);
    }
}
