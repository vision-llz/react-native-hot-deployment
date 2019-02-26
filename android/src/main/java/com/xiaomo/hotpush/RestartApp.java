package com.xiaomo.hotpush;

import android.content.Context;
import android.content.Intent;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

/**
 * Created by xiaomo on 2019/1/11   永无bug
 */

public class RestartApp extends ReactContextBaseJavaModule {

    public RestartApp(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    /**
     * 第二代产品
     * 重启整个APP
     * @param context
     * @param Delayed 延迟多少毫秒
     */
    public static void restartAPP(Context context, long Delayed){

        /**开启一个新的服务，用来重启本APP*/
        Intent intent1=new Intent(context,KillService.class);
        intent1.putExtra("PackageName",context.getPackageName());
        intent1.putExtra("Delayed",Delayed);
        context.startService(intent1);

        /**杀死整个进程**/
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    /***重启整个APP*/
    public static void restartAPP(Context context) {
        restartAPP(context, 2000);
    }

    @ReactMethod
    public void Restart() {
//        loadBundle();
        restartAPP(getReactApplicationContext());
    }

    @Override
    public String getName() {
        return "RestartApp";
    }
}
