package com.jyq.petidentifyapp.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.jyq.petidentifyapp.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *测试文件
 */

public class TestIdentifyActivity extends Activity
        implements CameraBridgeViewBase.CvCameraViewListener{

    private static final String TAG = null;

    Mat grayscaleImage;
    int absoluteObjSize;

    //预览图
    CameraBridgeViewBase mIdentifyJCView;
    //模型文件
    File mCascadeFile;
    //级联分类器
    CascadeClassifier mCascadeClassifier = null;


    // 使用opencv自带的分类器文件初始化
    private BaseLoaderCallback mIdentifyLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    try{
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface_improved);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface_improved.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mCascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mCascadeClassifier.empty()) {
                            Log.e("", "Failed to load cascade classifier");
                            mCascadeClassifier = null;
                        } else
                            Log.e("", "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();
                        mIdentifyJCView.enableView();

                    }catch (IOException e){
                        e.printStackTrace();
                        Log.e("", "Failed to load cascade. Exception thrown: " + e);
                    }
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_identify);

        mIdentifyJCView = findViewById(R.id.identifyJCView);
        mIdentifyJCView.setCvCameraViewListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mIdentifyJCView != null) {
            mIdentifyJCView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG,"OpenCV library not found!");
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mIdentifyLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mIdentifyJCView != null) {
            mIdentifyJCView.disableView();
        }
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        grayscaleImage = new Mat(height, width, CvType.CV_8UC4);
        absoluteObjSize = (int) (height * 0.2);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        Imgproc.cvtColor(inputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);
        MatOfRect identifyObj = new MatOfRect();
        if (mCascadeClassifier != null) {
            mCascadeClassifier.detectMultiScale(grayscaleImage, identifyObj, 1.1, 2, 2,
                    new Size(absoluteObjSize, absoluteObjSize), new Size());
        }
        Rect[] facesArray = identifyObj.toArray();
        for (int i = 0; i <facesArray.length; i++)
            Imgproc.rectangle(inputFrame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
        return inputFrame;
    }

}