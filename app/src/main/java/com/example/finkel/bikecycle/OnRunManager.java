package com.example.finkel.bikecycle;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Finkel on 28/02/2016.
 */
public class OnRunManager  {
    private static List<Location> locList = new ArrayList<>();
    private static double distTotal = 0;
    private static Location lastLocation;
    private static int dataRetriveMode = 0; //0 = GPS, 1 = bluetooth
    final static DateFormat fmt = DateFormat.getTimeInstance(DateFormat.LONG);
    private static int LedRingBrightness = 60;
    private static BluetoothManager bm;
    private static BluetoothCommunicator bc;


    private static INFO nowShowing = INFO.SPEED;
    public enum INFO{
        SPEED,
        TOTAl_DISTANCE,
        DIST_TO_TURN,
    }

    public static INFO getNowShowing() {
        return nowShowing;
    }

    public static void setNowShowing(INFO nowShowing) {
        OnRunManager.nowShowing = nowShowing;
    }


    public static void setBtManager(BluetoothManager bman){
        bm = bman;
        bc = new BluetoothCommunicator();
    }



    public static int getLedRingBrightness() {
        return LedRingBrightness;
    }

    public static void setLedRingBrightness(int ledRingBrightness) {
        LedRingBrightness = ledRingBrightness;
    }



    public static void setLastLocation(Location l){
        lastLocation = l;
    }
    public static boolean isLocListEmpty(){
        return locList.isEmpty();
    }


    public static Location getLastLoc(){
        if(!locList.isEmpty()){
            return locList.get(locList.size()-1);
        }else{
            return null;
        }
    }

    public static void addOnLocList(Location ll){
        locList.add(ll);
        if (dataRetriveMode == 0) {
            int size = locList.size();
            if (size > 2) {
                double dist = locList.get(size - 1).distanceTo(locList.get(size - 2));
                addToTotalDistance(dist);
            }
        }
    }

    private static void addToTotalDistance(double dist){
        distTotal+=dist;
    }

    public static float getSpeed(){
        float retSpeed=0;
        switch (dataRetriveMode) {
            case 0:
                if (lastLocation != null) {
                    try {
                        retSpeed = lastLocation.getSpeed();
                    }catch (Exception e){
                        //error did not find speed
                        retSpeed=0;
                    }
                } else {
                    retSpeed = 0;
                }
                break;
            case 1:
                //// TODO: 02/03/2016
                retSpeed = 0;
                break;
        }
        return retSpeed;
    }

    public static double getTotalDistance() {
        return distTotal;
    }

    public static void sendNowShowingInfo(){
        if(bc != null) {
            switch (nowShowing) {
                case DIST_TO_TURN:
                    bc.sendDisplay("" + NavigationManager.getDistanceToNextTurn(), bm);
                    break;
                case TOTAl_DISTANCE:
                    bc.sendDisplay("" + OnRunManager.getTotalDistance(), bm);
                    break;
                case SPEED:
                    bc.sendDisplay("" + OnRunManager.getSpeed(), bm);
                    break;
            }
            bc.sendDirection(NavigationManager.getNextTurn().toString(),NavigationManager.getDistanceToNextTurn(),bm);
        }
    }

}
