package android.HH100.Control;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class GpsInfo2 {

    private final Context mContext;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean isGetLocation = false;
    Location location;
    double lat = 0;
    double lon = 0;
    Location mLocation;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;

    private static final long MIN_TIME_BW_UPDATES = 0;

    protected LocationManager locationManager;

    public GpsInfo2(Context context) {
        this.mContext = context;
        ExcuteGps();
    }

    public Location ExcuteGps() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
        mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        return mLocation;
    }

    public double GetLat() {

        return lat;
    }

    public double GetLon() {

        return lon;
    }

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            lat = location.getLatitude();
            lon = location.getLongitude();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        public void onProviderEnabled(String provider) {

        }

        public void onProviderDisabled(String provider) {

        }
    };

}
