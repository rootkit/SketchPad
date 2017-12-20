package com.tuanbi97.opencvtest;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MixActivity extends AppCompatActivity {

    ImageListView left;
    ImageListView right;
    MixView mxv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_mix);
        left = (ImageListView) findViewById(R.id.setimage);
        right = (ImageListView) findViewById(R.id.setbackground);
        mxv = (MixView) findViewById(R.id.mixview);
        left.post(new Runnable() {
            @Override
            public void run() {
                initleft();
            }
        });
        right.post(new Runnable() {
            @Override
            public void run() {
                initright();
            }
        });
    }

    private void initleft() {
        ArrayList<Bitmap> bmps = new ArrayList<Bitmap>(0);
        for (int i = 1; i < Storage.images.size(); i++){
            bmps.add(Storage.images.get(i));
        }
        left.setImage(bmps, 5, 2);
        left.setOnClickListener(new ImageListView.OnClickListener() {
            @Override
            public void onClickListener(View v, Bitmap bitmap, int selectedId) {
                final Dialog dialog = new Dialog(MixActivity.this);
                dialog.setContentView(R.layout.dialog);
                dialog.setTitle("Settings");
                Button btn = (Button) dialog.findViewById(R.id.buttonok);

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText et = (EditText) dialog.findViewById(R.id.posx);
                        int posx = Integer.parseInt(et.getText().toString());
                        et = (EditText) dialog.findViewById(R.id.posy);
                        int posy = Integer.parseInt(et.getText().toString());
                        SeekBar sb = (SeekBar) dialog.findViewById(R.id.seekbar);
                        float scale = (float) (1.0*sb.getProgress() / 100.0);
                        mxv.addObject(posx, posy, scale, left.images.get(left.selectedId));
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }

    private void initright(){
        ArrayList<Bitmap> bmps = new ArrayList<Bitmap>(0);
        bmps.add(BitmapFactory.decodeResource(getResources(), R.drawable.bg1));
        bmps.add(BitmapFactory.decodeResource(getResources(), R.drawable.bg2));
        bmps.add(BitmapFactory.decodeResource(getResources(), R.drawable.bg3));
        bmps.add(BitmapFactory.decodeResource(getResources(), R.drawable.bg4));
        bmps.add(BitmapFactory.decodeResource(getResources(), R.drawable.bg5));
        bmps.add(BitmapFactory.decodeResource(getResources(), R.drawable.bg6));
        bmps.add(BitmapFactory.decodeResource(getResources(), R.drawable.bg7));
        right.setImage(bmps, 5, 2);
        mxv.addObject(0, 0, 1, bmps.get(0));
        right.setOnClickListener(new ImageListView.OnClickListener() {
            @Override
            public void onClickListener(View v, Bitmap bitmap, int selectedId) {
                mxv.setObject(0, 0, 0, 1, bitmap);
            }
        });
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

    public class BlendTask extends AsyncTask<Void, Void, Bitmap>{

        public native void blend(long bgAddr, long fgAddr, long maskAddr, long retAddr, int posx, int posy);
        ProgressDialog asyncDialog = new ProgressDialog(MixActivity.this);
        @Override
        protected void onPreExecute() {
            asyncDialog.setTitle("Blending...");
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            ArrayList<MixView.proObject> objects = mxv.objects;
            float scale = (float) 0.5;

            Bitmap bg = getResizedBitmap(mxv.objects.get(0).bmp.copy(Bitmap.Config.ARGB_8888, true), scale);
            for (int i = 1; i < objects.size(); i++){
                Bitmap fg = getResizedBitmap(mxv.objects.get(i).bmp.copy(Bitmap.Config.ARGB_8888, true), scale);
                Bitmap mask = Bitmap.createBitmap((int)(mxv.objects.get(i).bmp.getWidth() * scale), (int)(mxv.objects.get(i).bmp.getHeight() * scale), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(mask);
                canvas.drawColor(0xFFFFFFFF);

                Mat mbg = new Mat();
                Mat mfg = new Mat();
                Mat mmask = new Mat();
                Mat ret = new Mat();
                Utils.bitmapToMat(bg, mbg);
                Utils.bitmapToMat(fg, mfg);
                Utils.bitmapToMat(mask, mmask);
                Imgproc.cvtColor(mmask, mmask, Imgproc.COLOR_RGB2GRAY);
                blend(mbg.getNativeObjAddr(), mfg.getNativeObjAddr(), mmask.getNativeObjAddr(), ret.getNativeObjAddr(), (int) (mxv.objects.get(i).posx*scale), (int) (mxv.objects.get(i).posy*scale));
                bg = Bitmap.createBitmap(ret.width(), ret.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(ret, bg);
            }
            return bg;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mxv.objects = new ArrayList<>(0);
            mxv.addObject(0, 0, 2, bitmap);
            asyncDialog.dismiss();
        }
    }

    public void clickPoissonBlend(View view) {
        BlendTask blendTask = new BlendTask();
        blendTask.execute();
    }

    public void clickSave(View view) {

        final Dialog dialog = new Dialog(MixActivity.this);
        dialog.setContentView(R.layout.dialogsave);
        dialog.setTitle("Name");
        Button btn = (Button) dialog.findViewById(R.id.okbutton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = (EditText) dialog.findViewById(R.id.editname);
                String name = et.getText().toString();
                dialog.dismiss();
                String file_path = Environment.getExternalStorageDirectory().toString()+"/SavedSketch";
                // Log.d("path", file_path);
                File dir = new File(file_path);
                if(!dir.exists())
                    dir.mkdirs();
                File file = new File(dir, name + ".png");
                FileOutputStream fOut = null;
                try {
                    fOut = new FileOutputStream(file);
                    Bitmap rBitmap = mxv.objects.get(0).bmp;
                    for (int i = 1; i < mxv.objects.size(); i++){
                        Canvas canvas = new Canvas();
                        canvas.setBitmap(rBitmap);
                        canvas.drawBitmap(mxv.objects.get(i).bmp, mxv.objects.get(i).posx, mxv.objects.get(i).posy, null);
                    }
                    rBitmap.compress(Bitmap.CompressFormat.PNG, 80, fOut);
                    fOut.flush();
                    fOut.close();
                    Toast.makeText(MixActivity.this, "Save success", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        dialog.show();
    }
}
