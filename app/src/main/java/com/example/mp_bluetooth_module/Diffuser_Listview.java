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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import Classes.PairedBluetoothDevice;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Diffuser_Listview extends AppCompatActivity {

    // Initializing variables
    private Button BackBtn, RefreshBtn;
    private TextView BluetoothTextView;
    private ListView BluetoothListView;
    private BluetoothAdapter PhoneBluetoothAdapter;
    private BluetoothManager PhoneBluetoothManager;
    private List<PairedBluetoothDevice> PairedDeviceList;
    Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diffuser_listview);


        PhoneBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        PhoneBluetoothAdapter = PhoneBluetoothManager.getAdapter();

        BackBtn = findViewById(R.id.Back_button);
        RefreshBtn = findViewById(R.id.Refresh_button);
        BluetoothTextView = findViewById(R.id.Diffuser_Activity_Textview);
        BluetoothListView = findViewById(R.id.Diffuser_Bluetooth_ListView);

        // Initializing list of paired bluetooth devices
        pairedDevices = PhoneBluetoothAdapter.getBondedDevices();

        // Initializing new array list
        PairedDeviceList = new ArrayList();

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

    public void BluetoothListView() {
        PairedDeviceList.clear();
        PhoneBluetoothAdapter.enable();
        for(BluetoothDevice bt: pairedDevices) {
            PairedBluetoothDevice newDevice = new PairedBluetoothDevice();
            newDevice.addDevice(bt.getName(),bt.getAddress());
            PairedDeviceList.add(newDevice);
        }
        Adapter ListViewAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,PairedDeviceList);
    }

    public void RefreshBluetoothListView() {
        BluetoothListView();
        Toast.makeText(this,"Refreshed",Toast.LENGTH_SHORT).show();
    }

}

//TODO Test it out on android phone once done before moving to connection