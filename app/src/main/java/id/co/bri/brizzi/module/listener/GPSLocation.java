package id.co.bri.brizzi.module.listener;

import android.location.Location;

/**
 * Created by Ahmad on 6/29/2016.
 */
public class GPSLocation {
    private Location location;

    public GPSLocation() {

    }

    public void setLocation(Location location) {
        this.location=location;
    }

    public Location getLocation() {
        return location;
    }
}
