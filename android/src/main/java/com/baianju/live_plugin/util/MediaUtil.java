package com.baianju.live_plugin.util;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MediaUtil {

    /**
     * 保存jpeg到相册
     */
    public static void saveImageToAlbum(Context context,Bitmap bitmap,String displayName) {
        saveImageToAlbum(context,bitmap,displayName,"image/jpeg",Bitmap.CompressFormat.JPEG);
    }

    /**
     * 保存图片到相册
     */
    public static void saveImageToAlbum(Context context,Bitmap bitmap,String displayName,String mimeType,Bitmap.CompressFormat compressFormat) {
        ContentValues values = createImageContentValues(displayName,mimeType);
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        if(uri != null){
            try {
                OutputStream os = context.getContentResolver().openOutputStream(uri);
                if(os != null) {
                    bitmap.compress(compressFormat,100,os);
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static ContentValues createImageContentValues(String displayName, String mimeType){
        ContentValues values = new ContentValues();
        values.put(MediaStore.DownloadColumns.DISPLAY_NAME,displayName);
        values.put(MediaStore.DownloadColumns.MIME_TYPE,mimeType);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            values.put(MediaStore.DownloadColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
        }else{
            values.put(MediaStore.MediaColumns.DATA, "${Environment.getExternalStorageDirectory().path}/${Environment.DIRECTORY_DCIM}/$displayName");
        }
        return values;
    }

    public static void saveVideoToAlbum(Context context,InputStream inputStream,String displayName) {
        saveMediaToAlbum(context,inputStream,displayName,"video/mp4");
    }

    /**
     * 保存媒体到相册
     */
    public static void saveMediaToAlbum(Context context,InputStream inputStream,String displayName,String mimeType) {
        ContentValues values = createImageContentValues(displayName,mimeType);
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        if(uri != null){
            try {
                OutputStream os = context.getContentResolver().openOutputStream(uri);
                if (os != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        FileUtils.copy(inputStream, os);
                        FileUtils.closeQuietly(inputStream);
                        FileUtils.closeQuietly(os);
                    } else {
                        copy(inputStream,os);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void copy(InputStream inputStream, OutputStream outputStream)
            throws IOException {
        // 数据读取对象封装
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        // 数据写入对象
        BufferedOutputStream bos = new BufferedOutputStream(
                outputStream);

        // 数据读写操作
        byte[] bys = new byte[1024];
        int len = 0;
        while ((len = bis.read(bys)) != -1) {
            bos.write(bys, 0, len);
        }
        // 关闭流
        bos.close();
        bis.close();
    }
}
