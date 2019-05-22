package com.xiaomo.hotpush;

import android.content.SharedPreferences;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by xiaomo on 2019/5/22   永无bug
 */

public class UpdataVersion extends ReactContextBaseJavaModule {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public UpdataVersion(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "HotPushVersion";
    }

    @ReactMethod
    public void updataVersion(String version){
        sharedPreferences=getReactApplicationContext().getSharedPreferences("appVersion.xml",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("version",version);
        editor.commit();
    }
}
