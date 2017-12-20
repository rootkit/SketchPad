package com.tuanbi97.opencvtest.HomeView;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;

import com.tuanbi97.opencvtest.R;

import java.io.File;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Created by User on 9/17/2017.
 */

public class DrawButton {
    private Context context;
    private Rect clickBox;
    private Bitmap[] ImagesAva;
    private double[] sImagesAngle;
    private int[] sImagesId;
    public int selectedID = 0;
    private int nAva;
    private ValueAnimator animator;
    private Point pivot;
    private float radius;

    public DrawButton(Context context){
        this.context = context;
        init();
    }

    public void setAnimator(){
        animator = new ValueAnimator();
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float newPosition = (float) animation.getAnimatedValue();
                sImagesAngle[1] = newPosition;
                for (int i = 0; i < 3; i++){
                    sImagesAngle[i] = newPosition + PI/3 * (i - 1);
                }
            }
        });
        animator.setFloatValues((float)(sImagesAngle[1]), (float)(PI/4));
        animator.start();
    }

    private void init() {
        radius = 250;
        pivot = new Point(1030 + 120, 470 + 120);
        clickBox = new Rect(910, 350, 1280, 720);

        //Load saved images
        ImagesAva = new Bitmap[3];
        ImagesAva[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.buttondraw);
        ImagesAva[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.buttonstar);
        ImagesAva[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.buttonnote);
        nAva = ImagesAva.length;

        //Init position
        sImagesAngle = new double[3];
        sImagesId = new int[3];
        for (int i = 0; i < 3; i++){
            sImagesAngle[i] = (float) (PI/4 + (i - 1)*PI/3);
            sImagesId[i] = ((i - 1) % nAva + nAva) % nAva;
        }
    }

    public void draw(Canvas canvas){
        Paint paint = new Paint();
        for (int i = 0; i < sImagesId.length; i++){
            int id = sImagesId[i];
            Point pos = new Point((float)(1280 - cos(sImagesAngle[i]) * radius), (float)(720 - sin(sImagesAngle[i]) * radius));
            int alpha = (int) ((-155.0*4.0)/PI * abs(sImagesAngle[i] - PI/4) + 255);
            paint.setAlpha(alpha);
            Bitmap bmp = ImagesAva[id];
            canvas.drawBitmap(bmp, pos.x - bmp.getWidth() / 2, pos.y - bmp.getHeight() / 2, paint);
        }
    }

    public boolean onClick(float x, float y) {
        if (x >= clickBox.left && x <= clickBox.right && y >= clickBox.top && y <= clickBox.bottom){
            return true;
        }
        else return false;
    }

    private double angle(float y, float x){
        return atan2(y, x);
    }

    public void updatePos(Point pOld, Point pNew) {
        double da = angle(720 - pNew.y, 1280 - pNew.x) - angle(720 - pOld.y, 1280 - pOld.x);
       // Log.i("Rotate", Double.toString(da) + " " + Integer.toString(selectedID));
        int lc = 0;
        for (int i = 0; i < sImagesAngle.length; i++){
            sImagesAngle[i] += da;
            if (abs(sImagesAngle[i] - PI/4) < PI/8  && selectedID != sImagesId[i]){
                selectedID = sImagesId[i];
                lc = 1;
            }
        }
      //  Log.i("Rotate", Double.toString(da) + " " + Integer.toString(selectedID));
        if (lc == 1){
            if (da < 0){
                for (int i = 0; i < sImagesId.length - 1; i++){
                    sImagesId[i] = sImagesId[i + 1];
                    sImagesAngle[i] = sImagesAngle[i + 1];
                }
                sImagesId[sImagesId.length - 1] = (sImagesId[sImagesId.length - 1] + 1) % nAva;
                sImagesAngle[sImagesId.length - 1] = sImagesAngle[1] + PI/3;
            }
            else{
                for (int i = sImagesAngle.length - 1; i > 0; i--){
                    sImagesId[i] = sImagesId[i - 1];
                    sImagesAngle[i] = sImagesAngle[i - 1];
                }
                sImagesId[0] = ((sImagesId[0] - 1) % nAva + nAva) % nAva;
                sImagesAngle[0] = sImagesAngle[1] - PI/3;
            }
        }
    }
}
