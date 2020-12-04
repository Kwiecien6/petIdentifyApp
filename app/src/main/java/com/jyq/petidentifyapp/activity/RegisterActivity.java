package com.jyq.petidentifyapp.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.jyq.petidentifyapp.R;
import com.jyq.petidentifyapp.db.DatabaseHelper;
import com.jyq.petidentifyapp.db.PetInfo;
import com.jyq.petidentifyapp.util.ToastUtil;
import com.jyq.petidentifyapp.util.DateUtil;

import java.util.Calendar;

import static com.jyq.petidentifyapp.util.DateUtil.getNowDate;
import static com.jyq.petidentifyapp.util.DateUtil.strToDate;


public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final String FEMALE = "母", MALE = "公";
    private EditText petName;
    private EditText petType;
    private RadioGroup petSex;
    private EditText petBirth;
    private EditText petInfo;
    private Button register;
    private ImageView imageView;
    private PetInfo pet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        imageView = (ImageView) findViewById(R.id.registerImageView);
        petName = (EditText) findViewById(R.id.registerPetNameEdText);
        petType = (EditText) findViewById(R.id.registerPetTypeEdText);
        petSex = (RadioGroup) findViewById(R.id.registerPetSex);
        petBirth = (EditText) findViewById(R.id.registerPetBirthEdText);
        petInfo = (EditText) findViewById(R.id.registerPetInfo);
        register = (Button) findViewById(R.id.registerBtn);

        init();

    }

    private void init() {
        pet = new PetInfo();

        Bitmap petFace = getIntent().getParcelableExtra("PetFace");
        imageView.setImageBitmap(petFace);

        petSex.check(R.id.registerFemaleRBtn);
        pet.setPetSex(FEMALE);
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

//        点击EditText 弹出日期选择器DatePickerDialog
        petBirth.setInputType(InputType.TYPE_NULL);
        petBirth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if(hasFocus){
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
                && !TextUtils.isEmpty(petInfo.getText())
                && !TextUtils.isEmpty(petType.getText())) {

            DatabaseHelper dbHelper = new DatabaseHelper(RegisterActivity.this);

            //判断是否存在相同昵称
            if (dbHelper.isExist(petName.getText().toString())) {
                ToastUtil.showToast(RegisterActivity.this, "昵称已存在", 0);
            } else {
                Bitmap bitmap = getIntent().getParcelableExtra("PetFace");
                String path = dbHelper.saveBitmapToLocal(bitmap);

                pet.setPetName(petName.getText().toString());
                pet.setPetType(petType.getText().toString());
                pet.setPetBirth(strToDate(petBirth.getText().toString()));
                pet.setPetInfo(petInfo.getText().toString());
                pet.setPetRegistTime(getNowDate());
                pet.setPetUpdateTime(getNowDate());
                pet.setPetPicPath(path);

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

}