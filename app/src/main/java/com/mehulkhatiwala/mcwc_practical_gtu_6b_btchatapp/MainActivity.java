package com.mehulkhatiwala.mcwc_practical_gtu_6b_btchatapp;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    //defining and initializing constant >0
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "MainActivity";

    // Creating all Java objects related to design
    private Button onBtn, offBtn, listBtn, findBtn, sendFileViaBT;
    private TextView textStatus, textListTitle;
    private ListView myListView;

    //Defining bluetooth adapter
    private BluetoothAdapter mybluetoothAdapter;

    //Defining Bluetooth array adapter for list of devices
    private DeviceListAdapter mDeviceListAdapter;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    private Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onResume() {
        super.onResume();
        if(mybluetoothAdapter.isEnabled()) {
            textStatus.setText("Status: Enabled");
            textStatus.setTextColor(getResources().getColor(R.color.colorGreen));
            onBtn.setEnabled(false);
            offBtn.setEnabled(true);
            listBtn.setEnabled(true);
            findBtn.setEnabled(true);
            sendFileViaBT.setEnabled(true);
        }
        else {
            textStatus.setText("Status: Disabled");
            textStatus.setTextColor(getResources().getColor(R.color.colorRed));
            onBtn.setEnabled(true);
            offBtn.setEnabled(false);
            listBtn.setEnabled(false);
            findBtn.setEnabled(false);
            sendFileViaBT.setEnabled(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Binding the controls
        onBtn = findViewById(R.id.btnOn);
        offBtn = findViewById(R.id.btnOff);
        listBtn = findViewById(R.id.listPairedDevices);
        findBtn = findViewById(R.id.scanNewDevices);
        sendFileViaBT = findViewById(R.id.sendFile);
        textStatus = findViewById(R.id.textViewStatus);
        myListView = findViewById(R.id.listViewDevices);
        textListTitle = findViewById(R.id.listTitle);



        mybluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mybluetoothAdapter==null){
            //Bluetooth is not supported
            textStatus.setText("Status: Not supported");
            onBtn.setEnabled(false);

            Toast.makeText(this, "Bluetooth is not supported in this device.", Toast.LENGTH_SHORT).show();
        }else{

            onBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

                }
            });
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    textStatus.setText("Status: Disabled");
                    textListTitle.setText(null);
                    textStatus.setTextColor(getResources().getColor(R.color.colorRed));
                    mybluetoothAdapter.disable();
                    onBtn.setEnabled(true);
                    offBtn.setEnabled(false);
                    listBtn.setEnabled(false);
                    findBtn.setEnabled(false);
                    sendFileViaBT.setEnabled(false);
                    Toast.makeText(MainActivity.this, "Bluetooth Disabled!", Toast.LENGTH_SHORT).show();
                }
            });
            listBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "btnPairedDevices: Looking for paired devices.");

                    mDeviceListAdapter = new DeviceListAdapter(MainActivity.this,R.layout.device_adapter_view,mBTDevices);
                    mDeviceListAdapter.clear();
                    mDeviceListAdapter.notifyDataSetChanged();

                    pairedDevices = mybluetoothAdapter.getBondedDevices();
                    textStatus.setText("Status: Paired device(s)");
                    textStatus.setTextColor(getResources().getColor(R.color.colorGreen));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        textStatus.setTooltipText("List of all Paired devices attached to your phone.");
                    }

                    textListTitle.setText("** List of Paired devices **");

                    if (pairedDevices.size() > 0) {

                        for (BluetoothDevice device : pairedDevices) {
                            mBTDevices.add(device);
                        }
                        mDeviceListAdapter = new DeviceListAdapter(MainActivity.this,R.layout.device_adapter_view,mBTDevices);

                        myListView.setAdapter(mDeviceListAdapter);

                        Toast.makeText(MainActivity.this, "Displaying Paired devices!", Toast.LENGTH_SHORT).show();
                    } else {
                        //BTArrayAdapter.add("No paired device(s) found. Nothing to list down.");
                        Toast.makeText(MainActivity.this, "No Paired device found.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            findBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

                    textStatus.setText("Status: Unpaired device(s)");
                    textStatus.setTextColor(getResources().getColor(R.color.colorGreen));

                    textListTitle.setText("** List of Unpaired devices **");
                    if (mybluetoothAdapter.isDiscovering()) {
                        mybluetoothAdapter.cancelDiscovery();
                        Log.d(TAG, "btnDiscover: Cancelling discovery.");

                        //Check BT permission in manifest file
                        checkBTPermissions();

                        mybluetoothAdapter.startDiscovery();
                        IntentFilter discoverDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                        registerReceiver(mReceiver, discoverDeviceIntent);
                    }
                    if (!mybluetoothAdapter.isDiscovering()) {

                        //check BT permissions in manifest
                        checkBTPermissions();

                        mybluetoothAdapter.startDiscovery();
                        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                        registerReceiver(mReceiver, discoverDevicesIntent);
                    }
                }
            });
        }

        sendFileViaBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,SendFileViaBluetooth.class));
            }
        });
        //Broadcast when bond state change (while pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiver1,filter);

        myListView.setOnItemClickListener(MainActivity.this);
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
                permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
                if (permissionCheck != 0) {

                    this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
                }else{
                    Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_ENABLE_BT){
            if(mybluetoothAdapter.isEnabled()) {
                textStatus.setText("Status: Enabled");
                textStatus.setTextColor(getResources().getColor(R.color.colorGreen));
                onBtn.setEnabled(false);
                offBtn.setEnabled(true);
                listBtn.setEnabled(true);
                findBtn.setEnabled(true);
                sendFileViaBT.setEnabled(true);
            }else {
                Toast.makeText(this, "User disallowed enabling the bluetooth.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            mBTDevices.clear();
            mDeviceListAdapter = new DeviceListAdapter(MainActivity.this,R.layout.device_adapter_view,mBTDevices);
            mDeviceListAdapter.notifyDataSetChanged();

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());

                mDeviceListAdapter = new DeviceListAdapter(MainActivity.this,R.layout.device_adapter_view,mBTDevices);
                myListView.setAdapter(mDeviceListAdapter);
            }
        }
    };

    private final BroadcastReceiver mReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                //3 cases
                //case 1: bonded already
                if(device.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "broadcastReceive: BOND_BONDED.");
                }
                //case 2: creating a bond
                if(device.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.d(TAG, "broadcastReceive: BOND_BONDING.");
                }
                //case 3: breaking a bond
                if(device.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "broadcastReceive: BOND_NONE.");
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unregisterReceiver(mReceiver1);
        //unregisterReceiver(mBroadcastReceiver2);
        //mBluetoothAdapter.cancelDiscovery();
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.
        mybluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(i).createBond();
        }
    }
}