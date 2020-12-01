package com.jyq.petidentifyapp.util;

import android.content.Context;
import android.widget.Toast;

/**
 * 控制Toast的弹出
 *
 */

public class ToastUtil {
    private static Toast toast;

    public static void showToast(Context context, String content, int duration) {
        if (toast == null) {
            toast = Toast.makeText(context, content, duration);
        } else {
            toast.setText(content);
        }
        toast.show();
    }
}