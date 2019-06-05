package com.xiaomo.download;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.File;

public class DownloadApk extends ReactContextBaseJavaModule {
    public static String description;

    DownloadManager downManager;
    Activity myActivity;
    long downloadid;

    public DownloadApk(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "DownloadApk";
    }

    @ReactMethod
    public void downloading(String url, String description) {
        String root = getReactApplicationContext().getFilesDir().getAbsolutePath();
        File file = new File("/sdcard/Download","hotpush.apk");
        // File file = new File(getReactApplicationContext().getE(Environment.DIRECTORY_DOWNLOADS), "hotpush.apk");
        if (file.exists() && file.isFile()){
            file.delete();
        }
        DownloadApk.description = description;

        myActivity = getCurrentActivity();
        downManager = (DownloadManager)myActivity.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new Request(uri);
        // 设置允许使用的网络类型，这里是移动网络和wifi都可以
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        //设置通知栏标题
        request.setNotificationVisibility(Request.VISIBILITY_VISIBLE);
        request.setMimeType("application/vnd.android.package-archive");
        request.setTitle("下载");

        if (description == null || "".equals(description)) {
            description = "目标apk正在下载";
        }

        request.setDescription(description);
        request.setAllowedOverRoaming(false);
        // 设置文件存放目录
//        request.setDestinationInExternalFilesDir(myActivity, Environment.DIRECTORY_DOWNLOADS, description);
        request.setDestinationInExternalPublicDir("Download", "hotpush.apk");
        downloadid = downManager.enqueue(request);
        SharedPreferences sPreferences = myActivity.getSharedPreferences("ggfw_download", 0);
        sPreferences.edit().putLong("ggfw_download_apk", downloadid).commit();
    }

}