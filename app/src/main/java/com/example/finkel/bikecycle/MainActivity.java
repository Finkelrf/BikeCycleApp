package com.example.finkel.bikecycle;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.model.LatLng;

import java.util.concurrent.ExecutionException;



public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ViewFlipper vf;
    private BluetoothManager bm;
    private BluetoothCommunicator bc;
    private final LatLng LOCATION_LOC1 = new LatLng(48.41967,-4.47109);
    //private final LatLng LOCATION_LOC2 = new LatLng(48.39031,-4.48639);
    private final LatLng LOCATION_LOC2 = new LatLng(48.40785,-4.46358);
    private final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private final int REQUEST_ACCESS_COARSE_LOCATION = 2;
    private Intent gpsIntent;

    //config content
    private SeekBar ledRingBar;
    private TextView ledRingTxt;
    private Switch btStatusSwitch;
    private int permissionCounter = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        vf = (ViewFlipper) findViewById(R.id.viewFlipper1);
        bc = new BluetoothCommunicator();


        setupStartRunButton();

        testOnRunManager();

        startLiveDebug();

    }

    private void startNavigationTest() {
        NavigationManager dir = new NavigationManager();
        String data = "Empty string";
        try {
            data = dir.execute(new LatLng(OnRunManager.getLastLoc().getLatitude(),OnRunManager.getLastLoc().getLongitude()),LOCATION_LOC2).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    private void testOnRunManager() {
        //new OnRunManager().start();
        gpsIntent=new Intent(getBaseContext(), GpsService.class);
        startService(gpsIntent);
    }


    public void onClick_testButton(View v){
        startNavigationTest();
    }

    private void setupStartRunButton() {
        // get reference to the button
        Button messageButton = (Button) findViewById(R.id.startRunBtn);
        //set the click listener to run my code
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bc.startDemo(bm);
                // startNavigationTest();

            }

        });
    }

    private void bluetoothHandler() {
        bm = new BluetoothManager(MainActivity.this);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(vf.getDisplayedChild()!=0){
            vf.setDisplayedChild(0);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void configContentInit(){
        //Setup bluetooth connection status switch
        btStatusSwitch = (Switch) findViewById(R.id.btSwitch);
        if(bm!=null)
            btStatusSwitch.setChecked(bm.isConnected());
        else
            btStatusSwitch.setChecked(false);
        btStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    bluetoothHandler();
                    if(!bm.isConnected()) {
                        btStatusSwitch.setChecked(false);
                        Toast.makeText(MainActivity.this, "It is not possible to connect", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this,"Connected to Smart Cycle device",Toast.LENGTH_SHORT).show();
                        OnRunManager.setBtManager(bm);
                    }
                } else {
                    if(bm==null){
                        Toast.makeText(MainActivity.this,"Already disconnected",Toast.LENGTH_SHORT).show();
                    }else {
                        if(bm.isConnected()){
                            bm.close();
                        }
                        Toast.makeText(MainActivity.this,"Disconnected",Toast.LENGTH_SHORT).show();

                    }

                }

            }
        });

        //Setup Led ring bar
        ledRingBar = (SeekBar) findViewById(R.id.led_ring_bright_bar);
        ledRingTxt = (TextView) findViewById(R.id.led_ring_bright_txt);

        ledRingBar.setMax(100);
        ledRingBar.setProgress(OnRunManager.getLedRingBrightness());

        //initiate led ring bar
        ledRingTxt.setText("Led ring brightness " + ledRingBar.getProgress() + "%");

        ledRingBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                ledRingTxt.setText("Led ring brightness " + progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ledRingTxt.setText("Led ring brightness " + progress + "%");
                //send to OnRunManager
                OnRunManager.setLedRingBrightness(progress);
                //send to bike cycle device throught BT
                bc.sendLedRingPwm(progress,bm);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION:
            case REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    permissionCounter++;

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_ACCESS_FINE_LOCATION);


            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_ACCESS_COARSE_LOCATION);


            }
        }
        Log.d("DEBUG","Cheguei aqui");

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED ){
            // Handle navigation view item clicks here.
            int id = item.getItemId();

            if (id == R.id.run_info) {
                //flip layout
                vf.setDisplayedChild(1);
                runInfoHandler();
            } else if (id == R.id.nav_navigation) {
                //open MapsActivity to configure the navigation
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            } else if (id == R.id.nav_about) {

            } else if (id == R.id.nav_configurations) {
                vf.setDisplayedChild(2);
                configContentInit();
            } else if (id == R.id.nav_about) {

            }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    private void runInfoHandler() {
        //thread to update the values
        //maybe I should use AsyncTask
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //update speed
                                TextView txtViewSpeed = (TextView) findViewById(R.id.infoSpeedTxt);
                                float speed = OnRunManager.getSpeed();
                                String speedString = String.format("%.02f", speed);
                                txtViewSpeed.setText(speedString);

                                //update distance
                                TextView txtViewDist = (TextView) findViewById(R.id.infoDistanceTxt);
                                double dist = OnRunManager.getTotalDistance();
                                String distString = String.format("%.02f", dist);
                                txtViewDist.setText(distString);

                                //update distance
                                TextView txtTurn = (TextView) findViewById(R.id.infoNextTurnTxt);
                                String turnDir = NavigationManager.getNextTurn().toString();
                                txtTurn.setText(turnDir);

                                //update distance
                                TextView txtTurnDist = (TextView) findViewById(R.id.infoNextTurnDistanceTxt);
                                String turnDist = String.format("%.02f", NavigationManager.getDistanceToNextTurn());
                                txtTurnDist.setText(turnDist);

                                OnRunManager.sendNowShowingInfo();

                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();

        //TODO ARRUMAR ISSO
/*
        // Create the Handler object (on the main thread by default)
        Handler handler = new Handler();
// Define the code block to be executed
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                Log.d("Handlers", "Called on main thread");
                switch (OnRunManager.getNowShowing()){
                    case SPEED:
                        bc.sendDisplay(""+OnRunManager.getSpeed(),bm);
                        break;
                    case TOTAl_DISTANCE:
                        bc.sendDisplay(""+OnRunManager.getTotalDistance(),bm);
                        break;
                    case DIST_TO_TURN:
                        bc.sendDisplay(String.format("%.02f", NavigationManager.getDistanceToNextTurn()),bm);
                        break;
                    default:
                        break;
                }
            }
        };
// Run the above code block on the main thread after 2 seconds
        handler.postDelayed(runnableCode, 500);*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(gpsIntent);
        bm.close();

    }

    public void startLiveDebug(){
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(LiveLogger.isNewLog())
                                    Toast.makeText(MainActivity.this,LiveLogger.getLog(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();
    }
}
