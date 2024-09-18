package eu.halyid.drone;

import android.graphics.PointF;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.mission.waypointv2.Action.ActionTypes;
import dji.common.mission.waypointv2.Action.CameraFocusRegionType;
import dji.common.mission.waypointv2.Action.WaypointActuator;
import dji.common.mission.waypointv2.Action.WaypointAircraftControlParam;
import dji.common.mission.waypointv2.Action.WaypointAircraftControlRotateYawParam;
import dji.common.mission.waypointv2.Action.WaypointAircraftControlStartStopFlyParam;
import dji.common.mission.waypointv2.Action.WaypointCameraActuatorParam;
import dji.common.mission.waypointv2.Action.WaypointCameraCustomNameParam;
import dji.common.mission.waypointv2.Action.WaypointCameraFocusParam;
import dji.common.mission.waypointv2.Action.WaypointCameraFocusPointTargetParam;
import dji.common.mission.waypointv2.Action.WaypointCameraZoomParam;
import dji.common.mission.waypointv2.Action.WaypointGimbalActuatorParam;
import dji.common.mission.waypointv2.Action.WaypointReachPointTriggerParam;
import dji.common.mission.waypointv2.Action.WaypointTrigger;
import dji.common.mission.waypointv2.Action.WaypointV2Action;
import dji.common.mission.waypointv2.Action.WaypointV2AssociateTriggerParam;
import dji.common.mission.waypointv2.WaypointV2;
import dji.common.mission.waypointv2.WaypointV2Mission;
import dji.common.mission.waypointv2.WaypointV2MissionTypes;
import dji.common.model.LocationCoordinate2D;
import eu.halyid.drone.util.GenericMission;

public class SharperMission extends GenericMission {

    private final List<WaypointV2> waypointV2List;
    private final List<WaypointV2Action> waypointV2ActionList;

    private List<LatLng> waypointsCoordinates;

    private final Parameters parameters;

    public SharperMission(Parameters par) {
        parameters = par;

        waypointV2List = new ArrayList<>();
        waypointV2ActionList = new ArrayList<>();
    }

//    public void createWaypointsCoordinates() {
//        waypointsCoordinates = new ArrayList<>();
//
//        waypointsCoordinates = parameters.getEndpoints();
//
//
//
//        for (int i = 0; i < parameters.getEndpoints().size()-1; i+=2) {
//            LatLng wpBegin = parameters.getEndpoints().get(i);
//            LatLng wpEnd = parameters.getEndpoints().get(i+1);
//
//            double distance = getDistanceBetweenTwoPoints(wpBegin, wpEnd);
////            int steps = (int) (missionDistance / parameters.getWpDistance()) + 1;
//
//            double azimuth = getAngleBetweenTwoPoints(wpBegin, wpEnd);
//
////            for (int j = 0; j < steps; j++) {
//            waypointsCoordinates.add(getDestinationLatLong(wpBegin, azimuth, 0));
//            waypointsCoordinates.add(getDestinationLatLong(wpBegin, azimuth, distance/2.0));
//            waypointsCoordinates.add(getDestinationLatLong(wpBegin, azimuth, distance));
////            }
//
////            // Last waypoint
////            waypointsCoordinates.add(wpEnd);
//        }
//    }

    public List<WaypointV2> getWaypointV2List() {
        return waypointV2List;
    }

