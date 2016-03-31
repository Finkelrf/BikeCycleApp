package com.example.finkel.bikecycle;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Finkel on 23/01/2016.
 */
public class NavigationManager extends AsyncTask<Object, Integer, String>  {
    private static final String TAG = "Directions";
    private static String JSONData = "";
    private static List<TurnPoints> turnList = new ArrayList<TurnPoints>();
    private static float lastDistanceToTurn = 9999;

    public enum TurnDirection {
        NONE,
        RIGHT,
        LEFT,
        RDB1,
        RDB2,
        RDB3,
        RDB4,
        STRAIGHT,
        BACK,
    }


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

    private static String getDirectionsUrl(LatLng origin, LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Travelling Mode
        String mode = "mode=bicycling";

        //language
        String lang = "language=en-us";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+mode+"&"+lang;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        Log.d(TAG,url);
        return url;
    }

    /** A method to download json data from url */
    private static String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            data = "cai na exeption";
            Log.d(TAG, e.toString());
        }finally{
            //iStream.close();
            //urlConnection.disconnect();
        }
        return data;
    }

    @Override
    protected String doInBackground(Object... params) {
        String data = "";
        try {
            JSONData = downloadUrl(getDirectionsUrl((LatLng)params[0],(LatLng)params[1]));
            fillTurnList();
        } catch (IOException e) {
            Log.d(TAG,"Network error");
            e.printStackTrace();
        }
        return data;
    }

    public static boolean areDirectionsKnown(){
        if(JSONData == "")
            return false;
        else
            return true;
    }

    private boolean fillTurnList(){
        DirectionsJSONParser JSONParser = new DirectionsJSONParser();
        try {
            JSONObject jObject = new JSONObject(JSONData);
            turnList = DirectionsJSONParser.parse(jObject);
            int i;
            for (i=0;i<turnList.size();i++){
                Log.d(TAG,turnList.get(i).td.toString());
            }

            return true;
        } catch (JSONException e) {
            Log.d(TAG,"Error creating JSON Object");
            return false;
        }

    }

    private static class TurnPoints {
        LatLng ll;
        TurnDirection td;
        
        public TurnPoints(LatLng ll, TurnDirection td){
            this.ll = ll;
            this.td = td;
        }

        public LatLng getLatLng(){
            return ll;
        }

        public TurnDirection getTurnDirections(){
            return td;
        }

    }

    public static TurnDirection getNextDirection(){
        if(!turnList.isEmpty()) {
            TurnPoints tp = turnList.get(0);
            turnList.remove(0);
            return tp.td;
        }else
            return TurnDirection.NONE;
    }

    public static TurnDirection getNextTurn(){
        if(turnList.isEmpty()){
            return TurnDirection.NONE;
        }else {
            return turnList.get(0).getTurnDirections();
        }
    }
    public static double getDistanceToNextTurn(){
        LatLng locTurn = getNextTurnLoc();
        float[] result=new float[5];
        if(areDirectionsKnown() && (locTurn!=null)){
            Location.distanceBetween(OnRunManager.getLastLoc().getLatitude(), OnRunManager.getLastLoc().getLongitude(), locTurn.latitude, locTurn.longitude, result);
            if(result[0] > lastDistanceToTurn){
                //turn point may be passed
                if(lastDistanceToTurn>OnRunManager.getLastLoc().getAccuracy()){
                    //TODO recalculate
                    LiveLogger.setLog("You must recalculate route");
                    return 0.0;
                }else{
                    //turn point has been passed
                    lastDistanceToTurn = 99999;
                    return result[0]*-1;
                }
            }else{
                //turn point has not been passed
                lastDistanceToTurn = result[0];
                return result[0];
            }
        }else{
            return 0.0;
        }
    }

    private static LatLng getNextTurnLoc(){
        if(!turnList.isEmpty()){
            return turnList.get(turnList.size()-1).getLatLng();
        }else{
            return null;
        }
    }

    private static class DirectionsJSONParser {

        private static final String JSON_TAG = "JSON";
        private static final CharSequence RIGHT_STRING = "right";
        private static final CharSequence LEFT_STRING = "left";
        private static final CharSequence ROUNDABOUT = "roundabout";

        /**
         * Receives a JSONObject and returns a list of lists containing latitude and longitude
         */
        public static List<TurnPoints> parse(JSONObject jObject) {

            List<TurnPoints> turnPoints = new ArrayList<TurnPoints>();
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;

            try {

                jRoutes = jObject.getJSONArray("routes");

                /** Traversing all routes */
                for(int i=0;i<jRoutes.length();i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList<HashMap<String, String>>();

                    /** Traversing all legs */
                    for (int j = 0; j < jLegs.length(); j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                        /** Traversing all steps */
                        for (int k = 0; k < jSteps.length(); k++) {
                            double lat = (double) ((JSONObject) ((JSONObject) jSteps.get(k)).get("start_location")).get("lat");
                            double lng = (double) ((JSONObject) ((JSONObject) jSteps.get(k)).get("start_location")).get("lng");
                            String inst = (String) ((JSONObject) jSteps.get(k)).get("html_instructions");

                            //check if inst is left or right
                            TurnDirection td = TurnDirection.NONE;
                            if (inst.contains(RIGHT_STRING)) {
                                td = TurnDirection.RIGHT;
                            }else if(inst.contains(LEFT_STRING)){
                                td = TurnDirection.LEFT;
                            }else if(inst.contains(ROUNDABOUT)){
                                if(inst.contains("1")){
                                    td = TurnDirection.RDB1;
                                }else if(inst.contains("2")){
                                    td = TurnDirection.RDB2;
                                }else if(inst.contains("3")){
                                    td = TurnDirection.RDB3;
                                }else if(inst.contains("4")){
                                    td = TurnDirection.RDB4;
                                }else{
                                    td = TurnDirection.NONE;
                                }
                            }else{
                                td = TurnDirection.STRAIGHT;
                            }

                            turnPoints.add(new TurnPoints(new LatLng(lat, lng), td));
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
            }
            return turnPoints;
        }
    }

}
