package com.bird.baidumapview;

import android.content.Context;
import android.util.Log;

import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLngBounds;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

/**
 * Created by fenglei108 on 4/17/16.
 */
public class EventHelper {
    public static final String TAG = "RCTBaiduMap EventHelper";

    public static void handleStatusChange(MapStatus status,ThemedReactContext context) {
        Log.e(TAG, "handleStatusChange:");
        //MapStatus status = mapView.getMap().getMapStatus();
        WritableMap event = Arguments.createMap();

        LatLngBounds bound = status.bound;
        WritableArray boundArray = Arguments.createArray();
        WritableArray northeast = Arguments.createArray();
        WritableArray southwest = Arguments.createArray();
        northeast.pushDouble(bound.northeast.latitude);
        northeast.pushDouble(bound.northeast.longitude);
        southwest.pushDouble(bound.southwest.latitude);
        southwest.pushDouble(bound.southwest.longitude);

        boundArray.pushArray(northeast);
        boundArray.pushArray(southwest);

        event.putString("type", "change");
        event.putDouble("zoom", status.zoom);
        event.putArray("bound",boundArray);
        fireEvent(event,context);
    }

    public static void fireEvent(WritableMap event, ThemedReactContext context){
/*
        context.getJSModule(RCTEventEmitter.class).receiveEvent(
            mapView.getId(),
            //context.getViewTag(),
            "topChange",
            event);
            */
    }

}
