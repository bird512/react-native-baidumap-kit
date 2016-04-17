package com.bird.baidumapview;

import android.util.Log;

import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;

/**
 * Created by fenglei108 on 4/10/16.
 */
public class BaiduMapUtilModule extends ReactContextBaseJavaModule {
    public static final String RCT_CLASS = "RCTBaiduMapUtil";

    public BaiduMapUtilModule(ReactApplicationContext reactContextBaseJavaModule) {
        super(reactContextBaseJavaModule);
    }

    @Override
    public String getName() {
        return RCT_CLASS;
    }

    private GeoCoder geoCoder;
    private ArrayList promiseList;
    @ReactMethod
    public void reverseGeoCode(String userName, String password, final Promise promise){
        if(promiseList == null){
            promiseList = new ArrayList<Promise>();
            promiseList.add(promise);
        }
        if(geoCoder == null) {
            // 创建地理编码检索实例
            geoCoder = GeoCoder.newInstance();
            //
            OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
                // 反地理编码查询结果回调函数
                @Override
                public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                    //Promise promise = promiseList.get(0);
                    if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                        // 没有检测到结果
                        promise.reject("a", "没有检测到地址");
                    } else {
                        Log.e(RCT_CLASS, "onGetReverseGeoCodeResult:" + result.getAddress());
                        //lastMarkerClickEvent.putString("addr", result.getAddress());
                        //promise.resolve();
                    }

                }

                // 地理编码查询结果回调函数
                @Override
                public void onGetGeoCodeResult(GeoCodeResult result) {
                    if (result == null
                            || result.error != SearchResult.ERRORNO.NO_ERROR) {
                        // 没有检测到结果
                    }
                }
            };
            geoCoder.setOnGetGeoCodeResultListener(listener);
        }
        //getGeoCoder().reverseGeoCode(new ReverseGeoCodeOption().location(marker.getPosition()));
    }
    /*
    @ReactMethod
    public reverseGeoCodes(){
        getGeoCoder().reverseGeoCode(new ReverseGeoCodeOption().location(marker.getPosition()));
    }
*/



}
