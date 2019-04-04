package com.example.apptest;

import android.content.res.Resources;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.o3dr.services.android.lib.coordinate.LatLong;

import java.util.ArrayList;
import java.util.List;

public class MapUtils {
    static public LatLng coordToLatLng(LatLong coord) {
        return new LatLng(coord.getLatitude(), coord.getLongitude());
    }

    public static List<LatLng> coordToLatLng(List<? extends LatLong> coords) {
        List<LatLng> points = new ArrayList<>(coords.size());
        for (LatLong coord : coords) {
            points.add(coordToLatLng(coord));
        }

        return points;
    }

    public static LatLong latLngToCoord(LatLng point) {
        return new LatLong((float) point.latitude, (float) point.longitude);
    }

    public static LatLong locationToCoord(Location location) {
        return new LatLong((float) location.getLatitude(), (float) location.getLongitude());
    }

    public static int scaleDpToPixels(double value, Resources res) {
        final float scale = res.getDisplayMetrics().density;
        return (int) Math.round(value * scale);
    }

    public static List<com.baidu.mapapi.model.LatLng> coordToBaiduLatLng(List<? extends LatLong> coords) {
        List<com.baidu.mapapi.model.LatLng> points = new ArrayList<>(coords.size());
        for (LatLong coord : coords) {
            points.add(coordToBaiduLatLng(coord));
        }
        return points;
    }

    public static com.baidu.mapapi.model.LatLng coordToBaiduLatLng(LatLong coord) {
        return new com.baidu.mapapi.model.LatLng(coord.getLatitude(), coord.getLongitude());
    }

    public static LatLong baiduLatLngToCoord(com.baidu.mapapi.model.LatLng point) {
        return new LatLong((float) point.latitude, (float) point.longitude);
    }
}
