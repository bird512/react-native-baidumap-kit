# react-native-baidumap-kit
Bridget between react native and baidumap SDK

## install

`npm install react-native-baidumap-kit --save`

### iOS

1. Open your project in XCode, right click on `Libraries` and click `Add
   Files to "Your Project Name"` Look under `node_modules/react-native-baidumap` and add `RCTBaiduMap.xcodeproj`.
2. Add `libRCTBaiduMap.a` to `Build Phases -> Link Binary With Libraries.
3. Click on `RCTBaiduMap.xcodeproj` in `Libraries` and go the `Build
   Settings` tab. Double click the text to the right of `Header Search
   Paths` and verify that it has `$(SRCROOT)/../react-native/React` - if they
   aren't, then add them. This is so XCode is able to find the headers that
   the `RCTBaiduMap` source files are referring to by pointing to the
   header files installed within the `react-native` `node_modules`
   directory.
4. Add `node_modules/react-native-baidumap/RCTBaiduMap/RCTBaiduMap/BaiduMap/BaiduMapAPI_*.framework` and `BaiduMapAPI_Map.framework/Resources/mapapi.bundle` to your project.
5. Set your project's framework Search Paths to include `$(PROJECT_DIR)/../node_modules/react-native-baidumap/ios/RCTBaiduMap/RCTBaiduMap/BaiduMap`.
6. Set your project's Header Search paths to include `$(SRCROOT)/../node_modules/react-native-baidumap/ios/RCTBaiduMap/RCTBaiduMap`.
4. Whenever you want to use it within React code now you can: `var MapView =
   require('react-native-baidumap');`


### android

## usage

```
...
import MapView from 'react-native-baidumap';
import KKLocation from 'react-native-baidumap/KKLocation';

...
componentDidMount() {
    this.refs["mapView"].zoomToLocs([[39.918541, 116.4835]]);
    KKLocation.getCurrentPosition((position) => {
        console.log("location get current position: ", position);
    }, (error) => {
        console.log("location get current position error: ", error);
    });
    this.watchID = KKLocation.watchPosition((position) => {
        console.log("watch position: ", position);
    });
}

render() {
  <MapView
    style={{flex: 1, width: 300}}
    ref="mapView"
    showsUserLocation={true}
    userLocationViewParams={{accuracyCircleFillColor: 'red', image: require('./start_icon.png')}}
    annotations={[{latitude: 39.832136, longitude: 116.34095, title: "start", subtile: "hello", image: require('./amap_start.png')}, {latitude: 39.902136, longitude: 116.44095, title: "end", subtile: "hello", image: require('./amap_end.png')}]}
    overlays={[{coordinates: [{latitude: 39.832136, longitude: 116.34095}, {latitude: 39.832136, longitude: 116.42095}, {latitude: 39.902136, longitude: 116.42095}, {latitude: 39.902136, longitude: 116.44095}], strokeColor: '#666666', lineWidth: 3}]}
  />
}
