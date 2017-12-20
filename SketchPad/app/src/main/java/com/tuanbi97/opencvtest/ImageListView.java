package com.tuanbi97.opencvtest;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.tuanbi97.opencvtest.HomeView.Point;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.PI;

/**
 * Created by User on 9/18/2017.
 */

public class ImageListView extends View {

    public ArrayList<Bitmap> images;
    private Point[] sImagePos;
    private int[] sImageId;
    public int selectedId;
    private int sizeImage;
    private float dist2Images;
    private int numDisplay;//odd number
    private Point pivot;
    private int focusId;
    private float oldX, oldY;
    private boolean isClick = false;
    private int nImages;
    private Timer timer;
    private TimerTask timerTask;
    private ValueAnimator animator;
    private OnClickListener onClickListener = null;
    private OnItemChanged onItemChanged = null;

    public void setOnItemChangedListener(OnItemChanged onItemChangedListener) {
        this.onItemChanged = onItemChangedListener;
    }

    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener{
        public void onClickListener(View v, Bitmap bitmap, int selectedId);
    }

    public interface OnItemChanged{
        public void onItemChanged(View v, Bitmap bmp, int selectedId);
    }

    public ImageListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                postInvalidate();
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 40);
    }

    public void setImage(ArrayList<Bitmap> images, int numDisplay, int focusindex){
        this.nImages = images.size();
        this.images = images;
        sImagePos = new Point[numDisplay];
        sImageId = new int[numDisplay];
        this.numDisplay = numDisplay;
        sizeImage = getWidth();
        dist2Images = sizeImage + 4;
        focusId = focusindex;
        pivot = new Point(0, sizeImage/2 + (focusId - 1) * dist2Images);
        selectedId = 0;
        for (int i = 0; i < sImagePos.length; i++){
            sImagePos[i] = new Point(0, pivot.y + (i - focusId)*dist2Images);
            sImageId[i] = ((i - focusId)%nImages + nImages)%nImages;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:{
                isClick = true;
                break;
            }
            case MotionEvent.ACTION_MOVE:{
                if ((touchX-oldX)*(touchX-oldX) + (touchY-oldY)*(touchY-oldY) > 10) isClick = false;
                int lc = 0;
                for (int i = 0; i < numDisplay; i++){
                    sImagePos[i].y += (touchY - oldY);
                    if (Math.abs(sImagePos[i].y - pivot.y) < sizeImage/2 && i != focusId){
                        selectedId = sImageId[i];
                        if (onItemChanged != null)
                            onItemChanged.onItemChanged(this, images.get(selectedId), selectedId);
                        lc = 1;
                    }
                }
                if (lc == 1){
                    if (touchY - oldY > 0){
                        for (int i = numDisplay - 1; i > 0; i--){
                            sImageId[i] = sImageId[i - 1];
                            sImagePos[i] = sImagePos[i - 1];
                        }
                        sImageId[0] = ((sImageId[0] - 1)% nImages + nImages)%nImages;
                        sImagePos[0] = new Point(0, sImagePos[0].y - dist2Images);
                    }
                    else{
                        for (int i = 0; i < numDisplay - 1; i++){
                            sImageId[i] = sImageId[i + 1];
                            sImagePos[i] = sImagePos[i + 1];
                        }
                        sImageId[numDisplay - 1] = ((sImageId[numDisplay - 1] + 1)%nImages + nImages) % nImages;
                        sImagePos[numDisplay - 1] = new Point(0, sImagePos[numDisplay - 1].y + dist2Images);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:{
                if (isClick == true){
                    if (onClickListener != null){
                        onClickListener.onClickListener(this, images.get(selectedId), selectedId);
                    }
                }
                else
                if(isClick == false){
                    setAnimator();
                }
                break;
            }
            default:
                return false;
        }
        oldX = touchX;
        oldY = touchY;
        return true;
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
        for (int i = 0; i < numDisplay; i++){
            Bitmap bmp = images.get(sImageId[i]);
            bmp = getResizedBitmap(bmp, sizeImage, sizeImage);
            //Log.i("posX", Float.toString(sImagePos[i].x));
            Paint paint = new Paint();
            float dist = Math.abs(sImagePos[i].y - pivot.y);
            paint.setAlpha((int) (-255.0/((numDisplay/2 + 1)*sizeImage) * dist + 255.0));
            canvas.drawBitmap(bmp, sImagePos[i].x, sImagePos[i].y - bmp.getHeight()/2, paint);
        }
    }

    public void addImage(Bitmap bmp){
        if (selectedId == 0) {
            images.add(bmp);
            selectedId = images.size() - 1;
            if (onItemChanged != null)
                onItemChanged.onItemChanged(this, images.get(selectedId), selectedId);
            nImages += 1;
            for (int i = 0; i < sImagePos.length; i++){
                sImageId[i] = ((i - focusId)%nImages + nImages)%nImages;
            }
            for (int i = numDisplay - 1; i > 0; i--){
                sImageId[i] = sImageId[i - 1];
                sImagePos[i] = sImagePos[i - 1];
            }
            sImageId[0] = ((sImageId[0] - 1)% nImages + nImages)%nImages;
            sImagePos[0] = new Point(0, sImagePos[0].y - dist2Images);
            setAnimator();
        }
        else {
            images.set(selectedId, bmp);
        }
    }

    public void setAnimator(){
        animator = new ValueAnimator();
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float newPosition = (float) animation.getAnimatedValue();
                for (int i = 0; i < numDisplay; i++){
                    sImagePos[i].y = newPosition + (i - focusId) * dist2Images;
                }
            }
        });
        animator.setFloatValues((float)sImagePos[focusId].y, pivot.y);
        animator.start();
    }

}
