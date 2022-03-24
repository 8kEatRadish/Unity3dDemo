package com.eatradish.unitydemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

class Unity3dUtil {
    static void showToast(Context context, String msg) {
        Log.d("Unity3dUtil", "showToast: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    static private int imageWidth;
    static private int imageHeight;

    // Text转Bitmap
    static private Bitmap generateBitmap(String text, int textColor, int textSize, int alpha) {
        // 设置基本属性
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(textColor == 2 ? 0xff000000 : 0xffffffff);//0xff0000
        textPaint.setTextSize(80);
        textPaint.setAntiAlias(true);//抗锯齿
        textPaint.setAlpha(alpha);//0-255

        // 获取文字尺寸
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textWidth = textPaint.measureText(text);
        float textHeight = fm.descent - fm.ascent;
        imageWidth = (int) Math.ceil(textWidth);
        imageHeight = (int) Math.ceil(textHeight);

        // 生成图片
        Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(textColor == 1 ? 0xff000000 : 0xffffffff);
        canvas.drawText(text, 0f, textHeight - 20, textPaint);

        return bitmap;
    }

    // Text转Bitmap bytes （Unity中调用此接口获取图片数据）
    static byte[] generatePngBytes(String text, int textColor, int textSize, int alpha) {
        Bitmap bitmap = generateBitmap(text, textColor, textSize, alpha);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
