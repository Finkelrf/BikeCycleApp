package com.example.finkel.bikecycle;

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
import java.util.List;

/**
 * Created by Finkel on 26/02/2016.
 */
public class CoordinateDownloader extends AsyncTask<Object, Integer, LatLng> {

    private String JSONData;

    public static String setUrl(String address){
        address = address.replace(' ','+');
        // Origin of route
        String parameters = "address="+address;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "http://maps.googleapis.com/maps/api/geocode/"+output+"?"+parameters;
        Log.d("COORDINATES", url);
        return url;
    }

    /** A method to download json data from url */
    public static String downloadUrl(String strUrl) throws IOException {
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
            Log.d("COORDINATES", e.toString());
        }finally{
            //iStream.close();
            //urlConnection.disconnect();
        }
        return data;
    }

    @Override
    protected LatLng doInBackground(Object... params) {
        LatLng ll = null;
        try {
            String url = setUrl(params[0].toString());
            JSONData = downloadUrl(url);
            ll = parseLatLgn(JSONData);
        } catch (IOException e) {
            Log.d("COORDINATES", "Network error");
            //e.printStackTrace();
        }
        return ll;
    }

    private LatLng parseLatLgn(String jsonData) {
        String data = "";
        try {
            JSONObject jsonRootObject = new JSONObject(jsonData);

            //Get the instance of JSONArray that contains JSONObjects
            JSONArray jsonArrayResults = jsonRootObject.optJSONArray("results");
            JSONObject jsonObjGeometry = jsonArrayResults.optJSONObject(0);
            JSONObject jsonObjGeometry2 = jsonObjGeometry.optJSONObject("geometry"); //there is somethin weird here
            JSONObject jsonObjLocation = jsonObjGeometry2.optJSONObject("location");

            Double lat = Double.parseDouble(jsonObjLocation.optString("lat").toString());
            Double lng = Double.parseDouble(jsonObjLocation.optString("lng").toString());

            data += "lat: " + lat + " lng: " + lng;
            Log.d("COORDINATES",data);
            return new LatLng(lat,lng);
        } catch (JSONException e) {
            Log.d("COORDINATES","JSON exception");
            //e.printStackTrace();
        }
        return null;
    }

   /* @Override
    protected void onPostExecute(LatLng latLng) {
        super.onPostExecute(latLng);
        //send this value to MapsActivity
        MapsActivity.setDestinationLatLng(latLng);
    }*/
}
