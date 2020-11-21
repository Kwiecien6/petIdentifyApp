package com.jyq.petidentifyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity {

    private static final String CV_TAG = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iniLoadOpenCV();
        Button gray = findViewById(R.id.button);
        gray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convert2Gray();
            }
        });
    }


    private void iniLoadOpenCV(){
        boolean success = OpenCVLoader.initDebug();
        if(success){
            Toast.makeText(this.getApplicationContext(),
                    "OpenCV Libraries loaded succeed!",
                    Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this.getApplicationContext(),
                    "WARNING:Could not load OpenCV Libraries!",
                    Toast.LENGTH_SHORT).show();
        }
    }


    //调节图像灰度（功能测试）
    private void convert2Gray(){
        Bitmap image = BitmapFactory.decodeResource(getResources(),R.drawable.samplepic);
        Mat src = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(image,src);//把image转化为Mat
        Imgproc.cvtColor(src,dst,Imgproc.COLOR_BGRA2GRAY);//这里由于使用的是Imgproc这个模块所有这里要这么写
        Utils.matToBitmap(dst,image);//把mat转化为bitmap
        ImageView imageView = findViewById(R.id.imgView);
        imageView.setImageBitmap(image);
        //release
        src.release();
        dst.release();
    }

}