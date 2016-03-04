package com.example.finkel.bikecycle;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.model.LatLng;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ViewFlipper vf;
    private BluetoothManager bm;
    private final LatLng LOCATION_LOC1 = new LatLng(48.41967,-4.47109);
    //private final LatLng LOCATION_LOC2 = new LatLng(48.39031,-4.48639);
    private final LatLng LOCATION_LOC2 = new LatLng(48.40785,-4.46358);
    private Intent gpsIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        vf = (ViewFlipper) findViewById(R.id.viewFlipper1);

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
              /* bluetoothHandler();*/
                startNavigationTest();

            }

        });
    }

    private void bluetoothHandler() {
        bm = new BluetoothManager(MainActivity.this);
        bm.init();
        bm.connectToDevTarget();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            //flip layout
            vf.setDisplayedChild(1);
            runInfoHandler();
        } else if (id == R.id.nav_navigation) {
            //open MapsActivity to configure the navigation
            startActivity(new Intent(MainActivity.this, MapsActivity.class));
        } else if (id == R.id.nav_about) {

        } else if (id == R.id.nav_configurations) {

        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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

                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();



        //get nex direction and show in the screen
        /*ImageView img= (ImageView) findViewById(R.id.arrowImageViwer);
        switch (NavigationManager.getNextDirection()){
            case NONE:
                img.setImageResource(R.drawable.redx);
                break;
            case LEFT:
                img.setImageResource(R.drawable.leftarrow);
                break;
            case RIGHT:
                img.setImageResource(R.drawable.rightarrow);
                break;
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(gpsIntent);
        //bm.finalize();

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
