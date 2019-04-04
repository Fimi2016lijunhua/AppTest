package com.example.apptest;

import com.baidu.mapapi.model.LatLng;

import java.util.List;

/**
 * @author ljh create on 2019-4-3
 */
public class SmartWayPoint {
    /**
     * @param {Array} latlngs - 格式为[{lat,lng}]的经纬度数组，多边形的顶点集合
     * @return {Object} .center - 返回外接矩形的中心的
     * @method 创建多边形外接矩形
     */
    public CenterRect createPolygonBounds(List<LatLng> latlngs) {
        double maxLat = 0;
        double minLat = 0;
        double maxLng = 0;
        double minLng = 0;
        int size = latlngs.size();
        for (int i = 0; i < size; i++) {
            if (i < size - 1) {
                LatLng preLatLng = latlngs.get(i);
                LatLng nexLatLng = latlngs.get(i + 1);
                maxLng = preLatLng.longitude <= nexLatLng.longitude ? nexLatLng.longitude : preLatLng.longitude;
                minLng = preLatLng.longitude >= nexLatLng.longitude ? nexLatLng.longitude : preLatLng.longitude;
                maxLat = preLatLng.latitude <= nexLatLng.latitude ? nexLatLng.latitude : preLatLng.latitude;
                minLat = preLatLng.latitude >= nexLatLng.latitude ? nexLatLng.latitude : preLatLng.latitude;
            }
        }
        LatLng cLatLng = new LatLng((maxLat + minLat) / 2, (maxLng + minLng) / 2);
        CenterRect centerRect = new CenterRect();
        centerRect.setNeLatLng(new LatLng(maxLat, maxLng));
        centerRect.setNwLatLng(new LatLng(maxLat, minLng));
        centerRect.setSeLatLng(new LatLng(minLat, maxLng));
        centerRect.setSwLatLng(new LatLng(minLat, minLng));
        centerRect.setCenterPoint(cLatLng);
        return centerRect;
    }

}
