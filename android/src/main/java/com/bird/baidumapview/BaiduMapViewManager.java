package com.bird.baidumapview;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.clusterutil.clustering.ClusterItem;
import com.baidu.mapapi.clusterutil.clustering.ClusterManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
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

import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
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

import java.util.ArrayList;
import java.util.List;


public class BaiduMapViewManager extends SimpleViewManager<MapView> {
    public static final String RCT_CLASS = "RCTBaiduMap";
    public static final String TAG = "RCTBaiduMap";

    protected MapView mapView;
    protected Activity mActivity;
    protected ThemedReactContext context;
    private boolean _lineEnabled = false;
    private ArrayList<Marker> _markerList;
    private MarkerGenerator markerGenerator = new MarkerGenerator();
    @Override
    public LayoutShadowNode createShadowNodeInstance() {
        return new BaiduMapShadowNode();
    }

    @Override
    public Class getShadowNodeClass() {
        return BaiduMapShadowNode.class;
    }

    public BaiduMapViewManager(Activity activity) {
        Log.e(TAG, "BaiduMapViewManager activity:" + activity);
        mActivity = activity;
    }

    @Override
    public String getName() {
        return RCT_CLASS;
    }

    @Override
    protected MapView createViewInstance(ThemedReactContext reactContext) {
        this.context = reactContext;
        MapView mapView = new MapView(mActivity);

        mClusterManager = new ClusterManager<MyItem>(mActivity, mapView.getMap());

        //mapView.getMap().setOnMapStatusChangeListener(mClusterManager);
        //mapView.getMap().setOnMarkerClickListener(mClusterManager);
        addChangeListener(mapView);
        addMarkClickListener(mapView);
        this.mapView = mapView;
        Log.e(TAG, "createViewInstance:" + mapView);
        return mapView;

        //return new MapView(reactContext);
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
        Log.e(TAG, "mode:" + type);
        mapView.getMap().setMapType(type);
    }

    @ReactProp(name="zoom", defaultInt = 18)
    public void setZoom(MapView mapView, int type){
        Log.e(TAG, "setZoom:" + type);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(type);
        mapView.getMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }
    /**
     * 实时交通图
     *
     * @param mapView
     * @param isEnabled
     */
    @ReactProp(name="trafficEnabled", defaultBoolean = false)
    public void setTrafficEnabled(MapView mapView, boolean isEnabled) {
        Log.e(TAG, "trafficEnabled:" + isEnabled);
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
        Log.e(TAG, "heatMapEnabled" + isEnabled);
        mapView.getMap().setBaiduHeatMapEnabled(isEnabled);
    }


    private ClusterManager<MyItem> mClusterManager;
    /**
     * 每个Marker点，包含Marker点坐标以及图标
     */
    public class MyItem implements ClusterItem {
        private final LatLng mPosition;
        private int mIndex;
        public MyItem(LatLng latLng,int index) {
            mPosition = latLng;
            mIndex = index;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public BitmapDescriptor getBitmapDescriptor() {
            return  markerGenerator.getIcon(mIndex);
            //return BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
        }
    }
    /**
     * 显示地理标记
     *
     * @param mapView
     * @param array
     */
    @ReactProp(name="marker")
    public void setMarker(MapView mapView, ReadableArray array) {

        List<MyItem> items = new ArrayList<MyItem>();

        Log.e(TAG, "marker:" + array);
        if (array != null) {
            //mapView.getMap().clear();
            if(_markerList == null){
                _markerList = new ArrayList();
            }else{
                _markerList.clear();
            }
            for (int i = 0; i < array.size(); i++) {
                ReadableArray sub = array.getArray(i);
                //定义Maker坐标点
                LatLng point = new LatLng(sub.getDouble(0), sub.getDouble(1));

                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions()
                        .position(point)
                        .icon(markerGenerator.getIcon(i));

                //在地图上添加Marker，并显示
                //_markerList.add((Marker)mapView.getMap().addOverlay(option));


                items.add(new MyItem(point,i));

            }
            mClusterManager.clearItems();
            mClusterManager.addItems(items);
            mClusterManager.cluster();

        }
    }

