package com.example.mp_bluetooth_module;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import Classes.BlueToothListViewAdapter;
import Classes.PairedBluetoothDevice;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Diffuser_Listview extends AppCompatActivity {

    // Initializing variables
    private Button BackBtn, RefreshBtn;
    private TextView BluetoothTextView;
    private ListView DeviceListView;
    BluetoothAdapter PhoneBluetoothAdapter;
    BluetoothManager PhoneBluetoothManager;
    ArrayList<PairedBluetoothDevice> PairedDeviceList;
    Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diffuser_listview);

        PhoneBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        PhoneBluetoothAdapter = PhoneBluetoothManager.getAdapter();
        PhoneBluetoothAdapter.enable();

        BackBtn = findViewById(R.id.Back_button);
        RefreshBtn = findViewById(R.id.Refresh_button);
        BluetoothTextView = findViewById(R.id.Diffuser_Activity_Textview);
        DeviceListView = findViewById(R.id.BluetoothListView);


        // Initializing list of paired bluetooth devices
        pairedDevices = PhoneBluetoothAdapter.getBondedDevices();

        // Initializing new array list
        PairedDeviceList = new ArrayList<>();

        // Function to initialize and load the list view when coming to this page
        BluetoothListView();

        // Listens and runs the function below if back btn is clicked
        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnToHomePage();
            }
        });

        // Listens and refreshes the list view when clicked
        RefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RefreshBluetoothListView();
            }
        });

    }

    public void ReturnToHomePage() {
        Intent intentHomePage = new Intent(this, MainActivity.class);
        startActivity(intentHomePage);
    }

    // Function to populate ListView
    public void BluetoothListView() {
        PairedDeviceList.clear();
        for(BluetoothDevice bt: pairedDevices) {
            PairedBluetoothDevice newDevice = new PairedBluetoothDevice();
            newDevice.addDevice(bt.getName(), bt.getAddress());
            PairedDeviceList.add(newDevice);
       }

        // Custom Listview adapter for PairedBluetoothDevice class
        BlueToothListViewAdapter Adapter = new BlueToothListViewAdapter(this, R.layout.activity_bluetooth_device_text_view, PairedDeviceList);
        DeviceListView.setAdapter(Adapter);
    }

    public void RefreshBluetoothListView() {
        BluetoothListView();
        Toast.makeText(this,"Refreshed",Toast.LENGTH_SHORT).show();
    }

}
// TODO Find way to connect to bluetooth when touching one of the address from listview