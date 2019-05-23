package com.xiaomo.hotpush;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.File;

import static android.content.Context.DOWNLOAD_SERVICE;
public class Download extends ReactContextBaseJavaModule {
    private final QueryRunnable mQueryProgressRunnable = new QueryRunnable();
    long downloadid;
    public static String description;
    public DownloadManager mDownloadManager;
    public BroadcastReceiver broadcastReceiver;
    WritableMap params= Arguments.createMap();
    public Download(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "Download";
    }

    @ReactMethod
    public void downloading(String url) {
        String root = getReactApplicationContext().getFilesDir().getAbsolutePath();
        deleteDir(root);
        File js = new File(root+"/index.android.bundle");
        File zip = new File("/mnt/sdcard"+root+"/bundle.zip");
        if (js.exists() && js.isFile()){
            js.delete();
        }
        if (zip.exists() && zip.isFile()){
            zip.delete();
        }
        Log.d("TAG","开始下载======>");
        mDownloadManager = (DownloadManager)getReactApplicationContext().getSystemService(DOWNLOAD_SERVICE);

        String apkUrl = url;
        Uri resource = Uri.parse(apkUrl);

        DownloadManager.Request request = new DownloadManager.Request(resource);
        //下载的本地路径，表示设置下载地址为SD卡的Download文件夹，文件名为mobileqq_android.apk。
        request.setDestinationInExternalPublicDir(String.valueOf(getReactApplicationContext().getFilesDir().getAbsoluteFile()), "bundle.zip");
//      request.setDestinationInExternalFilesDir(getReactApplicationContext(),"", "bundle.zip");
        //start 一些非必要的设置
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
//        request.setVisibleInDownloadsUi(true);
//        request.setTitle("bundle.zip");
        //end 一些非必要的设置
        downloadid = mDownloadManager.enqueue(request);
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
            handler.postDelayed(mQueryProgressRunnable,500);
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
                params.putString("status",100*msg.arg1/msg.arg2+"");
                sendEvent(getReactApplicationContext(),"downloadZipStatus",params);
            }
        }
    };

    private void queryState(){
        Cursor cursor=mDownloadManager.query(new DownloadManager.Query().setFilterById(downloadid));
        if (cursor == null){
            //下载失败
            params.putString("status","error");
            sendEvent(getReactApplicationContext(),"downloadZipStatus",params);
        } else {
            if (!cursor.moveToFirst()){
                //下载失败
                params.putString("status","error");
                sendEvent(getReactApplicationContext(),"downloadZipStatus",params);
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
                    String root = getReactApplicationContext().getFilesDir().getAbsolutePath();
                    File zip = new File("/mnt/sdcard"+root,"bundle.zip");
                    Log.d("zip",zip.getPath());
                    if (zip.exists()){
                        Log.d("zip",zip.getName());
                        try {
                            ZipFolder.UnZipFolder("/mnt/sdcard"+root+"/bundle.zip",root);
                            zip.delete();
                            params.putString("status","success");
                        } catch (Exception e) {
                            e.printStackTrace();
                            params.putString("status","decompressionError");
                        }
                        sendEvent(getReactApplicationContext(),"downloadZipStatus",params);
                    }else{
                        params.putString("status","error");
                        sendEvent(getReactApplicationContext(),"downloadZipStatus",params);
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

    //删除文件夹和文件夹里面的文件
    public static void deleteDir(final String pPath) {
        File dir = new File(pPath);
        deleteDirWihtFile(dir);
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
//        dir.delete();// 删除目录本身
    }

}