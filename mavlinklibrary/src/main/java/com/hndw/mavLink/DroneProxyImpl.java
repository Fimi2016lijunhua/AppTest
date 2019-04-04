package com.hndw.mavLink;

import android.os.Bundle;
import android.util.Log;

import com.MAVLink.common.msg_mission_item;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.MissionApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import org.droidplanner.services.android.impl.core.drone.DroneInterfaces;
import org.droidplanner.services.android.impl.core.drone.autopilot.generic.GenericMavLinkDrone;
import org.droidplanner.services.android.impl.core.drone.variables.ApmModes;
import org.droidplanner.services.android.impl.utils.MissionUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

import static com.o3dr.services.android.lib.drone.action.StateActions.ACTION_ENABLE_RETURN_TO_ME;
import static com.o3dr.services.android.lib.drone.action.StateActions.ACTION_SET_VEHICLE_HOME;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_IS_RETURN_TO_ME_ENABLED;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_VEHICLE_HOME_LOCATION;

public class DroneProxyImpl implements DroneProxy, DroneListener {
    private Drone drone;
    private boolean isReturnToMeOn = true;
    private static DroneProxyImpl droneProxy;

    public DroneProxyImpl(Drone drone) {
        this.drone = drone;
    }

    public static DroneProxyImpl getDroneProxyInstance(Drone drone) {
        if (droneProxy == null) {
            synchronized (DroneProxyImpl.class) {
                droneProxy = new DroneProxyImpl(drone);
            }
        }
        return droneProxy;
    }

    public Drone getDrone() {
        return drone;
    }

    @Override
    public void takeOff(double altitude, AbstractCommandListener listener) {
        ControlApi.getApi(drone).takeoff(altitude, listener);
    }

    @Override
    public void land(AbstractCommandListener listener) {
        VehicleApi.getApi(drone).setVehicleMode(VehicleMode.COPTER_LAND, listener);
    }

    @Override
    public void unLockOrNot(boolean lock, AbstractCommandListener listener) {
        VehicleApi.getApi(drone).arm(lock, true, listener);
    }

    /**
     * Changes the vehicle home location.
     *
     * @param homeLocation New home coordinate
     * @param listener     Register a callback to receive update of the command execution state.
     */
    public void setVehicleHome(final LatLongAlt homeLocation, final AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_VEHICLE_HOME_LOCATION, homeLocation);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_VEHICLE_HOME, params), listener);
    }

    /**
     * Enables 'return to me'
     *
     * @param isEnabled
     * @param listener
     */
    public void enableReturnToMe(boolean isEnabled, final AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_IS_RETURN_TO_ME_ENABLED, isEnabled);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_ENABLE_RETURN_TO_ME, params), listener);
    }

    @Override
    public void addWayPoint(Mission mission) {
        if (mission != null) {
            MissionApi.getApi(drone).setMission(mission, true);
        }
    }

    @Override
    public void conDrone(ConnectionParameter parameter) {
        drone.connect(parameter);
        drone.registerDroneListener(this);
    }

    @Override
    public void disConDrone() {
        drone.disconnect();
        drone.unregisterDroneListener(this);
    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        if (event == AttributeEvent.STATE_CONNECTED) {
            VehicleApi.getApi(drone).enableReturnToMe(isReturnToMeOn, new AbstractCommandListener() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError(int i) {
                }

                @Override
                public void onTimeout() {
                }
            });
        }
        MessageEvent eventRt = new MessageEvent();
        eventRt.setEventKey(event);
        eventRt.setBundle(extras);
        EventBus.getDefault().post(eventRt);
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {
        MessageEvent eventRt = new MessageEvent();
        Bundle bundle = new Bundle();
        bundle.putString("errorMsg", errorMsg);
        eventRt.setBundle(bundle);
        eventRt.setEventKey("droneKey");
        EventBus.getDefault().post(eventRt);
    }

    @Override
    public void setDroneMode(VehicleMode mode, AbstractCommandListener listener) {
        VehicleApi.getApi(drone).setVehicleMode(mode, listener);
    }

    @Override
    public void onMissionReceived(List<msg_mission_item> list) {
/*        if (list != null) {
            ((GenericMavLinkDrone) drone).processHomeUpdate((msg_mission_item) list.get(0));
            list.remove(0);
            this.items.clear();
            this.items.addAll(MissionUtils.processMavLinkMessages(this, list));
            ((GenericMavLinkDrone) this.myDrone).notifyDroneEvent(DroneInterfaces.DroneEventsType.MISSION_RECEIVED);
            notifyMissionUpdate();

            VehicleApi.getApi(drone).setVehicleHome(list.get(0),null);
        }*/
    }


    @Override
    public void gotoHome(AbstractCommandListener listener) {
        VehicleApi.getApi(drone).setVehicleMode(VehicleMode.COPTER_RTL,listener);
    }
}
