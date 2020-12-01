package com.jyq.petidentifyapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.jyq.petidentifyapp.R;
import com.jyq.petidentifyapp.db.*;
import com.jyq.petidentifyapp.util.*;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button registerButton = (Button) findViewById(R.id.register);
        Button verifyButton = (Button) findViewById(R.id.verify);
        Button viewDataButton = (Button) findViewById(R.id.view_data);

        registerButton.setOnClickListener(this);
        viewDataButton.setOnClickListener(this);
        verifyButton.setOnClickListener(this);
        initDatabase();

    }


//     初始化数据库
    private void initDatabase() {
        DatabaseHelper helper = new DatabaseHelper(this);
        if (helper.query().size() == 0) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.sample_pet_pic);
            String path = helper.saveBitmapToLocal(bitmap);
            PetInfo pet = new PetInfo("无","无" ,"无", 0, path);
            helper.insert(pet);
        }
        helper.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                Intent registerIntent = new Intent(MainActivity.this,
                        DetectActivity.class);
                registerIntent.putExtra("flag", DetectActivity.FLAG_REGISTER);
                startActivity(registerIntent);
//                requestCameraPermission(new PermissionHelper.RequestListener() {
//                    @Override
//                    public void onGranted() {
//                        Intent intent = new Intent(MainActivity.this,
//                                DetectActivity.class);
//                        intent.putExtra("flag", DetectActivity.FLAG_REGISTER);
//                        startActivityForResult(intent,
//                                DetectActivity.FLAG_REGISTER);
//                    }
//
//                    @Override
//                    public void onDenied() {
//                        ToastUtil.showToast(MainActivity.this, "权限拒绝", 0);
//                    }
//                });
                break;
            case R.id.verify:
                Intent verifyIntent = new Intent(MainActivity.this,
                        DetectActivity.class);
                verifyIntent.putExtra("flag", DetectActivity.FLAG_VERIFY);
                startActivityForResult(verifyIntent,
                        DetectActivity.FLAG_VERIFY);
//                requestCameraPermission(new PermissionHelper.RequestListener() {
//                    @Override
//                    public void onGranted() {
//                        Intent intent = new Intent(MainActivity.this,
//                                DetectActivity.class);
//                        intent.putExtra("flag", DetectActivity.FLAG_VERIFY);
//                        startActivityForResult(intent,
//                                DetectActivity.FLAG_VERIFY);
//                    }
//
//                    @Override
//                    public void onDenied() {
//                        ToastUtil.showToast(MainActivity.this, "权限拒绝", 0);
//                    }
//                });
                break;
            case R.id.view_data:
                startActivity(new Intent(MainActivity.this, ViewDataActivity.class));
                break;
            default:
                break;
        }
    }


}