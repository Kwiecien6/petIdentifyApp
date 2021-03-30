package com.jyq.petidentifyapp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jyq.petidentifyapp.R;
import com.jyq.petidentifyapp.db.DatabaseHelper;
import com.jyq.petidentifyapp.db.PetInfo;
import com.jyq.petidentifyapp.util.FlashlightUtils;
import com.jyq.petidentifyapp.util.PetMatcher;
import com.jyq.petidentifyapp.util.ToastUtil;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
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
import java.util.List;

public class DetectActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener {

    private static final String TAG = "DetectActivity";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    public final static int FLAG_REGISTER = 1;
    public final static int FLAG_VERIFY = 2;
    public final static int FLAG_PHOTO_REGISTER = 3;
    public final static int FLAG_PHOTO_VERIFY = 4;
    int FLAG_DETECT_START = 0;
    int FLAG_FLASH_ON = 0;
    Mat grayscaleImage;
    int absoluteObjSize;
    private CascadeClassifier mCascadeClassifierDog;
    private CascadeClassifier mCascadeClassifierCat;
    private CameraBridgeViewBase mOpenCvCameraView;
    private ImageView mdetectImageView;
    private Button mDetectStartBtn;
    private Button mDetectPhotoBtn;
    private Button mDetectCheckBtn;
    private Button mDetectBackBtn;
    private Button flashBtn;
    List<PetInfo> petList;
    private Bitmap mDetectedPetFace;
    private Bitmap mDetectPhotoPetFace;
    private String detectPhotoImagePath;
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
                            int result = matcher.matchImageForVideo(mDetectedPetFace);
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
                            int result = matcher.matchImageForVideo(mDetectedPetFace);
                            if (result == matcher.UNFINISHED) {
                                ToastUtil.showToast(getApplicationContext(), "宠物验证中", 0);
                                mDetectedPetFace = null;
                            } else if (result == matcher.NO_MATCHER) {
                                ToastUtil.showToast(getApplicationContext(), "宠物未注册，请前往注册", 0);
                            } else {
                                ToastUtil.showToast(getApplicationContext(), "宠物身份验证成功:" + petList.get(result).getPetName(), 0);
                                //跳转宠物信息界面
                                intent = new Intent(DetectActivity.this, MatcherActivity.class);
                                intent.putExtra("matcherPet", petList.get(result));
                                intent.putExtra("petFace2Match", mDetectedPetFace);
                                startActivity(intent);
                                DetectActivity.this.finish();
                            }
                        }
                    }
                    break;
                case FLAG_PHOTO_REGISTER:
                    if (mDetectedPetFace == null) {
                        mDetectedPetFace = (Bitmap) msg.obj;
                        ToastUtil.showToast(getApplicationContext(), "检测到宠物，开始识别", 0);
                        if (petList.size() == 0) {
                            intent = new Intent(DetectActivity.this, RegisterActivity.class);
                            intent.putExtra("PetFace", mDetectedPetFace);
                            startActivity(intent);
                            DetectActivity.this.finish();
                        }else {
                            int result = matcher.matchImageForPhoto(mDetectedPetFace);
                            if(result == matcher.NO_MATCHER){
                                ToastUtil.showToast(getApplicationContext(), "宠物识别成功，开始注册", 0);
                                intent = new Intent(DetectActivity.this, RegisterActivity.class);
                                intent.putExtra("PetFace", mDetectedPetFace);
                                startActivity(intent);
                                DetectActivity.this.finish();
                            }else {
                                ToastUtil.showToast(getApplicationContext(), "宠物已经注册过啦", 0);
                            }
                        }
                    }
                    break;
                case FLAG_PHOTO_VERIFY:
                    if (mDetectedPetFace == null) {
                        if (petList.size() == 0) {
                            ToastUtil.showToast(getApplicationContext(), "宠物未注册，请前往注册", 0);
                        }else {
                            mDetectedPetFace = (Bitmap) msg.obj;
                            int result = matcher.matchImageForPhoto(mDetectedPetFace);
                            if(result == matcher.NO_MATCHER){
                                ToastUtil.showToast(getApplicationContext(), "宠物未注册，请前往注册", 0);
                            }else{
                                ToastUtil.showToast(getApplicationContext(), "宠物身份验证成功:" + petList.get(result).getPetName(), 0);
                                //跳转宠物信息界面
                                intent = new Intent(DetectActivity.this, MatcherActivity.class);
                                intent.putExtra("matcherPet", petList.get(result));
                                intent.putExtra("petFace2Match", mDetectedPetFace);
                                startActivity(intent);
                                DetectActivity.this.finish();
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
                        //导入猫模型文件
                        InputStream isCat = getResources().openRawResource(R.raw.haarcascade_cat);
                        File cascadeDirCat = getDir("cascade", Context.MODE_PRIVATE);
                        File mCascadeFileCat = new File(cascadeDirCat, "haarcascade_cat.xml");
                        FileOutputStream osCat = new FileOutputStream(mCascadeFileCat);

                        byte[] bufferCat = new byte[1024000];
                        int bytesReadCat;
                        while ((bytesReadCat = isCat.read(bufferCat)) != -1) {
                            osCat.write(bufferCat, 0, bytesReadCat);
                        }
                        isCat.close();
                        osCat.close();

                        //导入狗模型文件
                        InputStream isDog = getResources().openRawResource(R.raw.haarcascade_dog2);
                        File cascadeDirDog = getDir("cascade", Context.MODE_PRIVATE);
                        File mCascadeFileDog = new File(cascadeDirDog, "haarcascade_dog2.xml");
                        FileOutputStream osDog = new FileOutputStream(mCascadeFileDog);

                        byte[] bufferDog = new byte[1024000];
                        int bytesReadDog;
                        while ((bytesReadDog = isDog.read(bufferDog)) != -1) {
                            osDog.write(bufferDog, 0, bytesReadDog);
                        }
                        isDog.close();
                        osDog.close();

                        mCascadeClassifierDog = new CascadeClassifier(mCascadeFileDog.getAbsolutePath());
                        mCascadeClassifierCat = new CascadeClassifier(mCascadeFileCat.getAbsolutePath());

                        if (mCascadeClassifierCat.empty()) {
                            Log.e("", "Failed to load cascade classifier");
                            mCascadeClassifierCat = null;
                        } else
                            Log.e("", "Loaded cascade classifier from " + mCascadeFileCat.getAbsolutePath());

                        if (mCascadeClassifierDog.empty()) {
                            Log.e("", "Failed to load cascade classifier");
                            mCascadeClassifierDog = null;
                        } else
                            Log.e("", "Loaded cascade classifier from " + mCascadeFileDog.getAbsolutePath());

                        cascadeDirCat.delete();
                        cascadeDirDog.delete();
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

        //判断已经获取权限后打开相机
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.DetectCameraView);
        mOpenCvCameraView.setCvCameraViewListener(this);

        //开始检测按钮监听
        mDetectStartBtn = findViewById(R.id.detectStartBtn);
        mDetectStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FLAG_DETECT_START == 0) {
                    FLAG_DETECT_START = 1;
                    mDetectStartBtn.setBackgroundResource(R.drawable.ic_baseline_radio_button_unchecked_24);
                    mDetectedPetFace = null;
                    matcher.setCounter(0);
                    ToastUtil.showToast(getApplicationContext(), "开始检测", 0);
                } else {
                    FLAG_DETECT_START = 0;
                    mDetectStartBtn.setBackgroundResource(R.drawable.ic_baseline_radio_button_checked_24);
                    mDetectedPetFace = null;
                    matcher.setCounter(0);
                    ToastUtil.showToast(getApplicationContext(), "暂停检测", 0);
                }
            }
        });

        //闪光灯控件监听
        //闪光灯存在兼容性等问题,暂时无法使用
        flashBtn = findViewById(R.id.detectFlashBtn);
        FlashlightUtils.init(getApplicationContext());
        flashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FLAG_FLASH_ON == 0) {
                    flashBtn.setBackgroundResource(R.drawable.ic_baseline_flash_on_24);
                    FlashlightUtils.linghtOn();
                    FLAG_FLASH_ON = 1;
                } else {
                    flashBtn.setBackgroundResource(R.drawable.ic_baseline_flash_off_24);
                    FlashlightUtils.linghtOff();
                    FLAG_FLASH_ON = 0;
                }
            }
        });


        //相册按钮监听
        mDetectPhotoBtn = findViewById(R.id.detectPhotoBtn);
        mDetectCheckBtn = findViewById(R.id.detectCheckBtn);
        mdetectImageView = findViewById(R.id.detectImageView);
        mDetectPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //停止摄像头实时监测
                FLAG_DETECT_START = 0;
                mDetectStartBtn.setBackgroundResource(R.drawable.ic_baseline_radio_button_checked_24);
                mDetectedPetFace = null;
                matcher.setCounter(0);
                mOpenCvCameraView.setVisibility(View.INVISIBLE);
                mDetectStartBtn.setVisibility(View.INVISIBLE);

                //读取相册图片
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);

            }
        });

        //相册图片上传识别按钮监听
        mDetectCheckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDetectedPetFace = null;
                Message message = Message.obtain();
                message.what = getIntent().getIntExtra("flag", 0) + 2;
                message.obj = mDetectPhotoPetFace;
                mHandler.sendMessage(message);
            }
        });

        //返回按钮监听
        mDetectBackBtn = findViewById(R.id.detectBackBtn);
        mDetectBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //数据导入
        DatabaseHelper helper = new DatabaseHelper(DetectActivity.this);
        petList = helper.query();
        matcher = new PetMatcher(petList);
        helper.close();

        ToastUtil.showToast(getApplicationContext(), "请横屏使用", 0);

    }

    //相册Intent回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //获取图片路径
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            detectPhotoImagePath = c.getString(columnIndex);
            c.close();

            //检测并框选图片中的宠物
            Bitmap detectPhotoBitmap = BitmapFactory.decodeFile(detectPhotoImagePath);
            Mat detectPhotoMat = new Mat();
            Utils.bitmapToMat(detectPhotoBitmap, detectPhotoMat);
            Imgproc.cvtColor(detectPhotoMat, detectPhotoMat, Imgproc.COLOR_RGBA2RGB);

            MatOfRect identifyObj = new MatOfRect();

            if (mCascadeClassifierCat != null) {
                mCascadeClassifierCat.detectMultiScale(detectPhotoMat, identifyObj, 1.1, 2, 2,
                        new Size(absoluteObjSize, absoluteObjSize), new Size());
            }