    public WaypointV2Mission createWaypointMission() {
        waypointsCoordinates = parameters.getWaypoints();

        for (LatLng point : waypointsCoordinates) {
            WaypointV2 wp = Objects.requireNonNull(new WaypointV2.Builder()
                            .setAltitude(parameters.getDroneAltitude())
                            .setCoordinate(new LocationCoordinate2D(point.latitude, point.longitude)))
                    .setFlightPathMode(WaypointV2MissionTypes.WaypointV2FlightPathMode.GOTO_POINT_STRAIGHT_LINE_AND_STOP)
                    .setHeadingMode(WaypointV2MissionTypes.WaypointV2HeadingMode.AUTO)
                    .build();

            waypointV2List.add(wp);
        }

        WaypointV2Mission.Builder waypointV2MissionBuilder = new WaypointV2Mission.Builder();
        waypointV2MissionBuilder.setMissionID(new Random().nextInt(65535))
                .setMaxFlightSpeed(parameters.getDroneSpeed())
                .setAutoFlightSpeed(parameters.getDroneSpeed())
                .setFinishedAction(WaypointV2MissionTypes.MissionFinishedAction.GO_HOME)
                .setGotoFirstWaypointMode(WaypointV2MissionTypes.MissionGotoWaypointMode.SAFELY)
                .setExitMissionOnRCSignalLostEnabled(true)
                .setRepeatTimes(1)
                .addwaypoints(waypointV2List);

        return waypointV2MissionBuilder.build();
    }

    public WaypointV2Mission createRTHMission(LatLng home) {
        waypointV2List.clear();

        WaypointV2 wp = Objects.requireNonNull(new WaypointV2.Builder()
                        .setAltitude(30.0)
                        .setCoordinate(new LocationCoordinate2D(home.latitude, home.longitude)))
                .setFlightPathMode(WaypointV2MissionTypes.WaypointV2FlightPathMode.GOTO_POINT_STRAIGHT_LINE_AND_STOP)
                .setHeadingMode(WaypointV2MissionTypes.WaypointV2HeadingMode.AUTO)
                .build();

        waypointV2List.add(wp);

        wp = Objects.requireNonNull(new WaypointV2.Builder()
                        .setAltitude(30.0)
                        .setCoordinate(new LocationCoordinate2D(home.latitude, home.longitude)))
                .setFlightPathMode(WaypointV2MissionTypes.WaypointV2FlightPathMode.GOTO_POINT_STRAIGHT_LINE_AND_STOP)
                .setHeadingMode(WaypointV2MissionTypes.WaypointV2HeadingMode.AUTO)
                .build();
        waypointV2List.add(wp);

        WaypointV2Mission.Builder waypointV2MissionBuilder = new WaypointV2Mission.Builder();
        waypointV2MissionBuilder.setMissionID(new Random().nextInt(65535))
                .setMaxFlightSpeed(parameters.getDroneSpeed())
                .setAutoFlightSpeed(parameters.getDroneSpeed())
                .setFinishedAction(WaypointV2MissionTypes.MissionFinishedAction.AUTO_LAND)
                .setGotoFirstWaypointMode(WaypointV2MissionTypes.MissionGotoWaypointMode.SAFELY)
                .setExitMissionOnRCSignalLostEnabled(true)
                .setRepeatTimes(1)
                .addwaypoints(waypointV2List);

        return waypointV2MissionBuilder.build();
    }

