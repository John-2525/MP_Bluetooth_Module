package com.example.mp_bluetooth_module;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import Background_Items.BluetoothBackground;
import Background_Items.BluetoothConnection;

public class MainActivity extends AppCompatActivity {

    /** Initializing variables and their name */

    private static final String TAG = "CheckPoint";
    private Button DiffuserBtn, ReminderBtn, ImageAlbumBtn, GamesBtn;
    private TextView Home_text;
    BluetoothBackground Service;
    boolean Bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** Finds the view by the ID given and sets it to the variable */

        Home_text = findViewById(R.id.home_textview);
        DiffuserBtn = findViewById(R.id.diffuser_button);
        ReminderBtn = findViewById(R.id.reminder_button);
        ImageAlbumBtn = findViewById(R.id.image_album_button);
        GamesBtn = findViewById(R.id.game_button);

        /** Detects if the "Diffuser Bluetooth" button is clicked */
        DiffuserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDiffuserBluetoothListView();
            }
        });

        /** Listens for presses on the Image album button */
        ImageAlbumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Service.CheckBluetoothStatus()){
                    Service.SendString("1");
                }
               else {
                    Toast.makeText(MainActivity.this,"Please connect to a bluetooth device first under Diffuser Bluetooth",Toast.LENGTH_SHORT).show();
                }
                OpenImageVideoAlbum();
            }
        });

        /** Listens for presses on the Reminder button */
        ReminderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Service.CheckBluetoothStatus()){
                    Service.SendString("2");
                }
               else {
                    Toast.makeText(MainActivity.this,"Please connect to a bluetooth device first under Diffuser Bluetooth",Toast.LENGTH_SHORT).show();
                }
               OpenReminders();
            }
        });

        /** Listens for presses on the Games button */
        GamesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Service.CheckBluetoothStatus()){
                    Service.SendString("3");
                    Service.SendString("4");
                }
                else {
                    Toast.makeText(MainActivity.this,"Please connect to a bluetooth device first under Diffuser Bluetooth",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /** This is to toggle to the Diffuser Bluetooth Activity */
    public void OpenDiffuserBluetoothListView() {
        Intent intentDiffuser = new Intent(this, Diffuser_Listview.class);
        startActivity(intentDiffuser);
    }

    /** This is to toggle to the Image Video Album Activity */
    public void OpenImageVideoAlbum() {
        Intent intentAlbum = new Intent(this, Image_Video_Album.class);
        startActivity(intentAlbum);
    }

    /** This is to toggle to the Reminders Activity */
    public void OpenReminders() {
        Intent intentReminder = new Intent(this, Display_All_Reminders.class);
        startActivity(intentReminder);
    }


    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothBackground.LocalBinder binder = (BluetoothBackground.LocalBinder) service;
            /**
             * LocalBinder provide getService() method to retrieve current instance of LocalService()
             * to allow client to call public method of this service
             */
            Service = binder.getService();
            Bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Bound = false;
        }
    };

    /**
     * Its important to unbind activities or else some errors will occur (?) due to
     * multiple services running together
     * ( Confirm this theory )
     */


    /** When activity becomes visible to the user, starts the service by binding it to this activity */
    @Override
    protected void onStart() { /** onStart() means when activity is visible to user */
        super.onStart();
        /** Starts the service by calling an instance of the service intent and starting it */
        Intent intent = new Intent(this,BluetoothBackground.class);
        startService(intent);
        /** It binds the started service to the caller of the service to allow for interaction from components */
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        Log.d(TAG,"Bound service is bounded or created");
    }

    /** When activity is not visible to the user, stops the service by unbinding it from the activity */
    @Override
    protected void onStop() {  /** onStop is when activity is not visible to user  */
        super.onStop();
        /** Unbinds the service from the caller and destroys it*/
        unbindService(connection);
        Bound = false;
        Log.d(TAG,"Bound service is unbounded or destroyed");
    }
}