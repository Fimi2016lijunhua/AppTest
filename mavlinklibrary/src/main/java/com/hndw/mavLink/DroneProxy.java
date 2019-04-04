package com.hndw.mavLink;


import com.MAVLink.common.msg_mission_item;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.model.AbstractCommandListener;

import java.util.List;

/**
 * @author ljh create on 2019-3-25
 */
public interface  DroneProxy {
    /**
     * take off with the altitude
     * @param altitude
     * @param listener
     */
    void takeOff(double altitude, AbstractCommandListener listener);

    /**
     * land now
     */
    void land(AbstractCommandListener listener);

    /**
     *
     * @param lock
     * @param listener
     */
    void unLockOrNot(boolean lock,AbstractCommandListener listener);

    /**
     * 添加任务，可以是一系列动作
     * @param mission
     */
    void addWayPoint(Mission mission);

    /**
     * Changes the vehicle home location.
     *
     * @param homeLocation New home coordinate
     * @param listener     Register a callback to receive update of the command execution state.
     */
     void setVehicleHome(final LatLongAlt homeLocation, final AbstractCommandListener listener);

    /**
     * Enables 'return to me'
     * @param isEnabled
     * @param listener
     */
     void enableReturnToMe(boolean isEnabled, final AbstractCommandListener listener);

    /**
     * connection drone
     */
    void conDrone(ConnectionParameter parameter);

    /**
     * disconnect drone
     */
    void disConDrone();

    /**
     * set vehicle mode
     */
    void setDroneMode(VehicleMode mode, AbstractCommandListener listener);


    void onMissionReceived(List<msg_mission_item> list);

    /**
     * return home point
     */
    void gotoHome(AbstractCommandListener listener);

}
