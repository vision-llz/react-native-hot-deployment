package com.xiaomo.download;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.File;

public class DownloadApk extends ReactContextBaseJavaModule {
    public static String description;

    DownloadManager downManager;
    Activity myActivity;
    long downloadid;
  public BroadcastReceiver broadcastReceiver;
  private final QueryRunnable mQueryProgressRunnable = new QueryRunnable();
  WritableMap params;
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
      startQuery();
      listener(downloadid);
    }

  //更新下载进度
  private void startQuery() {
    if (downloadid != 0) {
      handler.post(mQueryProgressRunnable);
    }
  }
  //查询下载进度
  private class QueryRunnable implements Runnable {
    @Override
    public void run() {
      queryState();
      handler.postDelayed(mQueryProgressRunnable,100);
    }
  }

  //停止查询下载进度
  private void stopQuery() {
    handler.removeCallbacks(mQueryProgressRunnable);
  }

  private final Handler handler = new Handler(Looper.getMainLooper()){
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if (msg.what == 1001) {
        params = Arguments.createMap();
        params.putString("status",100*msg.arg1/msg.arg2+"");
        sendEvent(getReactApplicationContext(),"downloadApkStatus",params);
      }
    }
  };

  private void queryState(){
    Cursor cursor=downManager.query(new DownloadManager.Query().setFilterById(downloadid));
    if (cursor == null){
      //下载失败
      params = Arguments.createMap();
      params.putString("status","error");
      sendEvent(getReactApplicationContext(),"downloadApkStatus",params);
    } else {
      if (!cursor.moveToFirst()){
        //下载失败
        params = Arguments.createMap();
        params.putString("status","error");
        sendEvent(getReactApplicationContext(),"downloadApkStatus",params);
        if (!cursor.isClosed()){
          cursor.close();
        }
        return;
      }
      int mDownload_so_far = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
      int mDownload_all = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
      Message message = Message.obtain();
      if(mDownload_all>0) {
        message.what = 1001;
        message.arg1=mDownload_so_far;
        message.arg2=mDownload_all;
        handler.sendMessage(message);
      }
      if(!cursor.isClosed()){
        cursor.close();
      }
    }
  }
  private void listener(final long Id) {
    // 注册广播监听系统的下载完成事件。
    IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
    broadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        long ID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        if (ID == Id) {
          stopQuery();
          File apkFile = new File("/sdcard/Download","hotpush.apk");
          if (apkFile.exists()){
            params = Arguments.createMap();
            params.putString("status","success");
            sendEvent(getReactApplicationContext(),"downloadApkStatus",params);
          }else{
            params.putString("status","error");
            sendEvent(getReactApplicationContext(),"downloadApkStatus",params);
          }
        }
      }
    };
    getReactApplicationContext().registerReceiver(broadcastReceiver, intentFilter);

  }

  private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params){
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName,params);
  }


}