    /**
     *
     *--removed
     * @param mapView
     * @param isEnabled
     */
    @ReactProp(name="markerlinesEnabled", defaultBoolean = false)
    public void markerlinesEnabled(MapView mapView, boolean isEnabled) {
        Log.e(TAG, "drawLines:" + isEnabled);
        /*
        this._lineEnabled = isEnabled;
        if (isEnabled && _markerList != null && _markerList.size()>1) {
            OverlayOptions ooPolyline1 = new PolylineOptions().width(2)
                    .color(0xAA0026FF).points(_markerList);
            mapView.getMap().addOverlay(ooPolyline1);

        }
        */
    }

    /**
     *
     *
     * @param mapView
     * @param array
     */
    @ReactProp(name="polyline")
    public void setPolyline(MapView mapView, ReadableArray array) {
        Log.e(TAG, "polylines:" + array);
        if(array != null && array.size()>1) {
            ArrayList list = new ArrayList();
            for (int i = 0; i < array.size(); i++) {
                ReadableArray sub = array.getArray(i);
                LatLng point = new LatLng(sub.getDouble(0), sub.getDouble(1));
                list.add(point);
            }

            OverlayOptions ooPolyline1 = new PolylineOptions().width(10)
                    .color(0xAA0026FF).points(list);
            mapView.getMap().addOverlay(ooPolyline1);
        }
    }


    private TextView tipsView = null;
    @ReactProp(name="tips")
    public void showTips(final MapView mapView,ReadableArray array){
        Log.e(TAG, "showTips:" + array);
        if(array != null && array.size()>1){
            String text = array.getString(0);
            ReadableArray positon = array.getArray(1);
            LatLng llText = new LatLng(positon.getDouble(0), positon.getDouble(1));
            int yOffset = -50;
            if(array.size()>=3){
                yOffset = (int)(array.getDouble(2));
            }
            if(tipsView == null){
                tipsView = new TextView(mActivity);
                tipsView.setBackgroundColor(0xffffffff);
                tipsView.setPadding(30, 20, 30, 50);
                tipsView.setTextColor(0xff000000);
                tipsView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        mapView.getMap().hideInfoWindow();
                    }
                });
            }
            tipsView.setText(text);
            mapView.getMap().showInfoWindow(new InfoWindow(tipsView, llText, yOffset));
            ((ViewGroup) tipsView.getParent()).removeView(tipsView);
        }
    }

