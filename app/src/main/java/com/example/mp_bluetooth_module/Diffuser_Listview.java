package com.example.mp_bluetooth_module;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;

import Classes.BlueToothListViewAdapter;
import Classes.PairedBluetoothDevice;

public class Diffuser_Listview extends AppCompatActivity {

    // Initializing variables
    private Button BackBtn, RefreshBtn;
    private ListView DeviceListView;
    BluetoothAdapter PhoneBluetoothAdapter;
    BluetoothManager PhoneBluetoothManager;
    ArrayList<PairedBluetoothDevice> PairedDeviceList;
    Set<BluetoothDevice> pairedDevices;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diffuser_listview);

        // getSystemService() is used to access android system level services, bluetooth in this case
        PhoneBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        // Gets the bluetooth adapter of the device
        PhoneBluetoothAdapter = PhoneBluetoothManager.getAdapter();


        // Find the buttons and listview
        BackBtn = findViewById(R.id.Back_button);
        RefreshBtn = findViewById(R.id.Refresh_button);
        DeviceListView = findViewById(R.id.BluetoothListView);

        // Initializing new array list
        PairedDeviceList = new ArrayList<>();

        // Checks if bluetooth has been enabled
        if (!PhoneBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activityResultLauncher.launch(enableBtIntent);
            // Sends a small message to trigger refresh button to get bluetooth list
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
    }

    // Function is to return to main page
    public void ReturnToHomePage() {
        Intent intentHomePage = new Intent(this, MainActivity.class);
        startActivity(intentHomePage);
    }

    // Function to initialize and load the list view when coming to this page
    public void BluetoothListView() {
        // Initializing list of paired bluetooth devices
        pairedDevices = PhoneBluetoothAdapter.getBondedDevices();
        PairedDeviceList.clear();
        for(BluetoothDevice bt: pairedDevices) {
            PairedBluetoothDevice newDevice = new PairedBluetoothDevice();
            newDevice.addDevice(bt.getName(), bt.getAddress());
            PairedDeviceList.add(newDevice);
        }

        // Custom Listview adapter for PairedBluetoothDevice class
        BlueToothListViewAdapter Adapter = new BlueToothListViewAdapter(this, R.layout.activity_bluetooth_device_text_view, PairedDeviceList);
        Adapter.notifyDataSetChanged();
        DeviceListView.setAdapter(Adapter);
    }

}
//TODO Find way to connect to bluetooth when touching one of the address from listview