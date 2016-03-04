package com.example.finkel.bikecycle;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Finkel on 28/02/2016.
 */
public class BluetoothManager {
    private static final String BT_TARGET_DEV_NAME = "HC-06";
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Context c;
    private List<String> mArrayAdapter = new ArrayList<>();
    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                Log.d("BT",device.getName()+" "+device.getAddress());
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d("BT","Discovery Finished!");
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d("BT","Discovery Started!");
            }
        }
    };


    public BluetoothManager(Context context){
        this.c = context;
    }

    public void init(){
        Log.d("BT","Chamou o init!");
        if (mBluetoothAdapter == null) {
            Toast.makeText(c, "This device does not have Bluetooth", Toast.LENGTH_SHORT).show();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity)c).startActivityForResult(enableBtIntent, 1);
        }
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        ((Activity)c).registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        ((Activity)c).registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        ((Activity)c).registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
    }

    public void finalize(){
        ((Activity)c).unregisterReceiver(mReceiver);
    }

    public void connectToDevTarget() {
        Log.d("BT","Chamou o connect to de target!");
        String btTargetName = "";
        String btTargetAddress = "";
        //search default device
        for (int i=0;i<mArrayAdapter.size();i++){
            String[] adapterInfo = mArrayAdapter.get(i).split("\n");
            if(adapterInfo[0] == BT_TARGET_DEV_NAME){
                btTargetName = adapterInfo[0];
                btTargetAddress = adapterInfo[1];
                Log.d("BT",btTargetName+" "+btTargetAddress);
            }else{
                Log.d("BT","nao é esse");
            }
        }
    }



}
