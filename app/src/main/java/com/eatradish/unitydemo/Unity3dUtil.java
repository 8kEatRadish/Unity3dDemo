package com.eatradish.unitydemo;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

class Unity3dUtil {
    static void showToast(Context context, String msg) {
        Log.d("Unity3dUtil", "showToast: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
