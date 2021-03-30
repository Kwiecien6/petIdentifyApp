package com.jyq.petidentifyapp.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.jyq.petidentifyapp.R;
import com.jyq.petidentifyapp.db.DatabaseHelper;
import com.jyq.petidentifyapp.db.PetInfo;
import com.jyq.petidentifyapp.util.LocationUtil;
import com.jyq.petidentifyapp.util.ToastUtil;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.List;

import static com.jyq.petidentifyapp.util.DateUtil.getNowDate;
import static com.jyq.petidentifyapp.util.DateUtil.getStringDateShort;
import static com.jyq.petidentifyapp.util.DateUtil.strToDate;


public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final String FEMALE = "母", MALE = "公";
    private EditText petName;
    private EditText petType;
    private RadioGroup petSex;
    private RadioGroup petSterilization;
    private EditText petBirth;
    private EditText petState;
    private EditText petOwner;
    private EditText petOwnerPhone;
    private EditText petLocation;
    private Button petLocationBtn;
    private ImageView petDailyPic;
    private EditText petInfo;
    private Button register;
    private ImageView imageView;
    private ImageView featuresImageView;
    private PetInfo pet;
    private Bitmap petDailyPicBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //修改状态栏背景与字体颜色
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        imageView = (ImageView) findViewById(R.id.registerImageView);
        featuresImageView = (ImageView) findViewById(R.id.registerFeaturesImageView);
        petName = (EditText) findViewById(R.id.registerPetNameEdText);
        petType = (EditText) findViewById(R.id.registerPetTypeEdText);
        petSex = (RadioGroup) findViewById(R.id.registerPetSex);
        petSterilization = (RadioGroup) findViewById(R.id.registerPetSterilization);
        petBirth = (EditText) findViewById(R.id.registerPetBirthEdText);
        petState = (EditText) findViewById(R.id.registerPetState);
        petOwner = (EditText) findViewById(R.id.registerPetOwner);
        petOwnerPhone = (EditText) findViewById(R.id.registerPetOwnerPhone);
        petLocation = (EditText) findViewById(R.id.registerPetLocation);
        petLocationBtn = (Button) findViewById(R.id.registerPetLocationBtn);
        petDailyPic = (ImageView) findViewById(R.id.registerPetDailyPic);
        petInfo = (EditText) findViewById(R.id.registerPetInfo);
        register = (Button) findViewById(R.id.registerBtn);

        init();

    }

    private void init() {
        pet = new PetInfo();
        Bitmap petFace = getIntent().getParcelableExtra("PetFace");
        imageView.setImageBitmap(petFace);

        //AKAZE特征点检测与绘制
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.AKAZE);
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        Mat petFaceMat = new Mat();
        Utils.bitmapToMat(petFace, petFaceMat);
        Imgproc.cvtColor(petFaceMat, petFaceMat, Imgproc.COLOR_RGBA2GRAY);
        detector.detect(petFaceMat, keyPoints);
        Features2d.drawKeypoints(petFaceMat, keyPoints, petFaceMat);

        Bitmap featuresPetFace = Bitmap.createBitmap(petFaceMat.width(), petFaceMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(petFaceMat, featuresPetFace);
        featuresImageView.setImageBitmap(featuresPetFace);

        //宠物品种
//        petType.setInputType(InputType.TYPE_NULL);
        petType.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String[] list =getResources().getStringArray(R.array.pet_type);
                    showListPopupWindow(list, petType);
                }
            }
        });

        //宠物性别
        petSex.check(R.id.registerMaleRBtn);
        pet.setPetSex(MALE);
        petSex.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(
                            RadioGroup group, int checkedId) {
                        if (checkedId == R.id.registerFemaleRBtn) {
                            pet.setPetSex(FEMALE);
                        } else {
                            pet.setPetSex(MALE);
                        }
                    }
                });

        //是否绝育
        petSterilization.check(R.id.registerSterilizedRBtn);
        pet.setPetSterilization("已绝育");
        petSterilization.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(
                            RadioGroup group, int checkedId) {
                        if (checkedId == R.id.registerNotSterilizedRBtn) {
                            pet.setPetSterilization("未绝育");
                        } else {
                            pet.setPetSterilization("已绝育");
                        }
                    }
                });

        //宠物生日；点击EditText 弹出日期选择器DatePickerDialog
        petBirth.setInputType(InputType.TYPE_NULL);
        petBirth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Calendar c = Calendar.getInstance();
                    new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            petBirth.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                        }
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });

        //宠物状态
        petState.setInputType(InputType.TYPE_NULL);
        petState.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String[] list = {"正常", "走失"};//要填充的数据
                    showListPopupWindow(list, petState);
                }
            }
        });

        //注册地址
        petLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location location = LocationUtil.getMyLocation(RegisterActivity.this);
                if (location == null) {
                    ToastUtil.showToast(getApplicationContext(), "定位失败,请检查GPS是否打开", 0);
                } else {
                    petLocation.setText(LocationUtil.getLocationAddress(location, RegisterActivity.this));
                }
            }
        });


        //宠物照片
        petDailyPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //读取相册图片
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPetInfo();
            }
        });
    }

    public void submitPetInfo() {
        if (!TextUtils.isEmpty(petName.getText())
                && !TextUtils.isEmpty(petBirth.getText())
                && !TextUtils.isEmpty(petState.getText())
                && !TextUtils.isEmpty(petOwner.getText())
                && !TextUtils.isEmpty(petOwnerPhone.getText())
                && !TextUtils.isEmpty(petInfo.getText())
                && !TextUtils.isEmpty(petLocation.getText())
                && !TextUtils.isEmpty(petType.getText())) {

            DatabaseHelper dbHelper = new DatabaseHelper(RegisterActivity.this);

            //判断是否存在相同昵称
            if (dbHelper.isNameExist(petName.getText().toString())) {
                ToastUtil.showToast(RegisterActivity.this, "昵称已存在", 0);
            } else {
                Bitmap bitmap = getIntent().getParcelableExtra("PetFace");

                int num = dbHelper.getLastNumber();
                pet.setPetID(getStringDateShort() + num);
                pet.setPetName(petName.getText().toString());
                pet.setPetType(petType.getText().toString());
                pet.setPetBirth(strToDate(petBirth.getText().toString()));
                pet.setPetState(petState.getText().toString());
                pet.setPetOwner(petOwner.getText().toString());
                pet.setPetOwnerPhone(petOwnerPhone.getText().toString());
                pet.setPetRegistLocation(petLocation.getText().toString());
                pet.setPetHistLocation(petLocation.getText().toString());
                pet.setPetInfo(petInfo.getText().toString());
                pet.setPetRegistTime(getNowDate());
                pet.setPetUpdateTime(getNowDate());

                String petPicPath = dbHelper.saveBitmapToLocal(bitmap, pet.getPetID());
                pet.setPetPicPath(petPicPath);

                if(petDailyPicBitmap == null){
                    petDailyPicBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo2);
                }
                String petDailyPicPath = dbHelper.saveBitmapToLocal(petDailyPicBitmap, pet.getPetID() + "dailypic");
                pet.setPetDailyPicPath(petDailyPicPath);

                Log.d(TAG, "submitUserInfo: " + pet.toString());

                dbHelper.insert(pet);
                dbHelper.close();
                ToastUtil.showToast(RegisterActivity.this, "注册成功", 0);
                finish();
            }

        } else {
            ToastUtil.showToast(RegisterActivity.this, "注册信息不完整，无法注册", 0);
        }
    }

    /**
     * EditText下拉菜单ListPopupWindow
     */
    private void showListPopupWindow(final String[] list, final EditText editText) {
        final ListPopupWindow listPopupWindow;
        listPopupWindow = new ListPopupWindow(RegisterActivity.this);
        listPopupWindow.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, list));//用android内置布局，或设计自己的样式
        listPopupWindow.setAnchorView(editText);//以哪个控件为基准，在该处以logId为基准
        listPopupWindow.setModal(true);

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {//设置项点击监听
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                editText.setText(list[i]);//把选择的选项内容展示在EditText上
                listPopupWindow.dismiss();//如果已经选择了，隐藏起来
            }
        });
        listPopupWindow.show();//把ListPopWindow展示出来
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
            String petDailyPicPath = c.getString(columnIndex);
            c.close();

            petDailyPicBitmap = BitmapFactory.decodeFile(petDailyPicPath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            petDailyPicBitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
            petDailyPicBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()), null, null);
            petDailyPic.setImageBitmap(petDailyPicBitmap);
        }
    }

}