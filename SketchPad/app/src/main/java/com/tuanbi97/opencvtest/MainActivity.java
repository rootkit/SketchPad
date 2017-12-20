package com.tuanbi97.opencvtest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ImageListView lv;
    private SketchView sv;
    private ArrayList<ArrayList<Bitmap>> suggestList;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("Load OpenCV", "SUCCESS");
                    /*Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.bird);
                    Mat image = new Mat(bmp.getHeight()/2, bmp.getWidth()/2, CvType.CV_8U);
                    Utils.bitmapToMat(bmp, image);
                    Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
                    String s = getdim(image.getNativeObjAddr());
                    TextView tw = (TextView) findViewById(R.id.sizeimage);
                    tw.setText(s);

                    Imgproc.resize(image, image, new Size(300, 300));
                    Bitmap bmp2 = Bitmap.createBitmap(image.rows(), image.cols(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(image, bmp2);
                    ImageView iv = (ImageView) findViewById(R.id.image);
                    iv.setImageBitmap(bmp2);*/

                    //main test
                    loadDataset();
                    /*BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inScaled = false;
                    Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.test9, options);
                    Mat image = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8U);
                    Utils.bitmapToMat(bmp, image);
                    Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
                    double[] score = findMatch(image.getNativeObjAddr(), 60, 0.75, 32, 32, 4, 6, 18, 4);
                    for (int i = 0; i < score.length; i++){
                        Log.i("score final:", Double.toString(score[i]));
                    }*/

                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        if (OpenCVLoader.initDebug()){
            Log.i("Load OpenCV", "SUCCESS");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            Log.i("Load OpenCV", "FAILED");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_13, this, mLoaderCallback);
        }
    }
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        lv = (ImageListView) findViewById(R.id.tmpslot);
        lv.setOnItemChangedListener(new ImageListView.OnItemChanged() {
            @Override
            public void onItemChanged(View v, Bitmap bmp, int selectedId) {
                sv.setBitmap(bmp);
            }
        });
        sv = (SketchView) findViewById(R.id.sketchview);
        sv.lv = lv;
        suggestList = new ArrayList<>(3);
        suggestList.add(new ArrayList<Bitmap>(0));
        suggestList.add(new ArrayList<Bitmap>(0));
        suggestList.add(new ArrayList<Bitmap>(0));
        suggestList.get(0).add(BitmapFactory.decodeResource(getResources(), R.drawable.house1));
        suggestList.get(0).add(BitmapFactory.decodeResource(getResources(), R.drawable.house2));
        suggestList.get(0).add(BitmapFactory.decodeResource(getResources(), R.drawable.house3));
        suggestList.get(1).add(BitmapFactory.decodeResource(getResources(), R.drawable.sun1));
        suggestList.get(1).add(BitmapFactory.decodeResource(getResources(), R.drawable.sun2));
        suggestList.get(1).add(BitmapFactory.decodeResource(getResources(), R.drawable.sun3));
        suggestList.get(2).add(BitmapFactory.decodeResource(getResources(), R.drawable.flower1));
        suggestList.get(2).add(BitmapFactory.decodeResource(getResources(), R.drawable.flower2));
        suggestList.get(2).add(BitmapFactory.decodeResource(getResources(), R.drawable.flower3));
        sv.setSuggestionViews(findViewById(R.id.im1), findViewById(R.id.im2), findViewById(R.id.im3), suggestList);
        lv.post(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
    }

    void init(){

        ArrayList<Bitmap> bmps = new ArrayList<Bitmap>(0);
        bmps.add(BitmapFactory.decodeResource(getResources(), R.drawable.emptyimage));
        lv.setImage(bmps, 5, 1);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native String computeBiCE(long matAddr);
    public native void loadDataset();
    public native double[] findMatch(long matAddress, int patch_size, double overlap, int bx, int by, int b0, int nx, int ny, int n0);

    public void saveCanvas(View view) {
        SketchView sv = (SketchView) findViewById(R.id.sketchview);
        sv.save();
    }

    public void clearCanvas(View view) {
        SketchView sv = (SketchView) findViewById(R.id.sketchview);
        sv.clear();
    }

    public void findMatch(View view) {
        SketchView sv = (SketchView) findViewById(R.id.sketchview);
        Bitmap bmp = sv.getCanvasBitmap();
        Log.i("sizecanvas", Integer.toString(bmp.getWidth()) + " " + Integer.toString(bmp.getHeight()));
        bmp = sv.getResizedBitmap(bmp, 300, 300);

        Mat image = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8U);
        Utils.bitmapToMat(bmp, image);
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        ProgressDialog pd = new ProgressDialog(this);
        pd.show();
        double[] score = findMatch(image.getNativeObjAddr(), 60, 0.75, 32, 32, 4, 6, 18, 4);
        pd.dismiss();
        for (int i = 0; i < score.length; i++){
            Log.i("scorefinal:", Double.toString(score[i]));
        }
    }

    public void imOnClick(View view) {
        CustomImageView v = (CustomImageView) view;
        Bitmap bm = v.getSrc();
        lv.addImage(bm);
        sv.setBitmap(bm);
    }

    public void NextStepClick(View view) {
        Storage.images = lv.images;
        Intent intent = new Intent(MainActivity.this, MixActivity.class);
        startActivity(intent);
    }
    //public native String getdim(long matAddr);
}
