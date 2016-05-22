'use strict'

import React, {
  Component
} from 'react-native';

import Map from './Map';
export default class BaiduMap extends Component {
  constructor (props) {
    super(props)
    
  }
  render () {
    
    return (
      <Map {...this.props}/>
      
    )
  }
}



