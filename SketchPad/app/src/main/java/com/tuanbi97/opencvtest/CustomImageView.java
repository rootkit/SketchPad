package com.tuanbi97.opencvtest;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.renderscript.Sampler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by User on 9/18/2017.
 */

public class CustomImageView extends View {

    private Bitmap src;
    private Bitmap bitmap;
    private Timer timer;
    private TimerTask timerTask;
    private ValueAnimator valueAnimator;
    private float ratio;

    public CustomImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                postInvalidate();
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 40);
    }

    public void setImage(Bitmap bmp){
        src = bmp;
        Log.i("suggest", Float.toString(getX()) + " " + Float.toString(getY()));
        bitmap = getResizedBitmap(bmp, getWidth(), getWidth());
        setAnimation();
    }

    private void setAnimation() {
        valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float newSize = (float) animation.getAnimatedValue();
                ratio = newSize;
            }
        });
        valueAnimator.setFloatValues((float)0.1, 1);
        valueAnimator.start();
    }

    public Bitmap getResizedBitmap(Bitmap bm, float ratio) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ratio;
        float scaleHeight = ratio;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap != null) {
            Bitmap rbmp = getResizedBitmap(bitmap, ratio);
            float posx = (float)(1.0 * getWidth() / 2 - 1.0 * rbmp.getWidth()/2);
            float posy = (float)(1.0 * getHeight() / 2 - 1.0 * rbmp.getHeight()/2);
            Log.i("suggest", Float.toString(posx) + " " + Float.toString(posy));
            canvas.drawBitmap(rbmp, posx, posy, null);
        }
    }

    public Bitmap getSrc(){
        return src;
    }
}
