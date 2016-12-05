package com.example.finkel.bikecycle;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GpsService extends Service
{
    private static final String TAG = "SERVICE";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 500;
    private static final float LOCATION_DISTANCE = 0;
    private List<LatLng> latLngList = new ArrayList<>();

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.d(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.d(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            OnRunManager.setLastLocation(location);
            if(OnRunManager.getDestination() != null) {
                float arrowPos = OnRunManager.getLastLoc().bearingTo(OnRunManager.getDestination());
                //OnRunManager.setArrowPosition();
                Log.d("DEBUG", "" + arrowPos);
            }


            if(!OnRunManager.isLocListEmpty()){
                //check if the distance between points is greater than a threshold (accuracy in this case)
                if(location.distanceTo(OnRunManager.getLastLoc())>location.getAccuracy()){
                    OnRunManager.addOnLocList(new Location(location));
                    //update distance to turn, if navigation ON
                    double distNextTurn = NavigationManager.getDistanceToNextTurn();
                    if(distNextTurn>0){
                        //did not passed turn point
                        //update distance to BT and UI
                    }else if(distNextTurn<0){
                        //passed turn point
                        NavigationManager.getNextDirection();
                        //update distance and new turn to BT and UI
                        //Actually UI updates by itself
                    }
                    LiveLogger.setLog("New Location");

                    //update loc on map

                }
            }else{
                //set the first location
                OnRunManager.addOnLocList(new Location(location));
                LiveLogger.setLog("First Location");
            }


        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.d(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.d(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.d(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        Log.d(TAG,"On Bind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.d(TAG, "onCreate");
        initializeLocationManager();
        //Mixing the providers rresults in errors of distance mesurement
        /*try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }*/
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.d(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}