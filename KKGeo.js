/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 * @providesModule KKGeo
 * @flow
 */
'use strict';

import React from 'react-native';

var KKGeoSearcher = React.NativeModules.KKGeoSearcher;


type GeoPoint = {
  latitude: number;
  longitude: number;
}


var KKGeo = {

  reverseGeoCode: function(
    geo_success: Function,
    geo_error?: Function,
    geo_point?: GeoPoint
  ) {
    KKGeoSearcher.reverseGeoCode(
      geo_point || {},
      geo_success,
      geo_error || console.error
    );
  },

};

module.exports = KKGeo;
