package com.rukiasoft.androidapps.cocinaconroll.classes;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by iRuler on 6/9/15.
 */
public class ThreeTwoPortraitImageView extends ImageView {
    public ThreeTwoPortraitImageView(Context context) {
        super(context);
    }

    public ThreeTwoPortraitImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThreeTwoPortraitImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec){
        int threeTwoHeight = MeasureSpec.getSize(widthSpec) *2/3;
        int threeTwoHeightSpec = MeasureSpec.makeMeasureSpec(threeTwoHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthSpec, threeTwoHeightSpec);
    }
}
