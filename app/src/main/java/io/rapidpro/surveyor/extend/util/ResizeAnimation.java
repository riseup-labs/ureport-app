package io.rapidpro.surveyor.extend.util;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import io.rapidpro.surveyor.extend.StaticMethods;

public class ResizeAnimation extends Animation {

    final int startHeight;
    final int targetHeight;
    View view;

    public ResizeAnimation(Context context, View view, float newHeight) {
        this.view = view;
        startHeight = view.getHeight();
        this.targetHeight = StaticMethods.dip2px(context, newHeight) ;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newHeight = (int) (startHeight + (targetHeight - startHeight) * interpolatedTime);
        view.getLayoutParams().height = newHeight;
        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
