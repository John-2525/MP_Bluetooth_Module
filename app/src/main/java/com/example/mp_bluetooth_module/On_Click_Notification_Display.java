package com.example.mp_bluetooth_module;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Background_Items.BluetoothBackground;
import Classes.Reminder_For_Weekly_And_Single;

public class On_Click_Notification_Display extends AppCompatActivity {

    private static final String TAG = "CheckPoint";
    private TextView DisplayNotificationText;
    private Button PlayAudioBtn;
    private FloatingActionButton BackFAB;
    private String Message, DownloadUrl, AudioUrl, OutsideApp;
    private MediaPlayer mediaPlayer;
    private DatabaseReference ReminderDatabase;
    private Reminder_For_Weekly_And_Single ReminderAudio;
    private CountDownTimer InterruptTimer;
    BluetoothBackground Service;
    boolean Bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_click_notification_display);

        Message = getIntent().getStringExtra("DisplayReminderMessage");
        OutsideApp = getIntent().getStringExtra("ClickFromOutsideApp");
        QueryAudioDownloadUrl(Message);

        DisplayNotificationText = findViewById(R.id.NotificationDisplayText);
        BackFAB = findViewById(R.id.NotificationDisplayBackFloatingActionButton);
        PlayAudioBtn= findViewById(R.id.NotificationDisplayPlayAudio);

        DisplayNotificationText.setText(Message);

        BackFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(OutsideApp.equals("true")) {
                    BackToMainMenu();
                }
                else if(OutsideApp.equals("false")) {
                    BackToAllReminder();
                }
            }
        });

        PlayAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayReminderAudio();
            }
        });
    }

    public void BackToAllReminder() {
        Intent returnIntent = new Intent(this,Display_All_Reminders.class);
        startActivity(returnIntent);
    }

    public void BackToMainMenu() {
        Intent mmIntent = new Intent(this,MainActivity.class);
        startActivity(mmIntent);
    }

    public void PlayReminderAudio() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(DownloadUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(On_Click_Notification_Display.this, "Playing Recording", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Log.e(TAG, "Audio failed to play");
        }
    }

    public void QueryAudioDownloadUrl(String NotificationMessage) {
        ReminderDatabase = FirebaseDatabase.getInstance("https://image-video-album-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("Reminders");
        ReminderDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ReminderData: snapshot.getChildren()) {
                    ReminderAudio = ReminderData.getValue(Reminder_For_Weekly_And_Single.class);
                    if((ReminderAudio.getReminderMessage()).equals(NotificationMessage)) {
                        DownloadUrl = ReminderAudio.getReminderAudioDownloadURL();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG,"Failed to get Reminder data");
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
        InterruptTimer = new CountDownTimer(5000,1000) {

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

        if(InterruptTimer != null) {
            Log.d(TAG,"Timer is started");
        }
    }

    /** When activity is not visible to the user, stops the service by unbinding it from the activity */
    @Override
    protected void onStop() {  /** onStop is when activity is not visible to user  */
        super.onStop();

        /** Stops the count down timer early if this activity is no longer visible to the user */
        InterruptTimer.cancel();
        /** Sets the timer variable to null */
        InterruptTimer = null;
        if(InterruptTimer == null) {
            Log.d(TAG,"Timer is stopped");
        }
        else {
            Log.d(TAG,"Error : Timer is still running");
        }

        /** Unbinds the service from the caller and destroys it*/
        unbindService(connection);
        Bound = false;
        Log.d(TAG,"Bound service is unbounded or destroyed");
    }

}