/*
    public boolean onMarkerClick(final Marker marker)
    {
        //获得marker中的数据
        Info info = (Info) marker.getExtraInfo().get("info");

        InfoWindow mInfoWindow;
        //生成一个TextView用户在地图中显示InfoWindow
        TextView location = new TextView(getApplicationContext());
        location.setBackgroundResource(R.drawable.location_tips);
        location.setPadding(30, 20, 30, 50);
        location.setText(info.getName());
        //将marker所在的经纬度的信息转化成屏幕上的坐标
        final LatLng ll = marker.getPosition();
        Point p = mBaiduMap.getProjection().toScreenLocation(ll);
        Log.e(TAG, "--!" + p.x + " , " + p.y);
        p.y -= 47;
        LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);
        //为弹出的InfoWindow添加点击事件
        mInfoWindow = new InfoWindow(location, llInfo,
                new OnInfoWindowClickListener()
                {

                    @Override
                    public void onInfoWindowClick()
                    {
                        //隐藏InfoWindow
                        mBaiduMap.hideInfoWindow();
                    }
                });
        //显示InfoWindow
        mBaiduMap.showInfoWindow(mInfoWindow);
        //设置详细信息布局为可见
        mMarkerInfoLy.setVisibility(View.VISIBLE);
        //根据商家信息为详细信息布局设置信息
        popupInfo(mMarkerInfoLy, info);
        return true;
    }
/*
    /**
     * 显示地理标记
     *
     * @param mapView
     * @param array
     */
    @ReactProp(name="center")
    public void setCenter(MapView mapView, ReadableArray array) {
        Log.e(TAG, "center:" + array);
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
     * @param isEnabled
     */
    @ReactProp(name="locationEnabled",defaultBoolean = false)
    public void setLocationEnabled(MapView mapView, boolean isEnabled){
        Log.e(TAG,"setLocationEnabled: " +isEnabled);
        if(isEnabled){
            mapView.getMap().setMyLocationEnabled(true);
            if(mLocClient == null){
                mLocClient = new LocationClient(mActivity);
                mLocClient.registerLocationListener(myListener);
                LocationClientOption option = new LocationClientOption();
                option.setOpenGps(true); // 打开gps
                option.setCoorType("bd09ll"); // 设置坐标类型
                //option.setScanSpan(0);
                mLocClient.setLocOption(option);              
            }
            myListener.resetFlag();
            if(mLocClient.isStarted()){
                mLocClient.stop();;
            }
            mLocClient.start();         //TODO mLocClient.stop(); when distory
        }else{
            if(mLocClient != null)
                mLocClient.stop();
        }

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
        event.putDouble("zoom", status.zoom);
        event.putArray("bound",boundArray);
        fireEvent(event);
    }

    private WritableMap lastMarkerClickEvent;
    protected void handleMarkerClick(Marker marker) {
        Log.e(TAG, "handleMarkerClick:");
        WritableMap event = Arguments.createMap();
        if("cluster".equalsIgnoreCase(marker.getTitle())){
            //cluster marker will not display tips
            event.putString("type", "clusterClick");
            fireEvent(event);
            return;
        }
        /*for (Marker m: _markerList) {
            if(m != marker){
                m.setAlpha(1);
            }
        }*/
        marker.setAlpha(0.3F);
        //marker.setTitle("clicked");

        WritableArray postion = Arguments.createArray();

        postion.pushDouble(marker.getPosition().latitude);
        postion.pushDouble(marker.getPosition().longitude);

        event.putString("type", "markerClick");
        event.putArray("marker", postion);
        lastMarkerClickEvent = event;
        fireEvent(event);
        //getGeoCoder().reverseGeoCode(new ReverseGeoCodeOption().location(marker.getPosition()));
    }

    private GeoCoder geoCoder;
    protected GeoCoder getGeoCoder() {
        if(geoCoder == null) {
            // 创建地理编码检索实例
            geoCoder = GeoCoder.newInstance();
            //
            OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
                // 反地理编码查询结果回调函数
                @Override
                public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                    if(lastMarkerClickEvent == null)return;
                    if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                        // 没有检测到结果
                        lastMarkerClickEvent.putString("addr","没有检测到地址");
                    } else {
                        Log.e(TAG, "onGetReverseGeoCodeResult:" + result.getAddress());
                        lastMarkerClickEvent.putString("addr",result.getAddress());
                    }
                    fireEvent(lastMarkerClickEvent);
                    lastMarkerClickEvent = null;
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
        return  geoCoder;
        //TODO:
        // 释放地理编码检索实例
        // geoCoder.destroy();
    }


    protected void addChangeListener(MapView mapView){
       mapView.getMap().setOnMapStatusChangeListener(new OnMapStatusChangeListener() {
            public void onMapStatusChangeFinish(MapStatus status) {

                mClusterManager.onMapStatusChangeFinish(status);
                handleStatusChange(status);
            }

            public void onMapStatusChangeStart(MapStatus status) {
                //do nothing
                mClusterManager.onMapStatusChangeStart(status);
            }

            public void onMapStatusChange(MapStatus status) {
                //do nothing

                mClusterManager.onMapStatusChange(status);
            }
        });
    }

    protected void addMarkClickListener(final MapView mapView){
        mapView.getMap().setOnMarkerClickListener(new OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {
                Log.e(TAG,"onMarkCLick marker = "+marker);
                handleMarkerClick(marker);
                return true;
            }
        });

        mapView.getMap().setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapView.getMap().hideInfoWindow();

            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
    }


    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
        boolean isFirstLoc = true; // 是否首次定位
        public void resetFlag(){
            isFirstLoc = true;
        }
        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.e(TAG,"onReceiveLocation-------------------");
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
                Log.e(TAG, "onReceiveLocation first time:" );
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll);//.zoom(18.0f);
                mapView.getMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                handleStatusChange(mapView.getMap().getMapStatus());
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }
}
