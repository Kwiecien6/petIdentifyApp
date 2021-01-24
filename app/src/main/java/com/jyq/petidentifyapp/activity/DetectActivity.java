package com.jyq.petidentifyapp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jyq.petidentifyapp.R;
import com.jyq.petidentifyapp.db.DatabaseHelper;
import com.jyq.petidentifyapp.db.PetInfo;
import com.jyq.petidentifyapp.util.PetMatcher;
import com.jyq.petidentifyapp.util.ToastUtil;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DetectActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener {

    private static final String TAG = "DetectActivity";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    public final static int FLAG_REGISTER = 1;
    public final static int FLAG_VERIFY = 2;
    int FLAG_DETECT_START = 0;
    Mat grayscaleImage;
    int absoluteObjSize;
    private CascadeClassifier mCascadeClassifier;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Button mDetectStartBtn;
    List<PetInfo> petList;
    private Bitmap mDetectedPetFace;
    private PetMatcher matcher;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Intent intent;
            switch (msg.what) {
                case FLAG_REGISTER:
                    if (mDetectedPetFace == null) {
                        mDetectedPetFace = (Bitmap) msg.obj;
                        ToastUtil.showToast(getApplicationContext(), "检测到宠物，开始识别", 0);
                        if (petList.size() == 0) {
                            intent = new Intent(DetectActivity.this, RegisterActivity.class);
                            intent.putExtra("PetFace", mDetectedPetFace);
                            startActivity(intent);
                            DetectActivity.this.finish();

                        } else {
                            int result = matcher.histogramMatch(mDetectedPetFace);
                            if (result == matcher.UNFINISHED) {
                                ToastUtil.showToast(getApplicationContext(), "宠物识别中", 0);
                                mDetectedPetFace = null;

                            } else if (result == matcher.NO_MATCHER) {
                                ToastUtil.showToast(getApplicationContext(), "宠物识别成功，开始注册", 0);
                                intent = new Intent(DetectActivity.this, RegisterActivity.class);
                                intent.putExtra("PetFace", mDetectedPetFace);
                                startActivity(intent);
                                DetectActivity.this.finish();

                            } else {
                                ToastUtil.showToast(getApplicationContext(), "宠物已经注册过啦", 0);
                                mDetectedPetFace = null;

                            }
                        }
                    }
                    break;
                case FLAG_VERIFY:
                    if (mDetectedPetFace == null) {
                        if (petList.size() == 0) {
                            ToastUtil.showToast(getApplicationContext(), "宠物未注册，请前往注册", 0);
                        } else {
                            mDetectedPetFace = (Bitmap) msg.obj;
                            int result = matcher.histogramMatch(mDetectedPetFace);
                            if (result == matcher.UNFINISHED) {
                                ToastUtil.showToast(getApplicationContext(), "宠物验证中", 0);
                                mDetectedPetFace = null;
                            } else if (result == matcher.NO_MATCHER) {
                                ToastUtil.showToast(getApplicationContext(), "宠物未注册，请前往注册", 0);
                                mDetectedPetFace = null;
                            } else {
                                ToastUtil.showToast(getApplicationContext(), "宠物身份验证成功:"+petList.get(result).getPetName(), 0);
                                //跳转宠物信息界面
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    // 使用分类器xml文件初始化CascadeClassifier
    private BaseLoaderCallback mIdentifyLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    try {
                        InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalcatface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        File mCascadeFile = new File(cascadeDir, "haarcascade_frontalcatface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[1024000];
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
                        mOpenCvCameraView.enableView();

                    } catch (IOException e) {
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
        setContentView(R.layout.activity_detect);

        //申请相机权限
        if (ContextCompat.checkSelfPermission(DetectActivity.this,
                android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            //判断为没有权限，唤起权限申请询问
            ActivityCompat.requestPermissions(DetectActivity.this, new String[]{android.Manifest.permission.CAMERA}, 1);
        }

        //判断已经获取权限后的操作
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.DetectCameraView);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mDetectStartBtn = findViewById(R.id.detectStartBtn);
        mDetectStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FLAG_DETECT_START == 0) {
                    FLAG_DETECT_START = 1;
                    mDetectStartBtn.setBackgroundResource(R.drawable.circle_drawable);
                    ToastUtil.showToast(getApplicationContext(), "开始检测", 0);
                } else {
                    FLAG_DETECT_START = 0;
                    mDetectStartBtn.setBackgroundResource(R.drawable.circle_drawable_unselect);
                    ToastUtil.showToast(getApplicationContext(), "暂停检测", 0);
                }
            }
        });

        DatabaseHelper helper = new DatabaseHelper(DetectActivity.this);
        petList = helper.query();
        matcher = new PetMatcher(petList);
        helper.close();

        ToastUtil.showToast(getApplicationContext(), "请横屏使用", 0);


    }


    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library not found!");
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mIdentifyLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
        mHandler.removeCallbacksAndMessages(null);
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
        if (FLAG_DETECT_START == 1) {
            Imgproc.cvtColor(inputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);
            MatOfRect identifyObj = new MatOfRect();

            if (mCascadeClassifier != null) {
                mCascadeClassifier.detectMultiScale(grayscaleImage, identifyObj, 1.1, 2, 2,
                        new Size(absoluteObjSize, absoluteObjSize), new Size());
            }

            Rect[] facesArray = identifyObj.toArray();
            for (int i = 0; i < facesArray.length; i++) {
                Imgproc.rectangle(inputFrame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);

                // 获取并利用message传递当前检测的人脸
                try {
                    Mat faceMat = new Mat(inputFrame, facesArray[i]);
                    Imgproc.resize(faceMat, faceMat, new Size(200, 200));
                    Bitmap bitmap = Bitmap.createBitmap(faceMat.width(), faceMat.height(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(faceMat, bitmap);
                    Message message = Message.obtain();
                    message.what = getIntent().getIntExtra("flag", 0);
                    message.obj = bitmap;
                    mHandler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return inputFrame;
    }

}