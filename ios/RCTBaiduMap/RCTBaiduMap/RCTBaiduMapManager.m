//
//  RCTAMapManager.m
//  RCTAMap
//
//  Created by yiyang on 16/2/26.
//  Copyright © 2016年 creditease. All rights reserved.
//

#import "RCTBaiduMapManager.h"

#import "RCTBridge.h"
#import "RCTUIManager.h"
#import "RCTConvert+CoreLocation.h"
#import "RCTConvert+BaiduMapKit.h"
#import "RCTEventDispatcher.h"
#import "RCTBaiduMap.h"
#import "RCTUtils.h"
#import "UIView+React.h"
#import "RCTBaiduMapAnnotation.h"
#import "RCTBaiduMapOverlay.h"

static NSString *const RCTBaiduMapViewKey = @"BaiduMapView";


static NSString *const RCTBaiduMapPinRed = @"#ff3b30";
static NSString *const RCTBaiduMapPinGreen = @"#4cd964";
static NSString *const RCTBaiduMapPinPurple = @"#c969e0";

@implementation RCTConvert (BMKPinAnnotationColor)

RCT_ENUM_CONVERTER(BMKPinAnnotationColor, (@{
                                            RCTBaiduMapPinRed: @(BMKPinAnnotationColorRed),
                                            RCTBaiduMapPinGreen: @(BMKPinAnnotationColorGreen),
                                            RCTBaiduMapPinPurple: @(BMKPinAnnotationColorPurple)
                                            }), BMKPinAnnotationColorRed, unsignedIntegerValue)

@end


@interface RCTBaiduMapAnnotationView : BMKPinAnnotationView

@property (nonatomic, strong) UIView *contentView;

@property (nonatomic, assign) NSInteger size;

@end

@implementation RCTBaiduMapAnnotationView

@synthesize size = _size;


- (void)setContentView:(UIView *)contentView
{
    [_contentView removeFromSuperview];
    _contentView = contentView;
    [self addSubview:_contentView];
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    self.bounds = (CGRect){
        CGPointZero,
        _contentView.frame.size,
    };
}

@end

@interface RCTBaiduMapManager () <BMKMapViewDelegate>


@end

@implementation RCTBaiduMapManager

- (id)init
{
    self = [super init];
    if(self)
    {
        _clusterManager = [[BMKClusterManager alloc] init];
        _clusterCaches = [[NSMutableArray alloc] init];
        for (NSInteger i = 3; i < 22; i++) {
            [_clusterCaches addObject:[NSMutableArray array]];
        }
    }
    return self;
}



RCT_EXPORT_MODULE()

- (UIView *)view
{
    RCTBaiduMap *map = [RCTBaiduMap new];
    map.delegate = self;
    return map;
}

RCT_EXPORT_VIEW_PROPERTY(showsUserLocation, BOOL)
RCT_EXPORT_VIEW_PROPERTY(centerCoordinate, CLLocationCoordinate2D)
RCT_EXPORT_VIEW_PROPERTY(showsPointsOfInterest, BOOL)
RCT_EXPORT_VIEW_PROPERTY(followUserLocation, BOOL)
RCT_EXPORT_VIEW_PROPERTY(autoZoomToSpan, BOOL)
RCT_EXPORT_VIEW_PROPERTY(zoomEnabled, BOOL)
RCT_EXPORT_VIEW_PROPERTY(zoomLevel, float)
RCT_EXPORT_VIEW_PROPERTY(rotateEnabled, BOOL)
RCT_EXPORT_VIEW_PROPERTY(pitchEnabled, BOOL)
RCT_EXPORT_VIEW_PROPERTY(scrollEnabled, BOOL)
RCT_EXPORT_VIEW_PROPERTY(maxDelta, CGFloat)
RCT_EXPORT_VIEW_PROPERTY(minDelta, CGFloat)
RCT_EXPORT_VIEW_PROPERTY(legalLabelInsets, UIEdgeInsets)
RCT_EXPORT_VIEW_PROPERTY(mapType, BMKMapType)
RCT_EXPORT_VIEW_PROPERTY(annotations, NSArray<RCTBaiduMapAnnotation *>)
RCT_EXPORT_VIEW_PROPERTY(overlays, NSArray<RCTBaiduMapOverlay *>)
RCT_EXPORT_VIEW_PROPERTY(onAnnotationDragStateChange, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAnnotationFocus, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAnnotationBlur, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onChange, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onPress, RCTBubblingEventBlock)
RCT_CUSTOM_VIEW_PROPERTY(region, BMKCoordinateRegion, RCTBaiduMap)
{
    [view setRegion:json ? [RCTConvert BMKCoordinateRegion:json] : defaultView.region animated:YES];
}
RCT_CUSTOM_VIEW_PROPERTY(userLocationViewParams, BMKLocationViewDisplayParam, RCTBaiduMap)
{
    if (json) {
        [view setUserLocationViewParams:[RCTConvert RCTBaiduMapLocationViewDisplayParam:json]];
    }
}


