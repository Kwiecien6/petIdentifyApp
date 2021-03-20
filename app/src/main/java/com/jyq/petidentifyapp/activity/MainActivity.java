package com.jyq.petidentifyapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jyq.petidentifyapp.R;
import com.jyq.petidentifyapp.db.*;
import com.jyq.petidentifyapp.util.*;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //修改状态栏背景与字体颜色
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        //申请权限
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
        ||ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED ) {
            //判断为没有权限，唤起权限申请询问
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        }

        //定位测试
        ImageView imageView = (ImageView) findViewById(R.id.img2);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView locationTextView = (TextView) findViewById(R.id.location);
                Location location = LocationUtil.getMyLocation(MainActivity.this);
                if (location == null){
                    ToastUtil.showToast(getApplicationContext(), "定位失败,请检查GPS是否打开", 0);
                }else {
                    locationTextView.setText(LocationUtil.getLocationAddress(location,MainActivity.this));
                }
            }
        });


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
                break;

            case R.id.verify:
                Intent verifyIntent = new Intent(MainActivity.this,
                        DetectActivity.class);
                verifyIntent.putExtra("flag", DetectActivity.FLAG_VERIFY);
                startActivity(verifyIntent);
                break;

            case R.id.view_data:
                startActivity(new Intent(MainActivity.this, ViewDataActivity.class));
                break;

            default:
                break;
        }
    }


}