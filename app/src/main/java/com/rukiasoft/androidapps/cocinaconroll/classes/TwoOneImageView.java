package com.rukiasoft.androidapps.cocinaconroll.classes;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Raúl Feliz Alonso on 6/9/15.
 */
public class TwoOneImageView extends ImageView {
    public TwoOneImageView(Context context) {
        super(context);
    }

    public TwoOneImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TwoOneImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthSpec, int HeightSpec){
        int twoOneHeight = MeasureSpec.getSize(widthSpec) /2;
        int twoOneHeightSpec = MeasureSpec.makeMeasureSpec(twoOneHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthSpec, twoOneHeightSpec);
    }
}