RCT_CUSTOM_VIEW_PROPERTY(selectedAnnotation, RCTBaiduMapAnnotation, RCTBaiduMap){
    if (json) {
        RCTBaiduMapAnnotation *jsonAn = [RCTConvert RCTBaiduMapAnnotation:json];
        for (RCTBaiduMapAnnotation *annotation in view.annotations) {
            if([annotation.identifier isEqual:jsonAn.identifier] ){
                [view selectAnnotation:annotation animated:YES];
                break;
            }
        }
        
    }
}

RCT_CUSTOM_VIEW_PROPERTY(annotationsWithCluster, NSArray<RCTBaiduMapAnnotation *>, RCTBaiduMap)
{
    if(view.zoomLevel <= 10){
        view.showCluster = YES;
        if(json){
            [_clusterManager clearClusterItems];
            for (RCTBaiduMapAnnotation *annotation in json) {
                RCTBaiduMapAnnotation *an = [RCTConvert RCTBaiduMapAnnotation:annotation];
                BMKClusterItem *clusterItem = [[BMKClusterItem alloc] init];
                if(an.title){
                    clusterItem.title = an.title;
                }
                if(an.identifier){
                    clusterItem.identifier = an.identifier;
                }
                if(an.displayNumber){
                    clusterItem.displayNumber = an.displayNumber;
                }
                
                clusterItem.coor = CLLocationCoordinate2DMake(an.coordinate.latitude , an.coordinate.longitude );
                [_clusterManager addClusterItem:clusterItem];
            }
        }else{
            [_clusterManager clearClusterItems];
        }
        [self updateClusters:view];
      
    }else{
        view.showCluster = NO;
        NSMutableArray *addArray = [[NSMutableArray alloc] init];
        if(json){
            for (RCTBaiduMapAnnotation *annotation in json) {
                RCTBaiduMapAnnotation *an = [RCTConvert RCTBaiduMapAnnotation:annotation];
                [addArray  addObject:an];
            }
        }
        
        [view removeAnnotations:view.annotations];
        [view addAnnotations:addArray];
    }
    
}


- (NSDictionary<NSString *, id> *)constantsToExport
{
    NSString *red, *green, *purple;
    
    red = RCTBaiduMapPinRed;
    green = RCTBaiduMapPinGreen;
    purple = RCTBaiduMapPinPurple;

    return @{
             @"PinColors": @{
                     @"RED": red,
                     @"GREEN": green,
                     @"PURPLE": purple,
                     }
             };
    
}

RCT_EXPORT_METHOD(zoomToLocs:(nonnull NSNumber *)reactTag
                  locations:(NSArray *)locs)
{
    [self.bridge.uiManager addUIBlock:
     ^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry){
         RCTBaiduMap *view = viewRegistry[reactTag];
         if (!view || ![view isKindOfClass:[RCTBaiduMap class]]) {
             RCTLogError(@"Cannot find RCTBaiduMap with tag #%@", reactTag);
             return;
         }
         
         NSMutableArray<CLLocation *> *resultPoints = [NSMutableArray new];
         if (locs != nil && locs.count > 0) {
             for (id item in locs) {
                 if ([item isKindOfClass:[NSArray class]]) {
                     NSArray *oneLocation = (NSArray *)item;
                     if (oneLocation.count == 2) {
                         double latitude = [[oneLocation objectAtIndex:0] doubleValue];
                         double longitude = [[oneLocation objectAtIndex:1] doubleValue];
                         [resultPoints addObject:[[CLLocation alloc] initWithLatitude:latitude longitude:longitude]];
                     }
                 } else if ([item isKindOfClass:[NSDictionary class]]) {
                     NSDictionary *oneLocation = (NSDictionary *)item;
                     if ([oneLocation objectForKey:@"latitude"] && [oneLocation objectForKey:@"longitude"]) {
                         double latitude = [[oneLocation objectForKey:@"latitude"] doubleValue];
                         double longitude = [[oneLocation objectForKey:@"longitude"] doubleValue];
                         [resultPoints addObject:[[CLLocation alloc] initWithLatitude:latitude longitude:longitude]];
                     }
                 }
             }
         }
         
         [view zoomToSpan:resultPoints];
     }];
}

