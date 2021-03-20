package com.jyq.petidentifyapp.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
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


    /**
     * 获取当前位置信息
     * @param context
     * @return
     */
    public static Location getMyLocation(Context context) {

        //获取定位服务
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //获取支持的provider列表
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;

        //定位权限检查
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

        //遍历provider列表
        for (String provider : providers) {
            //通过getLastKnowLocation方法来获取
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location
                bestLocation = l;
            }
        }

        return bestLocation;


    }

    public static String getLocationAddress(Location location,Context context) {
        String addressStr = "";
        Geocoder geoCoder = new Geocoder(context);
        try {
            List<Address> addressList = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addressList!=null&&addressList.size()>0){
                Address address = addressList.get(0);
//                addressStr = address.getCountryName()+address.getLocality()+address.getSubLocality()+address.getFeatureName();
//                addressStr = address.getCountryName()+address.getLocality()+address.getSubLocality();
                addressStr = address.getAddressLine(0);
            }

        } catch (IOException e) {
            addressStr = "暂未获取到位置信息";
            e.printStackTrace();
        }
        return addressStr;
    }

}
