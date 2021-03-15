package com.jyq.petidentifyapp.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;

import com.jyq.petidentifyapp.activity.DetectActivity;
import com.jyq.petidentifyapp.activity.RegisterActivity;

import java.io.IOException;
import java.util.List;

public class LocationUtil {

    private static String provider;

    public static Location getMyLocation(Context context) {
//        获取当前位置信息
        //获取定位服务
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //获取当前可用的位置控制器
        List<String> list = locationManager.getProviders(true);

        if (list.contains(locationManager.GPS_PROVIDER)) {
//            GPS位置控制器
            provider = locationManager.GPS_PROVIDER;//GPS定位
        } else if (list.contains(locationManager.NETWORK_PROVIDER)) {
//            网络位置控制器
            provider = locationManager.NETWORK_PROVIDER;//网络定位
        }

        if (provider != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            Location lastKnownLocation = locationManager.getLastKnownLocation(provider);

            return lastKnownLocation;


        } else {
            ToastUtil.showToast(context,"请检查网络或GPS是否打开",0);
        }

        return null;
    }

    public static String getLocationAddress(Location location,Context context) {
        String addressStr = "";
        Geocoder geoCoder = new Geocoder(context);
        try {
            List<Address> addressList = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addressList!=null&&addressList.size()>0){
                Address address = addressList.get(0);
                addressStr = address.getCountryName()+address.getLocality()+address.getSubLocality()+address.getFeatureName();
            }

        } catch (IOException e) {
            addressStr = "China";
            e.printStackTrace();
        }
        return addressStr;
    }

}
