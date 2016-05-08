package com.rukiasoft.androidapps.cocinaconroll.classes;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Raúl Feliz Alonso on 6/9/15.
 */
public class ThreeOneImageView extends ImageView {
    public ThreeOneImageView(Context context) {
        super(context);
    }

    public ThreeOneImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThreeOneImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec){
        int threeOneHeight = MeasureSpec.getSize(widthSpec)/3;
        int threeOneHeightSpec = MeasureSpec.makeMeasureSpec(threeOneHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthSpec, threeOneHeightSpec);
    }
}
