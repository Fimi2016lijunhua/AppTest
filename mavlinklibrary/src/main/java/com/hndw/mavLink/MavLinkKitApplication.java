package com.hndw.mavLink;

import android.app.Application;
import android.os.Handler;
import android.widget.Toast;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.TowerListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MavLinkKitApplication extends Application implements TowerListener {
    private Drone drone;
    private Handler handler = new Handler();
    private ControlTower controlTower;


    @Override
    public void onCreate() {
        super.onCreate();
        initKit();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        controlTower.disconnect();
        EventBus.getDefault().unregister(this);
    }

    private void initKit() {
        controlTower = new ControlTower(getApplicationContext());
        drone = new Drone(this);
        controlTower.connect(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onTowerConnected() {
        controlTower.registerDrone(drone, handler);
        Toast.makeText(this, "tower is connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTowerDisconnected() {
        controlTower.unregisterDrone(drone);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEvent(MessageEvent event) {
        if (event != null && event.getEventKey().equals("droneKey")) {
            if (controlTower != null)
                controlTower.unregisterDrone(drone);
        }
    }

    public Drone getDrone() {
        return drone;
    }
}
