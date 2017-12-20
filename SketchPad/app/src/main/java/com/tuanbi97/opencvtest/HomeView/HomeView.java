package com.tuanbi97.opencvtest.HomeView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.tuanbi97.opencvtest.HomeActivity;
import com.tuanbi97.opencvtest.MainActivity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by User on 9/16/2017.
 */

public class HomeView extends View {
    Context context;
    private TimerTask timerTask;
    private Timer timer;
    private SaveSlots saveSlots;
    private DrawButton drawButton;
    private ImagePreview imagePreview;
    private int touchViewID;
    private float oldX, oldY;
    private int cr = 30, cg = 100, cb = 250, vr = 5, vg = 10, vb = 10;
    private boolean isClick = true;

    public HomeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
        this.setBackgroundColor(Color.argb(255, cr, cg, cb));
    }

    void init(){
        //init timer
        timerTask = new TimerTask() {
            @Override
            public void run() {
                postInvalidate();
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 40);

        //init cancdidate views
        saveSlots = new SaveSlots(context);
        drawButton = new DrawButton(context);
        imagePreview = new ImagePreview(context, 500, 160, 500 + 400, 160 + 400);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        imagePreview.setImage(canvas, saveSlots.savedImages[saveSlots.selectedID]);
        saveSlots.draw(canvas);
        drawButton.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        //respond to down, move and up events
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                isClick = true;
                if (saveSlots.onClick(touchX, touchY)) {
                    touchViewID = 1;
                }
                else
                if (drawButton.onClick(touchX, touchY)){
                    touchViewID = 2;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if ((touchX-oldX)*(touchX-oldX) + (touchY-oldY)*(touchY-oldY) > 10) isClick = false;
                Log.i("checkMove", "true");
                switch (touchViewID){
                    case 1: {
                        saveSlots.updatePos(touchX - oldX, touchY - oldY);
                        int color = getCurrentColor();
                        this.setBackgroundColor(color);
                        break;
                    }
                    case 2:{
                        drawButton.updatePos(new Point(oldX, oldY), new Point(touchX, touchY));
                        break;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (isClick == false) {
                    switch (touchViewID) {
                        case 1: {
                            saveSlots.setAnimator();
                            break;
                        }
                        case 2: {
                            drawButton.setAnimator();
                            break;
                        }
                    }
                }
                else{
                    switch (touchViewID) {
                        case 2: {
                            Intent intent = new Intent(context, MainActivity.class);
                            context.startActivity(intent);
                            break;
                        }
                    }
                }
                touchViewID = 0;
                break;
            }
            default:
                return false;
        }
        oldX = touchX;
        oldY = touchY;
        return true;
    }

    private int getCurrentColor() {
        cb += vb;
        if (cb > 255 || cb < 0){
            vb = -vb;
            if (cb < 0) cb = 0;
            if (cb > 255) cb = 255;
            cg += vg;
            if (cg > 255 || cg < 0){
                vg = -vg;
                if (cg < 0) cg = 0;
                if (cg > 255) cg = 255;
                cr += vr;
                if (cr > 50 || cr < 30){
                    vr = -vr;
                    if (cr > 50) cr = 50;
                    if (cr < 30) cr = 30;
                }
            }
        }
        return Color.argb(0xff, cr, cg, cb);
    }
}
