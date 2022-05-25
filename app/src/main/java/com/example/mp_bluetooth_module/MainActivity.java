package com.example.mp_bluetooth_module;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView Home_text = findViewById(R.id.home_textview);
        Button DiffuserBtn = findViewById(R.id.diffuser_button);
        Button ReminderBtn = findViewById(R.id.reminder_button);
        Button ImageAlbumBtn = findViewById(R.id.image_album_button);
        Button GamesBtn = findViewById(R.id.game_button);


        BluetoothAdapter Diffuser_Bluetooth = BluetoothAdapter.getDefaultAdapter();
        // To get MAC address : System.out.println(Diffuser_Bluetooth.getBondedDevices());

    }

    public void diffuserButtonClicked(View view) {
        //TODO Choose HC-05 from here
    }

    public void reminderButtonClicked(View view) {
        //TODO Insert Reminder Module
        //TODO Toggle Diffuser 1
    }

    public void imageAlbumButtonClicked(View view) {
        //TODO Insert Image Album Module
        //TODO Toggle Diffuser 2 and 3
    }

    public void gameButtonClicked(View view) {
        //TODO Insert Game Module
        //TODO Toggle Diffuser 4
    }
}