#pragma mark - BMKMapViewDelegate

- (void)mapView:(RCTBaiduMap *)mapView didSelectAnnotationView:(BMKAnnotationView *)view
{
    if (mapView.onPress && [view.annotation isKindOfClass:[RCTBaiduMapAnnotation class]]) {
        RCTBaiduMapAnnotation *annotation = (RCTBaiduMapAnnotation *)view.annotation;
        mapView.onPress(@{
                          @"action": @"annotation-click",
                          @"annotation": @{
                                  @"id": annotation.identifier?: @"",
                                  @"title": annotation.title ?: @"",
                                  @"subtitle": annotation.subtitle ?: @"",
                                  @"latitude": @(annotation.coordinate.latitude),
                                  @"longitude": @(annotation.coordinate.longitude)
                                  }
                          });
    }
    
    if ([view.annotation isKindOfClass:[RCTBaiduMapAnnotation class]]) {
        RCTBaiduMapAnnotation *annotation = (RCTBaiduMapAnnotation *)view.annotation;
        if (mapView.onAnnotationFocus) {
            mapView.onAnnotationFocus(@{
                                        @"annotationId": annotation.identifier?: @""
                                        });
        }
        if(annotation.size <= 1){
            view.image = [UIImage imageNamed:@"mapapi.bundle/images/pin_green.png"];
        }else{
            BMKMapStatus *newMapStatus = [BMKMapStatus new];
            newMapStatus.targetGeoPt = annotation.coordinate;
            newMapStatus.fLevel = mapView.zoomLevel+1;
            [mapView setMapStatus:newMapStatus withAnimation:YES];
        }
    }

    
}

- (void)mapView:(RCTBaiduMap *)mapView didDeselectAnnotationView:(BMKAnnotationView *)view
{
    if ([view.annotation isKindOfClass:[RCTBaiduMapAnnotation class]]) {
        RCTBaiduMapAnnotation *annotation = (RCTBaiduMapAnnotation *)view.annotation;
        if (mapView.onAnnotationBlur) {
            mapView.onAnnotationBlur(@{
                                        @"annotationId": annotation.identifier?: @"",
                                        });
        }
        if(annotation.size <= 1){
            //update the ping image
            view.image = [UIImage imageNamed:@"mapapi.bundle/images/pin_red.png"];
        }
    }
    
}

- (void)mapView:(RCTBaiduMap *)mapView annotationView:(BMKAnnotationView *)view didChangeDragState:(BMKAnnotationViewDragState)newState fromOldState:(BMKAnnotationViewDragState)oldState
{
    static NSArray *states;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        states = @[@"idle", @"starting", @"dragging", @"canceling", @"ending"];
    });
    
    if ([view.annotation isKindOfClass:[RCTBaiduMapAnnotation class]]) {
        RCTBaiduMapAnnotation *annotation = (RCTBaiduMapAnnotation *)view.annotation;
        if (mapView.onAnnotationDragStateChange) {
            mapView.onAnnotationDragStateChange(@{
                                                  @"state": states[newState],
                                                  @"oldState": states[oldState],
                                                  @"annotationId": annotation.identifier,
                                                  @"latitude": @(annotation.coordinate.latitude),
                                                  @"longitude": @(annotation.coordinate.longitude),
                                                  });
        }
    }
}

