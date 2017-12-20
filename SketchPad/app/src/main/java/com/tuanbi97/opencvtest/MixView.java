package com.tuanbi97.opencvtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by User on 9/19/2017.
 */

public class MixView extends View {

    public class proObject{
        public float posx, posy;
        public float scale;
        public Bitmap bmp;
        public Bitmap src;
        public proObject(float posx, float posy, float scale, Bitmap bmp){
            this.posx = posx;
            this.posy = posy;
            this.scale = scale;
            this.bmp = getResizedBitmap(bmp, scale);
            this.src = bmp;
        }
    }

    public ArrayList<proObject> objects;
    private Timer timer;
    private TimerTask timerTask;
    private int moveid = 0;
    private float oldX, oldY;
    public MixView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        objects = new ArrayList<>(0);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                postInvalidate();
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 40);
    }

    public void addObject(int posx, int posy, float scale, Bitmap bitmap) {
        objects.add(new proObject(posx, posy, scale, bitmap));
    }

    public void setObject(int index, int posx, int posy, float scale, Bitmap bitmap){
        objects.get(index).src = bitmap;
        objects.get(index).bmp = getResizedBitmap(bitmap, scale);
        objects.get(index).posx = posx;
        objects.get(index).posy = posy;
        objects.get(index).scale = scale;
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

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i("MixView","begin draw");
        for (int i = 0; i < objects.size(); i++){
            Log.i("MixView", Integer.toString(i));
            canvas.drawBitmap(objects.get(i).bmp, objects.get(i).posx, objects.get(i).posy, null);
        }
    }

    private boolean checkin(float x, float y, proObject p){
        if (x >= p.posx && x <= p.posx + p.bmp.getWidth() && y >= p.posy && y <= p.posy + p.bmp.getHeight()) return true;
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN: {
                for (int i = objects.size() - 1; i > 0; i--){
                    if (checkin(touchX, touchY, objects.get(i))){
                        moveid = i;
                        break;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:{
                if (moveid > 0) {
                    objects.get(moveid).posx += (touchX - oldX);
                    objects.get(moveid).posy += (touchY - oldY);
                }
                break;
            }
            case MotionEvent.ACTION_UP:{
                moveid = 0;
                break;
            }
            default:
                return false;
        }
        oldX = touchX;
        oldY = touchY;
        return true;
    }
}
