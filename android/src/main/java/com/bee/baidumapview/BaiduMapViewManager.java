package com.bee.baidumapview;

import android.app.Activity;
import android.util.Log;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.LayoutShadowNode;
//import com.facebook.react.uimanager.ReactProp;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.bridge.WritableArray;


public class BaiduMapViewManager extends SimpleViewManager<MapView> {
    public static final String RCT_CLASS = "RCTBaiduMap";
    public static final String TAG = "RCTBaiduMap";

    protected MapView mapView;
    protected Activity mActivity;
    protected ThemedReactContext context;

    @Override
    public LayoutShadowNode createShadowNodeInstance() {
        return new BaiduMapShadowNode();
    }

    @Override
    public Class getShadowNodeClass() {
        return BaiduMapShadowNode.class;
    }

    public BaiduMapViewManager(Activity activity) {
        Log.i(TAG, "BaiduMapViewManager activity:" + activity);
        mActivity = activity;
    }

    @Override
    public String getName() {
        return RCT_CLASS;
    }
    /**
     * 地图模式
     *
     * @param mapView
     * @param type
     *  1. 普通
     *  2.卫星
     */
    @ReactProp(name="mode", defaultInt = 1)
    public void setMode(MapView mapView, int type) {
        Log.i(TAG, "mode:" + type);
        mapView.getMap().setMapType(type);
    }

    /**
     * 实时交通图
     *
     * @param mapView
     * @param isEnabled
     */
    @ReactProp(name="trafficEnabled", defaultBoolean = false)
    public void setTrafficEnabled(MapView mapView, boolean isEnabled) {
        Log.d(TAG, "trafficEnabled:" + isEnabled);
        mapView.getMap().setTrafficEnabled(isEnabled);
    }

    /**
     * 实时道路热力图
     *
     * @param mapView
     * @param isEnabled
     */
    @ReactProp(name="heatMapEnabled", defaultBoolean = false)
    public void setHeatMapEnabled(MapView mapView, boolean isEnabled) {
        Log.d(TAG, "heatMapEnabled" + isEnabled);
        mapView.getMap().setBaiduHeatMapEnabled(isEnabled);
    }


    /**
     * 显示地理标记
     *
     * @param mapView
     * @param array
     */
    @ReactProp(name="marker")
    public void setMarker(MapView mapView, ReadableArray array) {
        Log.d(TAG, "marker:" + array);
        if (array != null) {
            mapView.getMap().clear();
            for (int i = 0; i < array.size(); i++) {
                ReadableArray sub = array.getArray(i);
                //定义Maker坐标点
                LatLng point = new LatLng(sub.getDouble(0), sub.getDouble(1));
                //构建Marker图标
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions()
                        .position(point)
                        .icon(bitmap)
                        .draggable(true);
                //在地图上添加Marker，并显示
                mapView.getMap().addOverlay(option);
            }
        }
    }

    /**
     * 显示地理标记
     *
     * @param mapView
     * @param array
     */
    @ReactProp(name="center")
    public void setCenter(MapView mapView, ReadableArray array) {
        Log.d(TAG, "center:" + array);
        if (array != null && array.size()>1) {//

            LatLng cenpt = new LatLng(array.getDouble(0), array.getDouble(1)); 
            //定义地图状态
            //MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(18).build();
            MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).build();
            //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

            MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
            //改变地图状态
            mapView.getMap().setMapStatus(mMapStatusUpdate);
        }
    }

    LocationClient mLocClient ;

    public MyLocationListenner myListener = new MyLocationListenner();
    /**
     * 定位
     *
     * @param mapView
     * @param boolean
     */
    @ReactProp(name="locationEnabled",defaultBoolean = false)
    public void setLocationEnabled(MapView mapView, boolean isEnabled){
        if(isEnabled){
            mapView.getMap().setMyLocationEnabled(true);
            if(mLocClient == null){
                mLocClient = new LocationClient(mActivity);
                mLocClient.registerLocationListener(myListener);
                LocationClientOption option = new LocationClientOption();
                option.setOpenGps(true); // 打开gps
                option.setCoorType("bd09ll"); // 设置坐标类型
                //option.setScanSpan(1000);
                mLocClient.setLocOption(option);              
            }
            mLocClient.start();         //TODO mLocClient.stop(); when distory
        }else{
            if(mLocClient != null)
                mLocClient.stop();
        }

    }

    @Override
    protected MapView createViewInstance(ThemedReactContext reactContext) {
        this.context = reactContext;
        MapView mapView = new MapView(mActivity);
        addChangeListener(mapView);
        addMarkClickListener(mapView);
        this.mapView = mapView;
        return mapView;

        //return new MapView(reactContext);
    }

    private void fireEvent(WritableMap event){
       context.getJSModule(RCTEventEmitter.class).receiveEvent(
          mapView.getId(),
          //context.getViewTag(),
          "topChange",
          event);         
    }

    protected void handleStatusChange(MapStatus status) {
        Log.e(TAG, "handleStatusChange:" );
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
        event.putDouble("zoom",status.zoom);
        event.putArray("bound",boundArray);
        fireEvent(event);
    }

    protected void handleMarkerClick(Marker marker){
        Log.e(TAG, "handleMarkerClick:" );
        WritableMap event = Arguments.createMap();
        WritableArray postion = Arguments.createArray();
       
        postion.pushDouble(marker.getPosition().latitude);
        postion.pushDouble(marker.getPosition().longitude);
        
        event.putString("type", "markerClick");
        event.putArray("postion",postion);
        fireEvent(event);       
    }

    protected void addChangeListener(MapView mapView){
       mapView.getMap().setOnMapStatusChangeListener(new OnMapStatusChangeListener() {
            public void onMapStatusChangeFinish(MapStatus status) {
                handleStatusChange(status);
            }

            public void onMapStatusChangeStart(MapStatus status) {
                //do nothing
            }

            public void onMapStatusChange(MapStatus status) {
                //do nothing
            }
        });
    }

    protected void addMarkClickListener(MapView mapView){
        mapView.getMap().setOnMarkerClickListener(new OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {
                handleMarkerClick(marker);
                return true;
            }
        });      
    }

    boolean isFirstLoc = true; // 是否首次定位
    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mapView.getMap().setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll);//.zoom(18.0f);
                mapView.getMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }
}
