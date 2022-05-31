package com.example.mp_bluetooth_module;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import Background_Items.BluetoothConnection;
import Background_Items.BluetoothDisconnect;
import Classes.BlueToothListViewAdapter;
import Classes.PairedBluetoothDevice;

public class Diffuser_Listview extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private static final String TAG = "Debug";
    // Initializing variables name
    private Button BackBtn, RefreshBtn;
    private ListView DeviceListView;
    BluetoothAdapter PhoneBluetoothAdapter;
    BluetoothManager PhoneBluetoothManager;
    BluetoothSocket DeviceBluetoothSocket;
    ArrayList<PairedBluetoothDevice> PairedDeviceList;
    ArrayList<BluetoothDevice> Device_List;
    Set<BluetoothDevice> pairedDevices;
    PairedBluetoothDevice DeviceInfo;
    BluetoothDevice Device;
    String ConnectName, ConnectAddress;
    ParcelUuid[] Device_ParcelUUID_List;
    UUID SpecificDeviceUUID;
    boolean BluetoothConnectToggle = false;
    BluetoothConnection Connection;
    BluetoothDisconnect Disconnect;


    // A launcher for a previously-prepared call to start the process of executing an ActivityResultContract ~ Android Studio Documentation
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.e("Activity result","OK");
                    // There are no request codes
                    Intent data = result.getData();
                }
            });


    // Code to run when user first comes to this activity or when app is restart
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diffuser_listview);

        // getSystemService() is used to access android system level services (bluetooth in this case)
        PhoneBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        // Gets the bluetooth adapter of the device
        PhoneBluetoothAdapter = PhoneBluetoothManager.getAdapter();


        // Find the buttons and listview
        BackBtn = findViewById(R.id.Back_button);
        RefreshBtn = findViewById(R.id.Refresh_button);
        DeviceListView = findViewById(R.id.BluetoothListView);

        // Initializing new array list
        PairedDeviceList = new ArrayList<>();
        Device_List = new ArrayList<>();

        // Initializing variables used to store data
        DeviceInfo = null;      // Stores an instance of PairedBluetoothDevice class
        Device = null;      // Stores info about bluetooth device
        ConnectAddress = ConnectName = null;        // Stores string of device name and address
        DeviceBluetoothSocket = null;       // Stores socket used to connect to bluetooth device

        // Checks if bluetooth has been enabled
        if (!PhoneBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activityResultLauncher.launch(enableBtIntent);
            // Sends a small message to tell users to trigger refresh button to get bluetooth list
            Toast.makeText(this,"Please press refresh once bluetooth is enabled",Toast.LENGTH_SHORT).show();
            }

        // Refer below
        BluetoothListView();

        // Listens and runs the function below if back btn is clicked
        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnToHomePage();
            }
        });

        // Listens and runs the function below if refresh btn is clicked
        RefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothListView();
            }
        });

        // Listens and runs function below if item on listview is clicked
        DeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ConnectName and ConnectAddress gets name and address from selected index of array
                // Gets specific ParcelUUID and converts it to UUID

                SpecificDeviceUUID = Device_ParcelUUID_List[position].getUuid();
                Device = Device_List.get(position);
                if (Device != null && SpecificDeviceUUID != null) {
                    Log.d(TAG, "Converted ParcelUUID to UUID and set it to a variable");
                    showConnectPopUpMenu(view);
                }
            }
        });
    }



    // Not sure how true it is, but try not to call lifecycle methods like onResume(), onDestroy(), ...
    // As it will be calling a super of the method which makes the system think that it is another occurrence of the same method
    // Check here : https://stackoverflow.com/questions/15658687/how-to-use-onresume


    // Function is to return to main page
    public void ReturnToHomePage() {
        Intent intentHomePage = new Intent(this, MainActivity.class);
        startActivity(intentHomePage);
    }


    // Function to initialize and load the list view when diffuser bluetooth button is toggled
    public void BluetoothListView() {
        // Initializing list of paired bluetooth devices
        pairedDevices = PhoneBluetoothAdapter.getBondedDevices();
        PairedDeviceList.clear();
        for(BluetoothDevice bt: pairedDevices) {
            Device_List.add(bt);
            PairedBluetoothDevice newDevice = new PairedBluetoothDevice();
            newDevice.addDevice(bt.getName(), bt.getAddress());
            // Gets a list of ParcelUUID
            Device_ParcelUUID_List = bt.getUuids();
            Log.d(TAG, "Got List of ParcelUUID");
            PairedDeviceList.add(newDevice);
        }

        // Custom Listview adapter for PairedBluetoothDevice class
        BlueToothListViewAdapter Adapter = new BlueToothListViewAdapter(this, R.layout.activity_bluetooth_device_text_view, PairedDeviceList);
        // Notify the adapter that the dataset has been changed so the view needs to reflect the changes as well
        Adapter.notifyDataSetChanged();
        DeviceListView.setAdapter(Adapter);
    }


    // showConnectPopUpMenu and onMenuItemClick is based on https://www.youtube.com/watch?v=s1fW7CpiB9c

    // Displays pop up menu
    public void showConnectPopUpMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.bluetooth_connect_popup_menu);
        popup.show();
    }


    // Below is the implementation of OnMenuItemClickListener from PopupMenu.OnMenuItemClickListener at public class
    @Override
    // Toggles bluetooth connection based on which menu item clicked
    //TODO Problem lies in socket becoming null
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.Connect:
                // Connects to selected bluetooth device
                Connection = new BluetoothConnection(Device,SpecificDeviceUUID);
                Connection.start();

                Toast.makeText(Diffuser_Listview.this,"Connected",Toast.LENGTH_SHORT).show();
                return true;

            case R.id.Disconnect:
                //TODO Disconnect from bluetooth here
                Connection.Cancel();
                Toast.makeText(Diffuser_Listview.this,"Disconnected",Toast.LENGTH_SHORT).show();
                return true;

            default:
                return false;
        }
    }
}