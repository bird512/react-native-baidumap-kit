var { View, PropTypes, requireNativeComponent } = require('react-native');

var iface = {
  name: 'RCTBaiduMap',
  propTypes: {
    ...View.propTypes,
    mode: PropTypes.number,
    trafficEnabled: PropTypes.bool,
    heatMapEnabled: PropTypes.bool,
    locationEnabled: PropTypes.bool,
    markerlinesEnabled:PropTypes.bool,
    tips:PropTypes.array,
    marker:PropTypes.array,
    center:PropTypes.array,
    onChange:PropTypes.func
  }
}

module.exports = requireNativeComponent('RCTBaiduMap', iface,{
  nativeOnly: {onChange: true},
});
