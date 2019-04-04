package com.example.apptest;

import com.baidu.mapapi.model.LatLng;

public class CenterRect {
    /**
     * 西北
     */
    private LatLng nwLatLng;
    /**
     * 东北
     */
    private LatLng neLatLng;
    /**
     * 东南
     */
    private LatLng seLatLng;
    /**
     * 西南
     */
    private LatLng swLatLng;
    /**
     * 中心
     */
    private LatLng centerPoint;

    public LatLng getNwLatLng() {
        return nwLatLng;
    }

    public void setNwLatLng(LatLng nwLatLng) {
        this.nwLatLng = nwLatLng;
    }

    public LatLng getNeLatLng() {
        return neLatLng;
    }

    public void setNeLatLng(LatLng neLatLng) {
        this.neLatLng = neLatLng;
    }

    public LatLng getSeLatLng() {
        return seLatLng;
    }

    public void setSeLatLng(LatLng seLatLng) {
        this.seLatLng = seLatLng;
    }

    public LatLng getSwLatLng() {
        return swLatLng;
    }

    public void setSwLatLng(LatLng swLatLng) {
        this.swLatLng = swLatLng;
    }

    public LatLng getCenterPoint() {
        return centerPoint;
    }

    public void setCenterPoint(LatLng centerPoint) {
        this.centerPoint = centerPoint;
    }
}
