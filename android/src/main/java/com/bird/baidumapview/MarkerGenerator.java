package com.bird.baidumapview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.clusterutil.clustering.view.DefaultClusterRenderer;
import com.baidu.mapapi.clusterutil.projection.Point;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fenglei108 on 4/16/16.
 */
public class MarkerGenerator {
    private final Map<String, BitmapDescriptor> _resources = new HashMap<String, BitmapDescriptor>();

    private final List<Integer> _ids = new ArrayList<Integer>();;

    public MarkerGenerator(){
        _ids.add(R.drawable.mark1);
        _ids.add(R.drawable.mark2);
        _ids.add(R.drawable.mark3);
        _ids.add(R.drawable.mark4);
        _ids.add(R.drawable.mark5);
        _ids.add(R.drawable.mark6);
        _ids.add(R.drawable.mark7);
        _ids.add(R.drawable.mark8);
        _ids.add(R.drawable.mark9);
        _ids.add(R.drawable.mark10);
        _ids.add(R.drawable.mark11);
        _ids.add(R.drawable.mark12);
        _ids.add(R.drawable.mark13);
        _ids.add(R.drawable.mark14);
        _ids.add(R.drawable.mark15);
        _ids.add(R.drawable.mark16);
        _ids.add(R.drawable.mark17);
        _ids.add(R.drawable.mark18);
        _ids.add(R.drawable.mark19);
        _ids.add(R.drawable.mark20);
        _ids.add(R.drawable.mark21);
        _ids.add(R.drawable.mark22);
        _ids.add(R.drawable.mark23);
        _ids.add(R.drawable.mark24);
        _ids.add(R.drawable.mark25);
        _ids.add(R.drawable.mark26);
        _ids.add(R.drawable.mark27);
        _ids.add(R.drawable.mark28);
        _ids.add(R.drawable.mark29);
        _ids.add(R.drawable.mark30);
    }

    public BitmapDescriptor getIcon(int i){
        if(i >=30){
            i = 29;
        }
        if(_resources.get(i+"") != null){
            return _resources.get(i+"");
        }
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(_ids.get(i));
        _resources.put(i+"",icon);
        //Log.e("getIcon", "i = " + i+", icon = "+icon);
        return icon;
    }

}
