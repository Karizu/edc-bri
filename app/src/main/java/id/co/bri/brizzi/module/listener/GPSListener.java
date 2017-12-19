package id.co.bri.brizzi.module.listener;

import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Ahmad on 6/23/2016.
 */
public class GPSListener implements LocationListener{
    private Location GPSLocation;

    public GPSListener() {
//        Log.d("GPS", "Listener called");
    }

    public Location getGPSLocation() {
        return GPSLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
//        Log.d("GPS", "Location Changed");
        GPSLocation = location;
        if (location!=null) {
            String longitude = String.valueOf(location.getLongitude());
            String latitude = String.valueOf(location.getLatitude());
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
