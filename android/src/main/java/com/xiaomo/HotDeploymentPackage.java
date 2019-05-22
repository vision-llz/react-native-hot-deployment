
package com.xiaomo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.facebook.react.bridge.JavaScriptModule;
import com.xiaomo.download.DownloadApk;
import com.xiaomo.hotpush.Download;
import com.xiaomo.hotpush.RestartApp;
import com.xiaomo.hotpush.UpdataVersion;

public class HotDeploymentPackage implements ReactPackage {
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new Download(reactContext));
        modules.add(new DownloadApk(reactContext));
        modules.add(new RestartApp(reactContext));
        modules.add(new UpdataVersion(reactContext));
        return modules;
    }

    // Deprecated from RN 0.47
    public List<Class<? extends JavaScriptModule>> createJSModules() {
      return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
      return Collections.emptyList();
    }
}