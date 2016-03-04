package com.example.finkel.bikecycle;

import android.util.Log;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Finkel on 02/03/2016.
 */
public class NavigationTEMP {

   /* public void a(){
        //get directions and create list of turns
        try {
            NavigationManager dir = new NavigationManager();
            String data = "Empty string";
            data = dir.execute(LOCATION_LOC1,LOCATION_LOC2).get();
            Log.d("URL", data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }*/
    public void start(){
        // Create a scheduled thread pool with 5 core threads
        ScheduledThreadPoolExecutor sch = (ScheduledThreadPoolExecutor)
                Executors.newScheduledThreadPool(5);

        // And yet another
        Runnable periodicTask = new Runnable(){
            @Override
            public void run() {
                try{
                    //check if we passed the turn
                }catch(Exception e){

                }
            }
        };

        ScheduledFuture<?> periodicFuture = sch.scheduleAtFixedRate(periodicTask, 0, 500, TimeUnit.MILLISECONDS);
    }
}
