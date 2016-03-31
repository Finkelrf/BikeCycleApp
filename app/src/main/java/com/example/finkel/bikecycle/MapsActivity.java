package com.example.finkel.bikecycle;

import android.content.Context;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    //private final LatLng LOCATION_LOC1 = new LatLng(48.41967, -4.47109);
    //private final LatLng LOCATION_LOC2 = new LatLng(48.39031,-4.48639);
    private final double DEFAULT_LATITUDE = 0;
    private final double DEFAULT_LOGITUDE = 0;
    private final long UPDATE_INTERVAL = 500;
    private final float MINIMUM_DISTANCE = 0;
    private static LatLng destination;
    LocationManager mLocationManager;
    Handler positionHandler = new Handler();
    Runnable updatePosition = new Runnable() {
        @Override
        public void run() {
            updateLocOnMap();
            positionHandler.postDelayed(this, UPDATE_INTERVAL);
        }
    };


    private EditText et;

    /*public static void setDestinationLatLng(LatLng ll){
        destination = ll;
    }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                MapsActivity.class));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //listen to destination address
        listenToDestinationAddres();
        positionHandler.postDelayed(updatePosition, UPDATE_INTERVAL);
    }

    //catch when user press enter in the Text field
    private void enterHandler() {
        et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    // the user is done typing.
                    // search for directions
                    //get latLng of the detination
                    try {
                        CoordinateDownloader c = new CoordinateDownloader();
                        destination = c.execute(et.getText()).get();

                        if (destination != null) {
                            //show destination in map
                            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(destination, 16);
                            mMap.animateCamera(update);
                            mMap.addMarker(new MarkerOptions()
                                    .position(destination)
                                    .title("Destination"));

                            //get directions and create list of turns

                            NavigationManager dir = new NavigationManager();
                            String data = "Empty string";
                            data = dir.execute(new LatLng(OnRunManager.getLastLoc().getLatitude(), OnRunManager.getLastLoc().getLongitude()), destination).get();
                            Log.d("URL", data);
                        } else {
                            Toast.makeText(MapsActivity.this, "Fail to find destinaiton", Toast.LENGTH_SHORT);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }


                    keyCode = 0;
                }
                return false;
            }
        });
    }
    private void listenToDestinationAddres() {
        et = (EditText)findViewById(R.id.destinationAddress);
        enterHandler();
        setFocusListener();
    }

    private void setFocusListener() {
        et.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                et.setText(" ");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
                return true;
            }
        });
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
        //show blue circle in my position
        mMap.setMyLocationEnabled(true);
        //move camera to my position
        if(!OnRunManager.isLocListEmpty()) {
            LatLng realLoc = new LatLng(OnRunManager.getLastLoc().getLatitude(),OnRunManager.getLastLoc().getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(realLoc));
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(realLoc,14);
            mMap.animateCamera(update);
        }
    }
    public void updateLocOnMap(){
        //TODO this callback is not used for now
        /*LatLng realLoc = new LatLng(OnRunManager.getLastLoc().getLatitude(),OnRunManager.getLastLoc().getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(realLoc));
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(realLoc,14);
        mMap.animateCamera(update);*/
    }

    @Override
    protected void onDestroy() {
        positionHandler.removeCallbacks(updatePosition);
        super.onDestroy();

    }
}
