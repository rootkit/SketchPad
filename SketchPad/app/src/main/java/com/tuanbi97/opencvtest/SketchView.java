package com.tuanbi97.opencvtest;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.InterpolatorRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by User on 9/13/2017.
 */

public class SketchView extends View {
    private Path drawPath;
    //defines what to draw
    private Paint canvasPaint;
    //defines how to draw
    private Paint drawPaint;
    //initial color
    private int paintColor = 0xFF000000;
    //canvas - holding pen, holds your drawings
    //and transfers them to the view
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;
    //brush size
    private float currentBrushSize, lastBrushSize;

    private int height, width;

    private int Xmin, Ymin, Xmax, Ymax;
    private double[] score;
    private double[] pscore;
    private CustomImageView[] im;
    private ArrayList<ArrayList<Bitmap>> suggestList;

    public ImageListView lv;


    private Context context;

    public class matchingTask extends AsyncTask<Object, Object, Object>{

        public native double[] findPartMatch(long matAddress, int Xmin, int Ymin, int Xmax, int Ymax, int patch_size, double overlap, int bx, int by, int b0, int nx, int ny, int n0);
        ProgressDialog asyncDialog = new ProgressDialog(context);
        @Override
        protected void onPreExecute() {
            asyncDialog.setTitle("Matching...");
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object... params) {
            return findPartMatch((long)params[0], (int)params[1], (int)params[2], (int)params[3], (int)params[4], 60, 0.75, 32, 32, 4, 6, 18 ,4);
        }

        @Override
        protected void onPostExecute(Object result) {
            pscore = (double[]) result;
            if (score == null){
                score = pscore;
            }
            else {
                for (int i = 0; i < pscore.length; i++)
                    score[i] += pscore[i];
            }
            int tmax = 0;
            for (int i = 0; i < score.length; i++){
                if (score[i] > score[tmax]) tmax = i;
            }
            for (int i = 0; i < 3;i++){
                im[i].setImage(suggestList.get(tmax).get(i));
            }
            for (int i = 0; i < pscore.length; i++){
                Log.i("partscore", Double.toString(score[i]));
            }
            asyncDialog.dismiss();
        }
    };

    private void init(){
        currentBrushSize = getResources().getInteger(R.integer.large_size);
        lastBrushSize = currentBrushSize;

        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(currentBrushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);

    }
    public SketchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0 , 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //create canvas of certain device size.
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;
        height = h;

        //create Bitmap of certain w,h
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        //apply bitmap to graphic to start drawing.
        drawCanvas = new Canvas(canvasBitmap);
        drawCanvas.drawColor(0xffffffff);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        //respond to down, move and up events
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Xmin = (int) touchX;
                Ymin = (int) touchY;
                Xmax = (int) touchX;
                Ymax = (int) touchY;
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                Xmin = (int) Math.min(Xmin, touchX);
                Ymin = (int) Math.min(Ymin, touchY);
                Xmax = (int) Math.max(Xmax, touchX);
                Ymax = (int) Math.max(Ymax, touchY);
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                Xmin = (int) Math.min(Xmin, touchX);
                Ymin = (int) Math.min(Ymin, touchY);
                Xmax = (int) Math.max(Xmax, touchX);
                Ymax = (int) Math.max(Ymax, touchY);
                drawPath.lineTo(touchX, touchY);
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                lv.addImage(canvasBitmap);
                PartMatching();
                break;
            default:
                return false;
        }
        //redraw
        invalidate();
        return true;
    }

    private void PartMatching() {
        Bitmap bmp = getResizedBitmap(canvasBitmap, 300, 300);
        Mat image = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8U);
        Utils.bitmapToMat(bmp, image);
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        new matchingTask().execute(image.getNativeObjAddr(), (int)(Xmin/(720.0/300.0)), (int)(Ymin/(720.0/300.0)), (int)(Xmax/(720.0/300.0)), (int)(Ymax/(720.0/300.0)));
    }

    void clear(){
        canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        drawCanvas.drawColor(0xffffffff);
        lv.addImage(canvasBitmap);
        score = null;
        postInvalidate();
    }

    void setBitmap(Bitmap bitmap){
        Bitmap tmp = Bitmap.createBitmap(bitmap);
        canvasBitmap = tmp.copy(Bitmap.Config.ARGB_8888, true);
        drawCanvas = new Canvas(canvasBitmap);
        //drawCanvas.drawColor(0xffffffff);
        postInvalidate();
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

    public void save(){
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/PhysicsSketchpad";
       // Log.d("path", file_path);
        File dir = new File(file_path);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dir, "sketchpad.png");
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
            Bitmap rBitmap = getResizedBitmap(canvasBitmap, 300, 300);
            rBitmap.compress(Bitmap.CompressFormat.PNG, 80, fOut);
            fOut.flush();
            fOut.close();
            Toast.makeText(context, "Save success", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSuggestionViews(View im1, View im2, View im3, ArrayList<ArrayList<Bitmap>> suggestList){
        im = new CustomImageView[3];
        im[0] = (CustomImageView) im1;
        im[1] = (CustomImageView) im2;
        im[2] = (CustomImageView) im3;
        this.suggestList = suggestList;
    }

    public Bitmap getCanvasBitmap(){
        return canvasBitmap;
    }
}
