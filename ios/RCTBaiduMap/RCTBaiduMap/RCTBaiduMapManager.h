//
//  RCTAMapManager.h
//  RCTAMap
//
//  Created by yiyang on 16/2/26.
//  Copyright © 2016年 creditease. All rights reserved.
//

#import "RCTViewManager.h"
#import "BMKClusterManager.h"

@interface RCTBaiduMapManager : RCTViewManager
@property (nonatomic, copy) BMKClusterManager *clusterManager;
@property (nonatomic) NSInteger clusterZoom;
@property (nonatomic) NSMutableArray *clusterCaches;


@end
