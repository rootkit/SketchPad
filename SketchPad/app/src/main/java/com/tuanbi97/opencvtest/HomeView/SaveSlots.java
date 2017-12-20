package com.tuanbi97.opencvtest.HomeView;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.Log;

import com.tuanbi97.opencvtest.R;

import java.io.File;

import static java.lang.Math.abs;

public class SaveSlots {

    private Context context;
    private Rect clickBox;
    public Bitmap[] savedImages;
    private Bitmap[] ImagesAva;
    private Point[] sImagesPos;
    private int[] sImagesId;
    private int nSavedImages;
    public int selectedID = 0;
    private ValueAnimator animator;

    public SaveSlots(Context context){
        this.context = context;
        init();
    }

    public void setAnimator(){
        animator = new ValueAnimator();
        animator.setDuration(150l);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float newPosition = (float) animation.getAnimatedValue();
                Log.i("animation", Float.toString(newPosition));
                sImagesPos[3] = new Point(0, newPosition);
                for (int i = 0; i < 7; i++){
                    sImagesPos[i] = new Point(0, newPosition + (i - 3)*144);
                }
            }
        });
        animator.setFloatValues(sImagesPos[3].y, 360);
        animator.start();
    }

    private void init() {
        clickBox = new Rect(0, 0, 300, 720);

        //Load saved images
        String path = Environment.getExternalStorageDirectory().toString()+"/SavedSketch";
        //Log.i("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        //Log.i("Files", "Size: "+ files.length);
        savedImages = new Bitmap[files.length + 1];
        ImagesAva = new Bitmap[files.length + 1];
        for (int i = 0; i < files.length; i++)
        {
            //Log.i("Files", "FileName:" + files[i].getName());
            savedImages[i] = BitmapFactory.decodeFile(path + "/" + files[i].getName());
            ImagesAva[i] = createSlotAvatar(files[i].getName());
        }
        Bitmap blank = Bitmap.createBitmap(720, 720, Bitmap.Config.ARGB_8888);
        savedImages[files.length] = Bitmap.createBitmap(blank);
        ImagesAva[files.length] = BitmapFactory.decodeResource(context.getResources(), R.drawable.pencilblank);
        nSavedImages = files.length + 1;

        //Init position
        sImagesPos = new Point[7];
        sImagesId = new int[7];
        for (int i = 0; i < 7; i++){
            sImagesPos[i] = new Point(0, 360 + (i - 3) * 144);
            sImagesId[i] = ((i - 3) % nSavedImages + nSavedImages) % nSavedImages;
        }
    }

    private Bitmap createSlotAvatar(String filename) {
        Bitmap src = BitmapFactory.decodeResource(context.getResources(), R.drawable.pencil);
        Bitmap dst = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(dst);
        canvas.drawBitmap(src, 0, 0, null);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(30);
        paint.setTextSize(40);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.createFromAsset(context.getAssets(), "cour.ttf"));
        paint.setFakeBoldText(true);
        canvas.drawText(filename.substring(0, filename.length() - 4), 20, src.getHeight()/2 + 10, paint);
        return dst;
    }

    public Bitmap getResizedBitmap(Bitmap bm, double ratio) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = (float) ratio;
        float scaleHeight = (float) ratio;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public void draw(Canvas canvas){
        Paint paint = new Paint();
        //Log.i("sImageslength", Integer.toString(sImagesId.length));
        for (int i = 0; i < sImagesId.length; i++){
            int id = sImagesId[i];
            //Log.i("sImages", Integer.toString(id));
            Point pos = sImagesPos[i];
            //Log.i("sImages", Float.toString(pos.x) + " " + Float.toString(pos.y));
            int alpha = (int) (abs(pos.y - 360) * (-255.0/432.0) + 255.0);
            paint.setAlpha(alpha);
            Bitmap bmp = ImagesAva[id];
            float ratio = (float) Math.max((float) (abs(pos.y - 360) * (-0.6/432.0) + 1.2), 0.6);
            bmp = getResizedBitmap(bmp, ratio);
            canvas.drawBitmap(bmp, pos.x, pos.y - bmp.getHeight() / 2, paint);
        }
    }

    public boolean onClick(float x, float y) {
        if (x >= clickBox.left && x <= clickBox.right && y >= clickBox.top && y <= clickBox.bottom){
            return true;
        }
        else return false;
    }

    public void updatePos(float dx, float dy) {
        //sImagesPos[0].y += dy;
        int lc = 0;
        for (int i = 0; i < sImagesPos.length; i++){
            //sImagesPos[i].x += dx;
            sImagesPos[i].y += dy;
            if (abs(sImagesPos[i].y - 360.0) < 72 && selectedID != sImagesId[i]){
                lc = 1;
                selectedID = sImagesId[i];
            }
        }
        //Log.i("selectedID", Integer.toString(selectedID));
        if (lc == 1){
            if (dy >= 0){
                for (int  i = sImagesPos.length - 1; i > 0; i--){
                    sImagesPos[i] = sImagesPos[i - 1];
                    sImagesId[i] = sImagesId[i - 1];
                }
                sImagesPos[0] = new Point(0, sImagesPos[3].y - 3*144);
                sImagesId[0] = ((sImagesId[1] - 1) % nSavedImages + nSavedImages) % nSavedImages;
            }
            else{
                for (int  i = 0; i < sImagesPos.length - 1; i++){
                    sImagesPos[i] = sImagesPos[i + 1];
                    sImagesId[i] = sImagesId[i + 1];
                }
                sImagesPos[sImagesPos.length - 1] = new Point(0, sImagesPos[3].y + 3*144);
                sImagesId[sImagesPos.length - 1] = (sImagesId[sImagesPos.length - 1] + 1) % nSavedImages;
            }
        }
    }
}
