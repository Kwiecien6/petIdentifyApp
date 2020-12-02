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
//        if (helper.query().size() == 0) {
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
//                    R.drawable.sample_pet_pic);
//            String path = helper.saveBitmapToLocal(bitmap);
//            PetInfo pet = new PetInfo("无","无" ,"无", 0, path);
//            helper.insert(pet);
//        }
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