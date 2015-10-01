package com.rukiasoft.androidapps.cocinaconroll.classes;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by iRuler on 6/9/15.
 */
public class ThreeTwoLandRelativeLayout extends RelativeLayout {
    public ThreeTwoLandRelativeLayout(Context context) {
        super(context);
    }

    public ThreeTwoLandRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThreeTwoLandRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec){
        int threeTwoWidth = MeasureSpec.getSize(heightSpec) *2/3;
        int threeTwoWidthSpec = MeasureSpec.makeMeasureSpec(threeTwoWidth, MeasureSpec.EXACTLY);
        super.onMeasure(widthSpec, threeTwoWidthSpec);
    }
}