- (BMKAnnotationView *)mapView:(RCTBaiduMap *)mapView viewForAnnotation:(RCTBaiduMapAnnotation *)annotation
{
    if (![annotation isKindOfClass:[RCTBaiduMapAnnotation class]]) {
        return nil;
    }
    
    BMKAnnotationView *annotationView;
    if (annotation.viewIndex != NSNotFound && false) {//no used for now
        NSString *reuseIdentifier = NSStringFromClass([RCTBaiduMapAnnotationView class]);
        annotationView = [mapView dequeueReusableAnnotationViewWithIdentifier:reuseIdentifier];
        if (!annotationView) {
            annotationView = [[RCTBaiduMapAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:reuseIdentifier];
        }
        
        UIView *reactView = mapView.reactSubviews[annotation.viewIndex];
        ((RCTBaiduMapAnnotationView *)annotationView).contentView = reactView;
    } else if (annotation.image) {
        NSString *reuseIdentifier = NSStringFromClass([BMKAnnotationView class]);
        annotationView = [mapView dequeueReusableAnnotationViewWithIdentifier:reuseIdentifier];
        if (!annotationView) {
            annotationView = [[BMKAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:reuseIdentifier];
        }
        annotationView.image = annotation.image;
        
        //UIImage *img = [UIImage imageNamed:@"mapapi.bundle/images/pin_green.png"];
        //annotationView.image = img;
        annotationView.centerOffset = CGPointMake(0, 0 - annotationView.image.size.height / 2);
        
    } else {
        
        NSString *reuseIdentifier = NSStringFromClass([BMKPinAnnotationView class]);
        
        /*
        annotationView =
        [mapView dequeueReusableAnnotationViewWithIdentifier:reuseIdentifier] ?:
        [[BMKPinAnnotationView alloc] initWithAnnotation:annotation
                                        reuseIdentifier:reuseIdentifier];
        */
        
        annotationView = [mapView dequeueReusableAnnotationViewWithIdentifier:reuseIdentifier];
        
        if (!annotationView) {
            annotationView = [[BMKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:reuseIdentifier];
           
        } else {
            annotationView = nil;
            annotationView = [[BMKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:reuseIdentifier];
            
        }
        
        ((BMKPinAnnotationView *)annotationView).animatesDrop = annotation.animateDrop;
        if(annotation.showCluster){
            //((RCTBaiduMapAnnotationView*)annotationView).size = annotation.size;
        }
        
//        ((BMKPinAnnotationView *)annotationView).pinColor = annotation.tintColor;
        
        
    }
    
    annotationView.canShowCallout = (annotation.title.length > 0);
    /*
    if (annotation.leftCalloutViewIndex != NSNotFound) {
        annotationView.leftCalloutAccessoryView = mapView.reactSubviews[annotation.leftCalloutViewIndex];
    } else if (annotation.hasLeftCallout) {
        annotationView.leftCalloutAccessoryView = [UIButton buttonWithType:UIButtonTypeDetailDisclosure];
    } else {
        annotationView.leftCalloutAccessoryView = nil;
    }
    
    if (annotation.rightCalloutViewIndex != NSNotFound) {
        annotationView.rightCalloutAccessoryView = mapView.reactSubviews[annotation.rightCalloutViewIndex];
    } else if (annotation.hasRightCallout) {
        annotationView.rightCalloutAccessoryView = [UIButton buttonWithType:UIButtonTypeDetailDisclosure];
    } else {
        annotationView.rightCalloutAccessoryView = nil;
    }
    */
    annotationView.draggable = annotation.draggable;
    
    
    if (annotation.title && annotation.size <= 1){
        //paopao view start
        UIFont *fontName = [UIFont systemFontOfSize:16.0f];
        //定义字体大小
        CGSize sizeTitle = [annotation.title sizeWithFont:fontName constrainedToSize:CGSizeMake(280,MAXFLOAT) lineBreakMode:NSLineBreakByWordWrapping];
        
        
        
        UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake(8, 4, 250, sizeTitle.height)];
        titleLabel.text = annotation.title;
        titleLabel.numberOfLines = 0;
        titleLabel.backgroundColor = [UIColor clearColor];
        titleLabel.font = [UIFont systemFontOfSize:16];
        titleLabel.textColor = [UIColor blackColor];
        titleLabel.textAlignment = NSTextAlignmentLeft;
        //titleLabel.lineBreakMode = UILineBreakModeWordWrap;
        UIView *popView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 280, sizeTitle.height + 8)];
        popView.backgroundColor = [UIColor whiteColor];
        [popView.layer setMasksToBounds:YES];
        [popView.layer setCornerRadius:3.0];
        popView.alpha = 0.9;
        
        [popView addSubview:titleLabel];
        
        BMKActionPaopaoView *pView = [[BMKActionPaopaoView alloc]initWithCustomView:popView];
            
       
        pView.frame = CGRectMake(0, 0, 280, sizeTitle.height + 18);
        annotationView.paopaoView = nil;
        annotationView.paopaoView = pView;
    }
    
    //paopao view end
    if (annotation.displayNumber) {
      
        UILabel *la = [[UILabel alloc]initWithFrame:CGRectMake(0, 0, annotationView.frame.size.width,annotationView.frame.size.height-annotationView.frame.size.height*20/69)];
        la.backgroundColor = [UIColor clearColor];
        la.font = [UIFont systemFontOfSize:12];
        la.textAlignment = NSTextAlignmentCenter;
        la.text = annotation.displayNumber;
        la.textColor = [UIColor whiteColor];
        la.tag = [annotation.displayNumber intValue];
        [annotationView addSubview:la];
    }
    if(annotation.size > 1){
        //we may use it later
        /*
        UILabel *la = [[UILabel alloc]initWithFrame:CGRectMake(0, 0, annotationView.frame.size.width,annotationView.frame.size.height-annotationView.frame.size.height*20/69)];
        la.backgroundColor = [UIColor clearColor];
        la.font = [UIFont systemFontOfSize:12];
        la.textAlignment = NSTextAlignmentCenter;
        la.text = [NSString stringWithFormat:@"%d",annotation.size];
        la.textColor = [UIColor whiteColor];
        la.tag = [annotation.displayNumber intValue];
        [annotationView addSubview:la];
         */
        UIImage *img = [UIImage imageNamed:@"mapapi.bundle/images/pin_purple.png"];
        annotationView.image = img;
        
    }

    return annotationView;
}

- (BMKOverlayView *)mapView:(BMKMapView *)mapView viewForOverlay:(RCTBaiduMapOverlay *)overlay
{
    if ([overlay isKindOfClass:[RCTBaiduMapOverlay class]]) {
        BMKPolylineView *polylineView = [[BMKPolylineView alloc] initWithOverlay:overlay];
        polylineView.strokeColor = overlay.strokeColor;
        polylineView.lineWidth = overlay.lineWidth;
        return polylineView;
    }
    
    return nil;
}

- (void)mapView:(RCTBaiduMap *)mapView regionWillChangeAnimated:(BOOL)animated
{
    [self _regionChanged:mapView];
    mapView.regionChangeObserveTimer = [NSTimer timerWithTimeInterval:RCTBaiduMapRegionChangeObserveInterval target:self selector:@selector(_onTick:) userInfo:@{RCTBaiduMapViewKey: mapView} repeats:YES];
    
    [[NSRunLoop mainRunLoop] addTimer:mapView.regionChangeObserveTimer forMode:NSRunLoopCommonModes];
}

- (void)mapView:(RCTBaiduMap *)mapView regionDidChangeAnimated:(BOOL)animated
{
    [mapView.regionChangeObserveTimer invalidate];
    mapView.regionChangeObserveTimer = nil;
    
    [self _regionChanged:mapView];
    
    if (mapView.hasStartedRendering) {
        [self _emitRegionChangeEvent:mapView continuous:NO];
    }
}

- (void)mapViewDidFinishLoading:(RCTBaiduMap *)mapView
{
    mapView.hasStartedRendering = YES;
    if (mapView.autoZoomToSpan) {
        [mapView zoomToSpan];
    }
    [self _emitRegionChangeEvent:mapView continuous:NO];
}

- (void)mapStatusDidChanged:(RCTBaiduMap *)mapView
{
    if (mapView.showCluster && _clusterZoom != 0 && _clusterZoom != (NSInteger)mapView.zoomLevel) {
        [self updateClusters:mapView];
    }
}

#pragma mark - Private

- (void)_onTick:(NSTimer *)timer
{
    [self _regionChanged:timer.userInfo[RCTBaiduMapViewKey]];
}

- (void)_regionChanged:(RCTBaiduMap *)mapView
{
    BOOL needZoom = NO;
    CGFloat newLongitudeDelta = 0.0f;
    BMKCoordinateRegion region = mapView.region;
    
    if (!CLLocationCoordinate2DIsValid(region.center)) {
        return;
    }
    
    if (mapView.maxDelta > FLT_EPSILON && region.span.longitudeDelta > mapView.maxDelta) {
        needZoom = YES;
        newLongitudeDelta = mapView.maxDelta * (1 - RCTBaiduMapZoomBoundBuffer);
    } else if (mapView.minDelta > FLT_EPSILON && region.span.longitudeDelta < mapView.minDelta) {
        needZoom = YES;
        newLongitudeDelta = mapView.minDelta * (1 + RCTBaiduMapZoomBoundBuffer);
    }
    if (needZoom) {
        region.span.latitudeDelta = region.span.latitudeDelta / region.span.longitudeDelta * newLongitudeDelta;
        region.span.longitudeDelta = newLongitudeDelta;
        mapView.region = region;
    }
    
    [self _emitRegionChangeEvent:mapView continuous:YES];
}

- (void)_emitRegionChangeEvent:(RCTBaiduMap *)mapView continuous:(BOOL)continuous
{
    if (mapView.onChange) {
        BMKCoordinateRegion region = mapView.region;
        if(!continuous){
            //region = [mapView regionThatFits:region];
        }
        
        if (!CLLocationCoordinate2DIsValid(region.center)) {
            return;
        }
        
        mapView.onChange(@{
                           @"continuous": @(continuous),
                           @"region": @{
                                   @"latitude_bak": @(RCTZeroIfNaN(region.center.latitude)),
                                   @"longitude_bak": @(RCTZeroIfNaN(region.center.longitude)),
                                   @"latitude": @(RCTZeroIfNaN(mapView.centerCoordinate.latitude)),
                                   @"longitude": @(RCTZeroIfNaN(mapView.centerCoordinate.longitude)),
                                   @"latitudeDelta": @(RCTZeroIfNaN(region.span.latitudeDelta)),
                                   @"longitudeDelta": @(RCTZeroIfNaN(region.span.longitudeDelta)),
                                   @"zoomLevel": @(mapView.zoomLevel)
                                   },
                           
                           });
    }
}


//更新聚合状态
- (void)updateClusters:(RCTBaiduMap *)mapView
{
    _clusterZoom = (NSInteger)mapView.zoomLevel;
    @synchronized(_clusterCaches) {
        __block NSMutableArray *clusters = [_clusterCaches objectAtIndex:(_clusterZoom - 3)];
        
        if (clusters.count > 0 && false) {
            [mapView removeAnnotations:mapView.annotations];
            [mapView addAnnotations:clusters];
        } else {
            dispatch_async(dispatch_get_global_queue(0, 0), ^{
                clusters = [[NSMutableArray alloc] init];
                ///获取聚合后的标注
                __block NSArray *array = [_clusterManager getClusters:_clusterZoom];
                
                dispatch_async(dispatch_get_main_queue(), ^{
                    for (BMKCluster *item in array) {
                        RCTBaiduMapAnnotation *annotation = [[RCTBaiduMapAnnotation alloc] init];
                        annotation.coordinate = item.coordinate;
                        annotation.size = item.size;
                        annotation.showCluster = true;
                        
                        if(item.size <= 1){
                            BMKClusterItem* clusterItem = (BMKClusterItem*)[item.clusterItems objectAtIndex:0];
                            if(clusterItem.title){
                                annotation.title  = clusterItem.title;
                            }
                            if(clusterItem.identifier){
                                annotation.identifier = clusterItem.identifier;
                            }else{
                                annotation.identifier = @"";
                            }
                            
                            if(clusterItem.displayNumber){
                                annotation.displayNumber =clusterItem.displayNumber;
                            }
                            
                        }
                        
                        [clusters addObject:annotation];
                    }
                    [mapView removeAnnotations:mapView.annotations];
                    [mapView addAnnotations:clusters];
                    
                });
            });
        }
    }
}

@end
