package com.bird.baidumapview;

import android.app.Activity;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class BaiduMapReactPackage implements ReactPackage {
    private Activity mActivity;

    public BaiduMapReactPackage() {
        //mActivity = activity;
    }

    public BaiduMapReactPackage(Activity activity) {
        mActivity = activity;
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Arrays.<ViewManager>asList(new BaiduMapViewManager(mActivity));
        //return Arrays.<ViewManager>asList(new BaiduMapViewManager(reactContext));
    }
}
