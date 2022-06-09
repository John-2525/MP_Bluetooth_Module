package com.example.mp_bluetooth_module;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.example.mp_bluetooth_module.ui.main.SectionsPagerAdapter;
import com.example.mp_bluetooth_module.databinding.ActivityImageVideoAlbumBinding;

import Background_Items.BluetoothBackground;

public class Image_Video_Album extends AppCompatActivity {

    private ActivityImageVideoAlbumBinding binding;
    private FloatingActionButton UploadFab, BackFab;
    BluetoothBackground Service;
    boolean Bound = false;
    private static final String TAG = "CheckPoint";
    private CountDownTimer InterrputTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

     binding = ActivityImageVideoAlbumBinding.inflate(getLayoutInflater());
     setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        UploadFab = binding.UploadFab;
        BackFab = binding.BackFab;

        UploadFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent uploadIntent = new Intent(Image_Video_Album.this,Upload_File_Firebase.class);
                startActivity(uploadIntent);
            }
        });

        BackFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Service.SendString("1");
                Intent mainIntent = new Intent(Image_Video_Album.this, MainActivity.class);
                startActivity(mainIntent);
            }
        });
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

        /**
         * Count down Timer is to prevent reset timer in Arduino Micro-controller from activating
         * and resetting stae of all diffusers once 1 min has passed
         *
         * So this timer will send a signal ("6" in String) every 30 secs to refresh the
         * timer in the Arduino Micro-controller
         *
         * " InterrputTimer = new CountDownTimer(...) " is to create a new instance of a timer
         * whenever this activity becomes visible to the user
         */
       InterrputTimer = new CountDownTimer(30000,1000) {

           /** A function that activates every count down interval (set by the user) */
            public void onTick(long millisUntilFinished) {
                Log.d("TIMER","seconds remaining: " + millisUntilFinished / 1000);
            };

            /** A function that activates once the timer has counted down to zero */
            public void onFinish() {
                Service.SendString("6");
                /** A way of looping the timer indefinetely whenever it ends */
                this.start();
            }
        }.start();  /** Starts the count down timer */

        if(InterrputTimer != null) {
            Log.d(TAG,"Timer is started");
        }
    }

    /** When activity is not visible to the user, stops the service by unbinding it from the activity */
    @Override
    protected void onStop() {  /** onStop is when activity is not visible to user  */
        super.onStop();
        /** Unbinds the service from the caller and destroys it*/
        unbindService(connection);
        Bound = false;
        Log.d(TAG,"Bound service is unbounded or destroyed");


        /** Stops the count down timer early if this activity is no longer visible to the user */
        InterrputTimer.cancel();
        /** Sets the timer variable to null */
        InterrputTimer = null;
        if(InterrputTimer == null) {
            Log.d(TAG,"Timer is stopped");
        }
        else {
            Log.d(TAG,"Error : Timer is still running");
        }
    }

}
