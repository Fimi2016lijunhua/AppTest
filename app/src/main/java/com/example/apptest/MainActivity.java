package com.example.apptest;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.hndw.mavLink.DroneProxyImpl;
import com.hndw.mavLink.MavLinkKitApplication;
import com.hndw.mavLink.MessageEvent;
import com.hndw.smartlibrary.until.GPS;
import com.hndw.smartlibrary.until.GPSConverterUtils;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.attribute.error.ErrorType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.spatial.BaseSpatialItem;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener, BaiduMap.OnMarkerClickListener, SensorEventListener {
    private Button takeOffBtn, lockBtn, landBtn, wayBtn, ble_btn, homeKeyBtn, goHomeBtn, addBtn;
    private DroneProxyImpl droneProxy;
    private TextView rt_tv;
    MavLinkKitApplication kitApplication;
    private MapView mapView;
    private final int SDK_PERMISSION_REQUEST = 127;
    private BaiduMap mBaiduMap;
    BitmapDescriptor markBmp, homeBmp;
    private LocationService locationService;
    LatLng homePoint;
    MapStatus.Builder builder;
    LatLng target;
    LatLng curLatLng;
    boolean isFirstLoc = true; // 是否首次定位
    private SensorManager mSensorManager;
    MyLocationListenner locationListenner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPersimmions();
        takeOffBtn = this.<Button>findViewById(R.id.take_off_btn);
        lockBtn = this.<Button>findViewById(R.id.lock_btn);
        landBtn = this.<Button>findViewById(R.id.land_btn);
        wayBtn = this.<Button>findViewById(R.id.way_btn);
        ble_btn = this.<Button>findViewById(R.id.ble_btn);
        rt_tv = this.<TextView>findViewById(R.id.rt_txt);
        homeKeyBtn = this.<Button>findViewById(R.id.home_point_btn);
        goHomeBtn = this.<Button>findViewById(R.id.home_btn);
        addBtn = this.<Button>findViewById(R.id.add_point_btn);
        mapView = findViewById(R.id.map_view);
        mBaiduMap = mapView.getMap();
        takeOffBtn.setOnClickListener(this);
        landBtn.setOnClickListener(this);
        wayBtn.setOnClickListener(this);
        lockBtn.setOnClickListener(this);
        ble_btn.setOnClickListener(this);
        goHomeBtn.setOnClickListener(this);
        homeKeyBtn.setOnClickListener(this);
        addBtn.setOnClickListener(this);
        markBmp = BitmapDescriptorFactory.fromResource(R.drawable.ic_wp_map);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);// 获取传感器管理服务
        locationListenner = new MyLocationListenner();
        initListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasWriteStoragePermission = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            }
        }
        kitApplication = (MavLinkKitApplication) getApplication();
        droneProxy = DroneProxyImpl.getDroneProxyInstance(kitApplication.getDrone());
        EventBus.getDefault().register(this);

        builder = new MapStatus.Builder();
        builder.zoom(16);
        mapView.getMap().setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        mBaiduMap.setOnMarkerClickListener(this::onMarkerClick);
        mBaiduMap.removeMarkerClickListener(this::onMarkerClick);
        mBaiduMap.setMyLocationEnabled(true);
        // -----------location config ------------
        locationService = ((DroidPlannerApp) getApplication()).locationService;
        locationService.registerListener(locationListenner);
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        locationService.start();// 定位SDK
    }

    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                StringBuffer sb = new StringBuffer(256);
                sb.append(location.getTime());
                sb.append("\nlocType : ");// 定位类型
                sb.append(location.getLocType());
                sb.append("\nlatitude : ");// 纬度
                sb.append(location.getLatitude());
                sb.append("\nlontitude : ");// 经度
                sb.append(location.getLongitude());
                homeBmp = BitmapDescriptorFactory.fromResource(R.drawable.redplane);
                OverlayOptions option = new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .icon(homeBmp);
                Marker marker = (Marker) mBaiduMap.addOverlay(option);
                marker.setTitle("HomeKey");
                marker.setZIndex(1);
                builder.target(new LatLng(location.getLatitude(), location.getLongitude()));
                builder.zoom(18);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                curLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                Log.i("ljh", "Home坐标:" + sb);
                rt_tv.setText("point : " + sb);
            }
        }

    };


    /**
     * 定位SDK监听函数
     */
    class MyLocationListenner extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {
            curLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            StringBuffer sb = new StringBuffer(256);
            sb.append(location.getTime());
            sb.append("\nlocType : ");// 定位类型
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");// 纬度
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");// 经度
            sb.append(location.getLongitude());
            sb.append("\nlocType ：" + location.getCoorType());
            rt_tv.setText("point : " + sb);

            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            locData = new MyLocationData.Builder().accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                homePoint = new LatLng(location.getLatitude(), location.getLongitude());
                homeBmp = BitmapDescriptorFactory.fromResource(R.drawable.redplane);
                OverlayOptions option = new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .icon(homeBmp);
                Marker marker = (Marker) mBaiduMap.addOverlay(option);
                marker.setTitle("HomeKey");
                marker.setZIndex(1);
                builder.target(new LatLng(location.getLatitude(), location.getLongitude()));
                builder.zoom(18);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
            Log.e("ljh", "--------------------------------------");
            Log.i("ljh", "latlang : " + sb + " \nGPS : " + curLatLng.toString());
        }
    }

    double lastX;
    private MyLocationData locData;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //每次方向改变，重新给地图设置定位数据，用上一次onReceiveLocation得到的经纬度、精度
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {// 方向改变大于1度才设置，以免地图上的箭头转动过于频繁
            mCurrentDirection = (int) x;
            locData = new MyLocationData.Builder().accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(mCurrentLat).longitude(mCurrentLon).build();
            mBaiduMap.setMyLocationData(locData);
        }
        lastX = x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        // 为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        mapView.getMap().clear();
        mBaiduMap.setMyLocationEnabled(false);
        mapView = null;
        markBmp.recycle();
        homeBmp.recycle();
        super.onDestroy();
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        String stringExtra = intent.getStringExtra("address");
        if (stringExtra != null) {
            ConnectionParameter newBluetoothConnection = ConnectionParameter.newBluetoothConnection(stringExtra, null, 200);
            if (newBluetoothConnection != null) {
                ConnectionParameter parameter = ConnectionParameter.newBluetoothConnection(stringExtra, null, 200);
                droneProxy.conDrone(parameter);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lock_btn:
                if (curMode != VehicleMode.COPTER_STABILIZE) {
                    droneProxy.setDroneMode(VehicleMode.COPTER_STABILIZE, new SimpleCommandListener() {
                        @Override
                        public void onSuccess() {
                            super.onSuccess();
                            droneProxy.unLockOrNot(true, new SimpleCommandListener() {
                                @Override
                                public void onSuccess() {
                                    super.onSuccess();
                                    Toast.makeText(MainActivity.this, "解锁成功", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(int executionError) {
                                    super.onError(executionError);
                                }

                                @Override
                                public void onTimeout() {
                                    super.onTimeout();
                                }
                            });
                        }
                    });
                }
                break;
            case R.id.take_off_btn:
                droneProxy.takeOff(10, new SimpleCommandListener() {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                    }

                    @Override
                    public void onError(int executionError) {
                        super.onError(executionError);
                    }

                    @Override
                    public void onTimeout() {
                        super.onTimeout();
                    }
                });
                break;
            case R.id.land_btn:
                droneProxy.land(null);
                break;
            case R.id.way_btn:
                droneProxy.setDroneMode(VehicleMode.COPTER_AUTO, new SimpleCommandListener() {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        Mission wayMission = new Mission();
                        MissionItemType selectedType = MissionItemType.WAYPOINT;
                        for (int j = 0; j < points.size(); j++) {
                            GPS gps = GPSConverterUtils.bd09_To_Gps84(points.get(j).latitude,points.get(j).longitude);
                            LatLong mLatlng = MapUtils.baiduLatLngToCoord(new LatLng(gps.getLat(),gps.getLon()));
                            BaseSpatialItem spatialItem = (BaseSpatialItem) selectedType.getNewItem();
                            spatialItem.setCoordinate(/*new LatLongAlt(points.get(j).latitude, points.get(j).longitude*/new LatLongAlt(mLatlng.getLatitude(), mLatlng.getLongitude(), 10));
                            wayMission.addMissionItem(spatialItem);
                        }
                        droneProxy.addWayPoint(wayMission);
                        Toast.makeText(MainActivity.this, "模式自设置成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int executionError) {
                        super.onError(executionError);
                    }

                    @Override
                    public void onTimeout() {
                        super.onTimeout();
                    }
                });
                break;
            case R.id.ble_btn:
                startActivityForResult(new Intent(this, BluetoothDevicesActivity.class), 1);
                break;
            case R.id.home_btn:
                droneProxy.gotoHome(null);
                mBaiduMap.clear();
                points.clear();
                builder.target(homePoint).zoom(mCurrentZoom);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                break;
            case R.id.home_point_btn:
                droneProxy.setVehicleHome(new LatLongAlt(homePoint.latitude, homePoint.longitude, 15), null);
                break;
            case R.id.add_point_btn:
                points.add(curLatLng);
                break;
        }
    }

    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(this);
        locationService.unregisterListener(locationListenner);
        locationService.stop();
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    VehicleMode curMode;
    private boolean initHome = true;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event != null) {
            if (event.getEventKey() == AttributeEvent.STATE_CONNECTED) {
                rt_tv.setText("Drone 连接成功");
            } else if (event.getEventKey() == AttributeEvent.AUTOPILOT_ERROR) {
                String errorName = event.getBundle().getString(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID);
                ErrorType errorType = ErrorType.getErrorById(errorName);
                CharSequence errorLabel = errorType.getLabel(MainActivity.this);
                rt_tv.setText(errorLabel);
            } else if (event.getEventKey() == AttributeEvent.ATTITUDE_UPDATED) {
                State state = kitApplication.getDrone().getAttribute(AttributeType.STATE);
                state.toString();
                if (curMode != null && curMode != state.getVehicleMode()) {
                    curMode = state.getVehicleMode();
                    rt_tv.setText(curMode.toString());
                }
                Log.i("ljh", "the state Mode: " + state.getVehicleMode() + " isFlying : " + state.isFlying() + " isArmed : " + state.isArmed() + " /error_id :" + state.getAutopilotErrorId());
            } else if (event.getEventKey() == AttributeEvent.GPS_POSITION) {
                if (initHome) {
                    Gps gps = kitApplication.getDrone().getAttribute(AttributeType.GPS);
                    if (gps != null && gps.isValid()) {
                        LatLong coord = gps.getPosition();
                        initHomePoint(coord);
                        initHome = false;
                    }
                }
            }
        }
    }


    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            /*
             * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
             */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            }
            // 读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
            }
            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            } else {
                permissionsList.add(permission);
                return false;
            }
        } else {
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 对地图事件的消息响应
     */
    private void initListener() {
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            public void onMapClick(LatLng point) {
                points.add(point);
                updateMapState();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        mBaiduMap.setOnMapDoubleClickListener(new BaiduMap.OnMapDoubleClickListener() {
            public void onMapDoubleClick(LatLng point) {
                coordinateConvert();
            }
        });

        /**
         * 地图状态发生变化
         */
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            public void onMapStatusChangeStart(MapStatus status) {
                updateMapState();
            }

            @Override
            public void onMapStatusChangeStart(MapStatus status, int reason) {

            }

            public void onMapStatusChangeFinish(MapStatus status) {
                updateMapState();
            }

            public void onMapStatusChange(MapStatus status) {
                mCurrentZoom = status.zoom;//获取手指缩放地图后的值
            }
        });
    }

    private float mCurrentZoom = 5;
    List<LatLng> points = new ArrayList<>();

    /**
     * 更新地图状态显示面板
     */
    private void updateMapState() {
        List<OverlayOptions> options = new ArrayList<>();
        for (int j = 0; j < points.size(); j++) {
            MarkerOptions ooA = new MarkerOptions().position(points.get(j)).icon(markBmp);
            ooA.zIndex(j + 1);
            options.add(ooA);
        }
        mBaiduMap.addOverlays(options);
        //设置折线的属性
        if (points.size() < 2) return;
        OverlayOptions mOverlayOptions = new PolylineOptions()
                .width(10)
                .color(0xAAFF0000)
                .points(points);
        // Overlay mPolyline = mBaiduMap.addOverlay(mOverlayOptions);

        OverlayOptions ooPolyline = new PolylineOptions().width(13).color(0xAAFF0000).points(points);
        //在地图上画出线条图层，mPolyline：线条图层
        Overlay mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
        mPolyline.setZIndex(3);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng latLng = marker.getPosition();//信息窗口显示的位置点
        Toast.makeText(MainActivity.this, "当前坐标：" + latLng.latitude + "," + latLng.longitude, Toast.LENGTH_SHORT).show();
        return true;
    }

    /**
     * 讲google地图的wgs84坐标转化为百度地图坐标
     */
    private void coordinateConvert() {
        double lanSum = 0;
        double lonSum = 0;
        for (int i = 0; i < points.size(); i++) {
            lanSum += points.get(i).latitude;
            lonSum += points.get(i).longitude;
        }
        target = new LatLng(lanSum / points.size(), lonSum / points.size());
        builder.target(target).zoom(mCurrentZoom);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    private LatLng initHomePoint(LatLong coord) {
        GPS gps = GPSConverterUtils.gps84_To_Gcj02(coord.getLatitude(), coord.getLongitude());
        GPS baidu = GPSConverterUtils.gcj02_To_Bd09(gps.getLat(), gps.getLon());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(baidu.getLat(), baidu.getLon()))
                .draggable(false)
                .alpha(0.5f)
                .title("Home")
                .visible(true);

        BitmapDescriptor homeMark = BitmapDescriptorFactory.fromResource(R.drawable.ic_home);
        if (homeMark != null) {
            markerOptions.icon(homeMark);
            mBaiduMap.addOverlay(markerOptions);
        }
        return null;
    }
}