    public double getAngleTwoPoints(LatLng point1, LatLng point2) {
        double lat1 = point1.latitude;
        double lat2 = point2.latitude;
        double long1 = point1.longitude;
        double long2 = point2.longitude;
        double deltaLong = long2 - long1;
        double angle = Math.atan2(Math.sin(deltaLong) * Math.cos(lat2), Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLong));
        return Math.toDegrees(angle);
    }

    public List<WaypointV2Action> createWaypointAction() {
        int actionId = 0;

        // Calculate bearing on a straight line (first and second point)
        // This is an approximation way in order to compute the -90/90 angle for the drone (in this case, always either -90 or 90)
        // We assume that at least two points are along a row (i.e., the first (0) and the second (1) indices)
        LatLng point1 = waypointsCoordinates.get(0);
        LatLng point2 = waypointsCoordinates.get(1);

        float bearingAngle = (float) getAngleTwoPoints(point1, point2);
        int droneYaw = parameters.getDroneYaw();
        // Adjusted yaw angle
        float adjustedDroneYaw = bearingAngle + droneYaw;
        if (adjustedDroneYaw > 180) {
            adjustedDroneYaw = adjustedDroneYaw - 360;
        }
        if (adjustedDroneYaw < -180) {
            adjustedDroneYaw = adjustedDroneYaw + 360;
        }

        for (int startIndex = 0; startIndex < waypointsCoordinates.size(); startIndex++) {
            // Do this once reached the waypoint
            WaypointTrigger waypointAction0Trigger = new WaypointTrigger.Builder()
                    .setTriggerType(ActionTypes.ActionTriggerType.REACH_POINT)
                    .setReachPointParam(new WaypointReachPointTriggerParam.Builder()
                            .setStartIndex(startIndex)
                            .setAutoTerminateCount(0)
                            .build())
                    .build();

            WaypointActuator waypointAction0Actuator = new WaypointActuator.Builder()
                    .setActuatorType(ActionTypes.ActionActuatorType.CAMERA)
                    .setCameraActuatorParam(new WaypointCameraActuatorParam.Builder()
                            .setCameraOperationType(ActionTypes.CameraOperationType.CUSTOM_NAME)
                            .setCustomNameParam(new WaypointCameraCustomNameParam.Builder()
                                    .type(ActionTypes.CameraCustomNameType.DIR)
                                    .customName("halyid")
                                    .build())
                            .build())
                    .build();

            WaypointV2Action waypointAction0 = new WaypointV2Action.Builder()
                    .setActionID(actionId++)
                    .setTrigger(waypointAction0Trigger)
                    .setActuator(waypointAction0Actuator)
                    .build();
            waypointV2ActionList.add(waypointAction0);


            // Stop the drone
            WaypointTrigger waypointActionStopTrigger = new WaypointTrigger.Builder()
                    .setTriggerType(ActionTypes.ActionTriggerType.ASSOCIATE)
                    .setAssociateParam(new WaypointV2AssociateTriggerParam.Builder()
                            .setAssociateActionID(actionId - 1)
                            .setAssociateType(ActionTypes.AssociatedTimingType.AFTER_FINISHED)
                            .setWaitingTime(0) // Doesn't affect any value here
                            .build())
                    .build();

            WaypointActuator waypointActionStopActuator = new WaypointActuator.Builder()
                    .setActuatorType(ActionTypes.ActionActuatorType.AIRCRAFT_CONTROL)
                    .setAircraftControlActuatorParam(new WaypointAircraftControlParam.Builder()
                            .setAircraftControlType(ActionTypes.AircraftControlType.START_STOP_FLY)
                            .setFlyControlParam(new WaypointAircraftControlStartStopFlyParam.Builder()
                                    .setStartFly(false)
                                    .build())
                            .build())
                    .build();

            WaypointV2Action waypointActionStop = new WaypointV2Action.Builder()
                    .setActionID(actionId++)
                    .setTrigger(waypointActionStopTrigger)
                    .setActuator(waypointActionStopActuator)
                    .build();
            waypointV2ActionList.add(waypointActionStop);

            // Generating matrix
            int[] gimbalYaws = new int[5];
            int[] gimbalPitches = new int[4];
            float[][] distances = new float[5][4];
            int[][] cameraFocals = new int[5][4];

//            float q_min = 0.7f;
            float q_max = 3.0f;

            float box_w = 1.15f;
            float box_h = 0.77f;

            // Yaws
            for (int i = 2; i < gimbalYaws.length; i++) {
                gimbalYaws[i] = (int) Math.toDegrees(Math.atan(((i-2)*box_w)/parameters.getGroundDistance()));
                gimbalYaws[gimbalYaws.length-1-i] = -gimbalYaws[i];
            }

            // Pitches
            for (int j = 0; j < gimbalPitches.length; j++) {
                gimbalPitches[j] = (int) -Math.toDegrees(Math.atan((parameters.getDroneAltitude()-(q_max-(j*box_h)))/parameters.getGroundDistance()));
            }

            // Yaws from index 2 (indices 0 and 1 are specular with 4 and 3, resp.)
            for (int i = 2; i < gimbalYaws.length; i++) {
                for (int j = 0; j < gimbalPitches.length; j++) {
                    float relative_h = parameters.getDroneAltitude()-(q_max-(j*box_h));
                    float relative_d = (float) Math.sqrt(Math.pow(parameters.getGroundDistance(), 2) + Math.pow((i-2)*box_w, 2));
                    distances[i][j] = (float) Math.sqrt(Math.pow(relative_d, 2) + Math.pow(relative_h, 2));
                    distances[gimbalYaws.length-1-i][j] = distances[i][j];
                }
            }

            // Focals
            for (int i = 0; i < gimbalYaws.length; i++) {
                for (int j = 0; j < gimbalPitches.length; j++) {
                    float tmp = (float) (9.6/((2*0.648)/distances[i][j]));
                    tmp = tmp*35.0f/7.53f;
                    tmp = tmp*10f;
                    cameraFocals[i][j] = (int) tmp;
                }
            }

            //
            for (int i = 0; i < gimbalYaws.length; i++) {
                int gimbalYaw = gimbalYaws[i];

                //
                for (int j = 0; j < gimbalPitches.length; j++) {
                    int gimbalPitch = gimbalPitches[j];
                    int cameraFocal = cameraFocals[i][j];

                    // Rotate the drone (yaw)
                    WaypointTrigger waypointActionRotateDroneTrigger = new WaypointTrigger.Builder()
                            .setTriggerType(ActionTypes.ActionTriggerType.ASSOCIATE)
                            .setAssociateParam(new WaypointV2AssociateTriggerParam.Builder()
                                    .setAssociateActionID(actionId - 1)
                                    .setAssociateType(ActionTypes.AssociatedTimingType.AFTER_FINISHED)
                                    .setWaitingTime(0)
                                    .build())
                            .build();

                    WaypointActuator waypointActionRotateDroneActuator = new WaypointActuator.Builder()
                            .setActuatorType(ActionTypes.ActionActuatorType.AIRCRAFT_CONTROL)
                            .setAircraftControlActuatorParam(new WaypointAircraftControlParam.Builder()
                                    .setAircraftControlType(ActionTypes.AircraftControlType.ROTATE_YAW)
                                    .setRotateYawParam(new WaypointAircraftControlRotateYawParam.Builder()
                                            .setYawAngle(adjustedDroneYaw)
                                            .build())
                                    .build())
                            .build();

                    WaypointV2Action waypointActionRotateDrone = new WaypointV2Action.Builder()
                            .setActionID(actionId++)
                            .setTrigger(waypointActionRotateDroneTrigger)
                            .setActuator(waypointActionRotateDroneActuator)
                            .build();
                    waypointV2ActionList.add(waypointActionRotateDrone);


                    // Rotate the gimbal (pitch+yaw)
                    WaypointTrigger waypointActionTmpTrigger = new WaypointTrigger.Builder()
                            .setTriggerType(ActionTypes.ActionTriggerType.ASSOCIATE)
                            .setAssociateParam(new WaypointV2AssociateTriggerParam.Builder()
                                    .setAssociateActionID(actionId - 1)
                                    .setAssociateType(ActionTypes.AssociatedTimingType.AFTER_FINISHED)
                                    .setWaitingTime(0) // parameters.getWaitingTime()
                                    .build())
                            .build();

                    float adjustedGimbalYaw = adjustedDroneYaw + gimbalYaw;
                    if (adjustedGimbalYaw > 180) {
                        adjustedGimbalYaw = adjustedGimbalYaw - 360;
                    }
                    if (adjustedGimbalYaw < -180) {
                        adjustedGimbalYaw = adjustedGimbalYaw + 360;
                    }

                    WaypointActuator waypointActionTmpActuator = new WaypointActuator.Builder()
                            .setActuatorType(ActionTypes.ActionActuatorType.GIMBAL)
                            .setGimbalActuatorParam(new WaypointGimbalActuatorParam.Builder()
                                    .operationType(ActionTypes.GimbalOperationType.ROTATE_GIMBAL)
                                    .rotation(new Rotation.Builder()
                                            .mode(RotationMode.ABSOLUTE_ANGLE)
                                            .pitch(gimbalPitch)
                                            .yaw(adjustedGimbalYaw)
                                            .time(1)
                                            .build())
                                    .build())
                            .build();

                    WaypointV2Action waypointActionTmp = new WaypointV2Action.Builder()
                            .setActionID(actionId++)
                            .setTrigger(waypointActionTmpTrigger)
                            .setActuator(waypointActionTmpActuator)
                            .build();
                    waypointV2ActionList.add(waypointActionTmp);

                    // Set focus
                    WaypointTrigger waypointActionFocusTrigger = new WaypointTrigger.Builder()
                            .setTriggerType(ActionTypes.ActionTriggerType.ASSOCIATE)
                            .setAssociateParam(new WaypointV2AssociateTriggerParam.Builder()
                                    .setAssociateActionID(actionId - 1)
                                    .setAssociateType(ActionTypes.AssociatedTimingType.AFTER_FINISHED)
                                    .setWaitingTime(0)
                                    .build())
                            .build();

                    WaypointActuator waypointActionFocusActuator = new WaypointActuator.Builder()
                            .setActuatorType(ActionTypes.ActionActuatorType.CAMERA)
                            .setCameraActuatorParam(new WaypointCameraActuatorParam.Builder()
                                    .setCameraOperationType(ActionTypes.CameraOperationType.FOCUS)
                                    .setFocusParam(new WaypointCameraFocusParam.Builder()
                                            .setCameraFocusRegionType(CameraFocusRegionType.POINT)
                                            .waypointCameraFocusPointTargetParam(new WaypointCameraFocusPointTargetParam.Builder()
                                                    .focusPoint(new PointF(0.5f, 0.5f))
                                                    .build())
                                            .build())
                                    .build())
                            .build();

                    WaypointV2Action waypointActionFocus = new WaypointV2Action.Builder()
                            .setActionID(actionId++)
                            .setTrigger(waypointActionFocusTrigger)
                            .setActuator(waypointActionFocusActuator)
                            .build();
                    waypointV2ActionList.add(waypointActionFocus);

                    // Set zoom
                    WaypointTrigger waypointActionZoomTrigger = new WaypointTrigger.Builder()
                            .setTriggerType(ActionTypes.ActionTriggerType.ASSOCIATE)
                            .setAssociateParam(new WaypointV2AssociateTriggerParam.Builder()
                                    .setAssociateActionID(actionId - 1)
                                    .setAssociateType(ActionTypes.AssociatedTimingType.AFTER_FINISHED)
                                    .setWaitingTime(0)
                                    .build())
                            .build();

                    WaypointActuator waypointActionZoomActuator = new WaypointActuator.Builder()
                            .setActuatorType(ActionTypes.ActionActuatorType.CAMERA)
                            .setCameraActuatorParam(new WaypointCameraActuatorParam.Builder()
                                    .setCameraOperationType(ActionTypes.CameraOperationType.ZOOM)
                                    .setZoomParam(new WaypointCameraZoomParam.Builder()
                                            .setFocalLength(cameraFocal)
                                            .build())
                                    .build())
                            .build();

                    WaypointV2Action waypointActionZoom = new WaypointV2Action.Builder()
                            .setActionID(actionId++)
                            .setTrigger(waypointActionZoomTrigger)
                            .setActuator(waypointActionZoomActuator)
                            .build();
                    waypointV2ActionList.add(waypointActionZoom);

                    // Take a photo
                    WaypointTrigger waypointActionPhotoTrigger = new WaypointTrigger.Builder()
                            .setTriggerType(ActionTypes.ActionTriggerType.ASSOCIATE)
                            .setAssociateParam(new WaypointV2AssociateTriggerParam.Builder()
                                    .setAssociateActionID(actionId - 1)
                                    .setAssociateType(ActionTypes.AssociatedTimingType.AFTER_FINISHED)
                                    .setWaitingTime(0)
                                    .build())
                            .build();

                    WaypointActuator waypointActionPhotoActuator = new WaypointActuator.Builder()
                            .setActuatorType(ActionTypes.ActionActuatorType.CAMERA)
                            .setCameraActuatorParam(new WaypointCameraActuatorParam.Builder()
                                    .setCameraOperationType(ActionTypes.CameraOperationType.SHOOT_SINGLE_PHOTO)
                                    .build())
                            .build();

                    WaypointV2Action waypointActionPhoto = new WaypointV2Action.Builder()
                            .setActionID(actionId++)
                            .setTrigger(waypointActionPhotoTrigger)
                            .setActuator(waypointActionPhotoActuator)
                            .build();
                    waypointV2ActionList.add(waypointActionPhoto);
                }
            }


            // Restart the drone
            WaypointTrigger waypointActionRestartTrigger = new WaypointTrigger.Builder()
                    .setTriggerType(ActionTypes.ActionTriggerType.ASSOCIATE)
                    .setAssociateParam(new WaypointV2AssociateTriggerParam.Builder()
                            .setAssociateActionID(actionId - 1)
                            .setAssociateType(ActionTypes.AssociatedTimingType.AFTER_FINISHED)
                            .setWaitingTime(0) // Doesn't affect any value here
                            .build())
                    .build();

            WaypointActuator waypointActionRestartActuator = new WaypointActuator.Builder()
                    .setActuatorType(ActionTypes.ActionActuatorType.AIRCRAFT_CONTROL)
                    .setAircraftControlActuatorParam(new WaypointAircraftControlParam.Builder()
                            .setAircraftControlType(ActionTypes.AircraftControlType.START_STOP_FLY)
                            .setFlyControlParam(new WaypointAircraftControlStartStopFlyParam.Builder()
                                    .setStartFly(true)
                                    .build())
                            .build())
                    .build();

            WaypointV2Action waypointActionRestart = new WaypointV2Action.Builder()
                    .setActionID(actionId++)
                    .setTrigger(waypointActionRestartTrigger)
                    .setActuator(waypointActionRestartActuator)
                    .build();
            waypointV2ActionList.add(waypointActionRestart);
        }

        return waypointV2ActionList;
    }

    public double getAngleBetweenTwoPoints(LatLng point1, LatLng point2) {
        double lat1 = point1.latitude;
        double lat2 = point2.latitude;
        double lon1 = point1.longitude;
        double lon2 = point2.longitude;

        double startLat = Math.toRadians(lat1);
        double startLong = Math.toRadians(lon1);
        double endLat = Math.toRadians(lat2);
        double endLong = Math.toRadians(lon2);
        double dLong = endLong - startLong;
        double dPhi = Math.log(Math.tan(endLat/2.0+Math.PI/4.0)/Math.tan(startLat/2.0+Math.PI/4.0));

        if (Math.abs(dLong) > Math.PI) {
            if (dLong > 0.0) {
                dLong = -(2.0 * Math.PI - dLong);
            } else {
                dLong = (2.0 * Math.PI + dLong);
            }
        }

        return (Math.toDegrees(Math.atan2(dLong, dPhi)) + 360.0) % 360.0;
    }

    private float getDistanceBetweenTwoPoints(LatLng point1, LatLng point2) {
        Location loc1 = new Location(LocationManager.GPS_PROVIDER);
        Location loc2 = new Location(LocationManager.GPS_PROVIDER);
        loc1.setLatitude(point1.latitude);
        loc1.setLongitude(point1.longitude);
        loc2.setLatitude(point2.latitude);
        loc2.setLongitude(point2.longitude);
        return loc1.distanceTo(loc2);
    }

    private LatLng getDestinationLatLong(LatLng point, double azimuth, double distance) {
        double lat = point.latitude;
        double lon = point.longitude;

        double R = 6378.1;
        double brng = Math.toRadians(azimuth);
        double d = distance/1000.;

        double lat1 = Math.toRadians(lat);
        double lon1 = Math.toRadians(lon);
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d/R) + Math.cos(lat1)* Math.sin(d/R)* Math.cos(brng));
        double lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(d/R)* Math.cos(lat1), Math.cos(d/R)- Math.sin(lat1)* Math.sin(lat2));

        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);

        return new LatLng(lat2, lon2);
    }

}
