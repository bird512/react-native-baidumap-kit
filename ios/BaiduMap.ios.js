/**
 * @providesModule BaiduMap
 * @flow
 */
'use strict';

var NativeBaiduMap = require('NativeModules').BaiduMap;

/**
 * High-level docs for the BaiduMap iOS API can be written here.
 */

var BaiduMap = {
  test: function() {
    NativeBaiduMap.test();
  }
};

module.exports = BaiduMap;