//            if (mCascadeClassifierDog != null) {
//                mCascadeClassifierDog.detectMultiScale(detectPhotoMat, identifyObj, 1.1, 2, 2,
//                        new Size(absoluteObjSize, absoluteObjSize), new Size());
//            }

            Rect[] facesArray = identifyObj.toArray();
            if (facesArray.length > 0) {
                mDetectCheckBtn.setVisibility(View.VISIBLE);
                Imgproc.rectangle(detectPhotoMat, facesArray[0].tl(), facesArray[0].br(), new Scalar(0, 255, 0, 255), 3);
                Utils.matToBitmap(detectPhotoMat, detectPhotoBitmap);

                Mat faceMat = new Mat(detectPhotoMat, facesArray[0]);
                Imgproc.resize(faceMat, faceMat, new Size(200, 200));
                mDetectPhotoPetFace = Bitmap.createBitmap(faceMat.width(), faceMat.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(faceMat, mDetectPhotoPetFace);
            } else {
                mDetectCheckBtn.setVisibility(View.INVISIBLE);
                ToastUtil.showToast(getApplicationContext(), "未检测到图片中存在宠物", 0);
            }
            mdetectImageView.setImageBitmap(detectPhotoBitmap);
        }
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

    @Override
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

            if (mCascadeClassifierCat != null) {
                mCascadeClassifierCat.detectMultiScale(grayscaleImage, identifyObj, 1.1, 2, 2,
                        new Size(absoluteObjSize, absoluteObjSize), new Size());
            }

//            if (mCascadeClassifierDog != null) {
//                mCascadeClassifierDog.detectMultiScale(grayscaleImage, identifyObj, 1.1, 2, 2,
//                        new Size(absoluteObjSize, absoluteObjSize), new Size());
//            }

            Rect[] facesArray = identifyObj.toArray();
            for (int i = 0; i < facesArray.length; i++) {
                Imgproc.rectangle(inputFrame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);

                // 获取并利用message传递当前检测到的脸
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

