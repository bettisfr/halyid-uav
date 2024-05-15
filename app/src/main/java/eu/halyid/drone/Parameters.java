package eu.halyid.drone;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Parameters {

    // Drone's parameters
    private String name;
    private float droneAltitude;
    private float droneSpeed;
    private List<LatLng> waypoints;
    private float groundDistance;
    private int droneYaw;

    private int rows;

    public int getDroneYaw() {
        return droneYaw;
    }

    public void setDroneYaw(int droneYaw) {
        this.droneYaw = droneYaw;
    }

    public void setGroundDistance(float groundDistance) {
        this.groundDistance = groundDistance;
    }

    public float getGroundDistance() {
        return groundDistance;
    }

    public float getDroneSpeed() {
        return droneSpeed;
    }

    public void setDroneSpeed(float droneSpeed) {
        this.droneSpeed = droneSpeed;
    }

    public float getDroneAltitude() {
        return droneAltitude;
    }

    public void setDroneAltitude(float droneAltitude) {
        this.droneAltitude = droneAltitude;
    }

    public List<LatLng> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<LatLng> waypoints) {
        this.waypoints = waypoints;
    }

    public Parameters() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }
}
