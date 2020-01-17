package com.example.soundclipalpha2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;                         //Enable bluetooth request variable
    private static final int REQUEST_DISCOVERABILITY = 1;                  //Make bluetooth discoverable request variable

    private BluetoothAdapter btAdapter;                                     //Define bluetooth adapter variable

    ArrayList<String> deviceList = new ArrayList<>();                       //Store paired devices list
    ArrayList<String> newDevices = new ArrayList<>();                       //Store available devices list
    ArrayList<String> allDevices = new ArrayList<>();                       //Store all devices for display on screen

    ListView btList;                                                        //Define list UI item


    @Override
    protected void onCreate(Bundle savedInstanceState) {                    //On activity creation do the following
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);                             //Pre-written -- to set layout as screen UI

        btList = (ListView) findViewById(R.id.list1);                        //Set list variable to UI item in layout
        btAdapter = BluetoothAdapter.getDefaultAdapter();                   //Initialize adapter

        if (allDevices != null) {
            allDevices.clear();                                             //Clear array to display fresh list
        }

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);             //Ask for permission to turn on location
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                pairedList();                                              //If user has enabled bluetooth, show list of paired devices
            }
        }
    }


    public void discoverOn() {                                              //Method for setting bluetooth on discoverable mode
        //for 5 minutes
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, REQUEST_DISCOVERABILITY);
    }


    public void pairedList() {                                                //Method gets list of paired devices
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        if (deviceList != null) {                                            //Clear older list items
            deviceList.clear();
        }

        if (pairedDevices != null) {
            for (BluetoothDevice bt : pairedDevices) {
                deviceList.add(bt.getName() + '\n' + bt.getAddress());      //Add current items
            }
        }

        Toast.makeText(getApplicationContext(), "Showing Paired Devices", Toast.LENGTH_SHORT).show();
        allDevices.addAll(deviceList);                                      //Update final display list
        showList();                                                         //Display list on screen
    }


    //Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Discovery has found a device. Get the BluetoothDevice
                //object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (newDevices != null) {
                    newDevices.clear();                                     //Clear earlier list
                }

                if (device != null) {
                    newDevices.add(device.getName() + '\n' + device.getAddress());
                    allDevices.addAll(newDevices);                          //Update new devices in final display list
                    showList();                                             //Display list
                }
            }
        }
    };

    @Override
    protected void onDestroy() {                                           //When application is closed,
        super.onDestroy();
        //Unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }


    public void showList() {                                                 //Method to set device list to ListView UI
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allDevices);
        btList.setAdapter(adapter);
    }


    // BUTTON 'CONNECT TO BLUETOOTH'
    public void btOn(View view) {                                           //Bluetooth On event
        if (btAdapter != null) {
            if (!btAdapter.isEnabled()) {                                  //Enable bluetooth if not enables already
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {                                                          //If already enabled display list of paired devices
                pairedList();
            }
        }
    }


    // BUTTON 'SCAN FOR NEW DEVICES'
    public void scanNewDevices(View view) {                                  //Scan for unpaired available bluetooth devices when "scan button" is clicked
        discoverOn();                                                       //Set bluetooth to discoverable

        if (btAdapter.isDiscovering()) {
            //Bluetooth is already in mode discovery mode, we cancel to restart it again
            btAdapter.cancelDiscovery();
        }

        btAdapter.startDiscovery();                                         //Start scanning
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }
}
