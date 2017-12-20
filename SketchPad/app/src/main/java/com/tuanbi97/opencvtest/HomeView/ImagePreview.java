package com.tuanbi97.opencvtest.HomeView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * Created by User on 9/17/2017.
 */

public class ImagePreview {

    int left, top, right, bottom;
    Context context;

    public ImagePreview(Context context, int left, int top, int right, int bottom){
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public Bitmap getResizedBitmap(Bitmap bm, float newW, float newH) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = (float) newW/width;
        float scaleHeight = (float) newH/height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public void setImage(Canvas canvas, Bitmap bmp){
        bmp = getResizedBitmap(bmp, right - left, bottom - top);
        canvas.drawBitmap(bmp, left, top, null);
        Paint paint = new Paint();
        paint.setStrokeWidth(20);
        paint.setColor(Color.WHITE);
        paint.setAlpha(150);
        canvas.drawLine(left - 20, top - 20, left - 20 + 200, top - 20, paint);
        canvas.drawLine(left - 20, top - 20, left - 20, top - 20 + 140, paint);
        canvas.drawLine(right + 20, bottom + 20, right + 20 - 200, bottom + 20, paint);
        canvas.drawLine(right + 20, bottom + 20, right + 20, bottom + 20 - 140, paint);
    }

}
