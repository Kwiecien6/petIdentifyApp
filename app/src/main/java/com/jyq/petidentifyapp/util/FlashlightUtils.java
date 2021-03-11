package com.jyq.petidentifyapp.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.RequiresApi;


public class FlashlightUtils {
    private static Context context;
    private static boolean isOpen = false;
    private static Camera camera;
    private static CameraManager manager;
    private static CountDownTimer timer;

    public static void init(Context context) {
        FlashlightUtils.context = context;
    }

    public static boolean isOpen() {
        return isOpen;
    }

    public static void linghtOn(){
        linghtOn(false);
    }

    public static void linghtOff(){
        linghtOff(false);
    }

    public static void sos(@IntRange(from = 1,to = 6) int speed){
        //先关闭闪光灯
        linghtOff(false);
        if(timer != null)
            timer.cancel();
        timer =getTimer(speed);
        timer.start();
    }

    public static void offSos(){
        linghtOff(false);
    }


    private static void linghtOn(boolean isSos) {
        if (!isSos) {
            if(timer != null)
                timer.cancel();
        }

        if (hasFlashlight()) {
            isOpen = true;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                linghtOn22();
            else
                linghtOn23();
        } else Toast.makeText(context, "该设备没有闪光灯", Toast.LENGTH_SHORT).show();


    }

    private static void linghtOff(boolean isSos) {
        if (!isSos) {
            if(timer != null)
                timer.cancel();
        }

        if (hasFlashlight()) {
            isOpen = false;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                linghtOff22();
            else
                linghtOff23();
        } else Toast.makeText(context, "该设备没有闪光灯", Toast.LENGTH_SHORT).show();


    }

    private static CountDownTimer getTimer(int speed){
        long countDownInterval = (long)(1500/speed);
        return new CountDownTimer(Long.MAX_VALUE,countDownInterval){
            @Override
            public void onTick(long millisUntilFinished) {
                if (isOpen) linghtOff(true); else linghtOn(true);
            }

            @Override
            public void onFinish() {
                start();
            }
        };
    }


    private static void linghtOn22() {
        if (camera == null)
            camera = Camera.open();
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameters);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void linghtOn23() {
        try {
            if (manager == null)
                manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            manager.setTorchMode("0", true);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private static void linghtOff22() {
        if (camera == null)
            camera = Camera.open();
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(parameters);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void linghtOff23() {
        try {
            if (manager == null)
                manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            manager.setTorchMode("0", false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否有闪光灯
     *
     * @return
     */
    public static Boolean hasFlashlight() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

}
