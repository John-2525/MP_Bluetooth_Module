package com.example.mp_bluetooth_module;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Initializing variables and their name
    private Button DiffuserBtn, ReminderBtn, ImageAlbumBtn, GamesBtn;
    private TextView Home_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Finds the view by the ID given
        Home_text = findViewById(R.id.home_textview);
        DiffuserBtn = findViewById(R.id.diffuser_button);
        ReminderBtn = findViewById(R.id.reminder_button);
        ImageAlbumBtn = findViewById(R.id.image_album_button);
        GamesBtn = findViewById(R.id.game_button);

        // Detects if the "Diffuser Bluetooth" button is clicked
        DiffuserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDiffuserBLuetoothListView();
            }
        });


    }

    //TODO Do the below for the remaining buttons

    // This is to toggle to the Diffuser Bluetooth Activity
    public void OpenDiffuserBLuetoothListView() {
        Intent intentDiffuser = new Intent(this, Diffuser_Listview.class);
        startActivity(intentDiffuser);
    }
}