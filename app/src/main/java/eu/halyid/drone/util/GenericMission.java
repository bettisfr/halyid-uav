package eu.halyid.drone.util;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import dji.common.mission.waypointv2.Action.WaypointV2Action;
import dji.common.mission.waypointv2.WaypointV2;
import dji.common.mission.waypointv2.WaypointV2Mission;

public abstract class GenericMission {
    public abstract List<WaypointV2> getWaypointV2List();

    public abstract List<WaypointV2Action> createWaypointAction();

    public abstract WaypointV2Mission createWaypointMission();

    public abstract WaypointV2Mission createRTHMission(LatLng home);
}
