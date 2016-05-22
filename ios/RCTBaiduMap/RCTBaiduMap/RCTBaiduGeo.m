//
//  RCTBaiduGeo.m
//  RCTBaiduMap
//
//  Created by lei feng on 5/20/16.
//  Copyright © 2016 creditease. All rights reserved.
//

#import <BaiduMapAPI_Search/BMKSearchComponent.h>
#import "RCTBaiduGeo.h"

#import "RCTBridge.h"
#import "RCTEventDispatcher.h"


@interface KKGeoRequest : NSObject

@property (nonatomic, copy) RCTResponseSenderBlock successBlock;
@property (nonatomic, copy) RCTResponseSenderBlock errorBlock;
@property (nonatomic, assign) CLLocationCoordinate2D locationPoint;

@end

@implementation KKGeoRequest

@end

@interface RCTBaiduGeo () <BMKGeoCodeSearchDelegate>

@end

@implementation RCTBaiduGeo
{

    BMKGeoCodeSearch *_geocodesearch;
    NSMutableArray<KKGeoRequest *> *_pendingRequests;
    KKGeoRequest *_lastGeoRequest;
}

RCT_EXPORT_MODULE(KKGeoSearcher);

@synthesize bridge = _bridge;

- (void)dealloc
{
    _geocodesearch.delegate = nil;
}

-(dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}



#pragma mark - Public API

RCT_EXPORT_METHOD(reverseGeoCode:(CLLocationCoordinate2D)pt
                  withSuccessCallback:(RCTResponseSenderBlock)successBlock
                  errorCallback:(RCTResponseSenderBlock)errorBlock)
{
    if (!_geocodesearch) {
        _geocodesearch = [BMKGeoCodeSearch new];
        _geocodesearch.delegate = self;
    }
    
    
    BMKReverseGeoCodeOption *reverseGeocodeSearchOption = [[BMKReverseGeoCodeOption alloc]init];
    reverseGeocodeSearchOption.reverseGeoPoint = pt;
    BOOL flag = [_geocodesearch reverseGeoCode:reverseGeocodeSearchOption];
    if(flag)
    {
        NSLog(@"反geo检索发送成功");
    }
    else
    {
        NSLog(@"反geo检索发送失败");
    }
    // Create request
    KKGeoRequest *request = [KKGeoRequest new];
    request.successBlock = successBlock;
    request.errorBlock = errorBlock ?: ^(NSArray *args){};
    request.locationPoint = pt;
    /*
    if (!_pendingRequests) {
        _pendingRequests = [NSMutableArray new];
    }
    [_pendingRequests addObject:request];
    */
    _lastGeoRequest = request;
}


#pragma mark - BMKGeoCodeSearchDelegate
-(void) onGetReverseGeoCodeResult:(BMKGeoCodeSearch *)searcher result:(BMKReverseGeoCodeResult *)result errorCode:(BMKSearchErrorCode)error
{
    NSDictionary<NSString *, id> *resultJson;
    if (error == 0) {
        
        //item.coordinate = result.location;
        //item.title = result.address;
        NSString *addr = result.address;
        
        resultJson = @{
                       @"addr": addr
                       };
        CLLocationCoordinate2D location = result.location;
        //NSLog( @(result.location));
        //NSLog( request.locationPoint);
        // Fire all queued callbacks
        /*
        for (KKGeoRequest *request in _pendingRequests) {
            //request.successBlock(result.address);
            
            if([result location].latitude == [request locationPoint].latitude &&
               [result location].longitude == [request locationPoint].longitude){
                request.successBlock(@[resultJson]);
                [_pendingRequests removeObject:request];
            }
        }
        [_pendingRequests removeAllObjects];
         */
        _lastGeoRequest.successBlock(@[resultJson]);
    }
    
    
}

@end
