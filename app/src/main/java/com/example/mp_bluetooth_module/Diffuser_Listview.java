package com.example.mp_bluetooth_module;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;

public class Diffuser_Listview extends AppCompatActivity {

    // Initializing variables
    private Button BackBtn, RefreshBtn;
    private TextView BluetoothTextView;
    private ListView BluetoothListView;

    // Initializing the Adapter for bluetooth
    private BluetoothAdapter BluetoothAdap = null;
    private Set Devices;
// comes in Oncreate method of the activity
    BluetoothAdap = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diffuser_listview);

        BackBtn = findViewById(R.id.Back_button);
        RefreshBtn = findViewById(R.id.Refresh_button);
        BluetoothTextView = findViewById(R.id.Diffuser_Activity_Textview);
        BluetoothListView = findViewById(R.id.Diffuser_Bluetooth_ListView);

        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnToHomePAge();
            }
        });
    }

    public void ReturnToHomePAge() {
        Intent intentHomePage = new Intent(this, MainActivity.class);
        startActivity(intentHomePage);
    }

}