package com.example.finkel.bikecycle;

import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final LatLng LOCATION_LOC1 = new LatLng(48.41967, -4.47109);
    private final LatLng LOCATION_LOC2 = new LatLng(48.39031,-4.48639);
    private final double DEFAULT_LATITUDE = 0;
    private final double DEFAULT_LOGITUDE = 0;
    private final long UPDATE_DELAY = 1000;
    private final float MINIMUM_DISTANCE = 0;

    private LatLng realLoc = new LatLng(DEFAULT_LATITUDE,DEFAULT_LOGITUDE);
    LocationManager mLocationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setupMaps();
    }

    private void setupMaps() {
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                //Got the location!
                realLoc = new LatLng(location.getLatitude(),location.getLongitude());
                Toast.makeText(MapsActivity.this, "Achou Loc", Toast.LENGTH_SHORT).show();
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(realLoc,14);
                mMap.animateCamera(update);
            }
        };
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(this, locationResult);

    }

    public void onClick_Loc1(View v){
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_LOC1, 16);
        mMap.animateCamera(update);
        mMap.addMarker(new MarkerOptions().position(LOCATION_LOC1));
    }

    public void onClick_Loc2(View v){
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_LOC2,14);
        mMap.animateCamera(update);
        mMap.addMarker(new MarkerOptions().position(LOCATION_LOC2));
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        // Add a marker in Sydney and move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(realLoc));
    }
}
