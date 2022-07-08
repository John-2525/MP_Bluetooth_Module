package com.example.mp_bluetooth_module;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import Adapter.AllReminderAdapter;
import Background_Items.BluetoothBackground;
import Background_Items.Reminder_Notification;
import Classes.Reminder_For_Weekly_And_Single;

public class Display_All_Reminders extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    /** Initialize variables */
    private RecyclerView DisplayAllReminderRecyclerView;
    private FloatingActionButton BackFAB, SetReminderFAB;
    BluetoothBackground Service;
    boolean Bound = false;
    private static final String TAG = "CheckPoint";
    private CountDownTimer InterruptTimer;
    private RecyclerView ReminderRecyclerView;
    private Query ReminderQuery, SearchReminderToDelete;
    private AllReminderAdapter ReminderAdapter;
    private LinearLayoutManager ReminderGridLayout;
    private Reminder_For_Weekly_And_Single ReminderData, DeleteReminderData;
    private StorageReference BaseStorageReference, ReminderStorageRef;
    private DatabaseReference BaseDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_all_reminders);

        DisplayAllReminderRecyclerView = findViewById(R.id.Reminder_Recycler_View);
        BackFAB = findViewById(R.id.Reminder_Recycler_View_Floating_Back_Btn);
        SetReminderFAB = findViewById(R.id.Reminder_Recycler_View_Floating_Set_Reminder_Button);
        ReminderRecyclerView = findViewById(R.id.Reminder_Recycler_View);

        /**
         * Create and initialize part of the references to Firebase Storage and Realtime database
         * for easier and shorter references below
         */
        BaseStorageReference = FirebaseStorage.getInstance().getReference();
        BaseDatabaseReference = FirebaseDatabase.getInstance("https://image-video-album-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();

        ReminderQuery = FirebaseDatabase.getInstance("https://image-video-album-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("Reminders");

        FirebaseRecyclerOptions<Reminder_For_Weekly_And_Single> options =
                new FirebaseRecyclerOptions.Builder<Reminder_For_Weekly_And_Single>()
                .setQuery(ReminderQuery, Reminder_For_Weekly_And_Single.class)
                .build();

        ReminderAdapter = new AllReminderAdapter(options);

        ReminderAdapter.setOnItemClickListener(new AllReminderAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(DataSnapshot ReminderSnapshot, int ReminderPosition, View v) {
                ReminderData = ReminderSnapshot.getValue(Reminder_For_Weekly_And_Single.class);
                showPopupMenu(v);
            }
        });

        ReminderGridLayout = new LinearLayoutManager(this);
        ReminderRecyclerView.setLayoutManager(ReminderGridLayout);
        ReminderRecyclerView.setAdapter(ReminderAdapter);


        BackFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnToMainMenu();
            }
        });

        SetReminderFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewReminder();
            }
        });
    }

    public void ReturnToMainMenu() {
        Service.SendString("2");
        Intent intentMainMenu = new Intent(this, MainActivity.class);
        startActivity(intentMainMenu);
    }

    public void CreateNewReminder() {
        Intent intentNewReminder = new Intent(this,Create_And_Upload_Reminder.class);
        startActivity(intentNewReminder);
    }


    public void showPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(this,v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.all_reminder_display_popup_menu);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case  R.id.ViewReminderMenuOption:
                if(ReminderData != null) {
                    Intent viewReminderIntent = new Intent(this,On_Click_Notification_Display.class);
                    viewReminderIntent.putExtra("DisplayReminderMessage",ReminderData.getReminderMessage());
                    viewReminderIntent.putExtra("ClickFromOutsideApp","false");
                    startActivity(viewReminderIntent);
                }
                return true;
            case R.id.CancelReminderMenuOption:
                if(ReminderData != null) {
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent deleteIntent = new Intent(this, Reminder_Notification.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this,ReminderData.getRequestCode(),
                    deleteIntent,PendingIntent.FLAG_UPDATE_CURRENT);

                    SearchReminderToDelete = BaseDatabaseReference.child("Reminders");
                    SearchReminderToDelete.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot ReminderSnapshot:snapshot.getChildren()) {
                                DeleteReminderData = ReminderSnapshot.getValue(Reminder_For_Weekly_And_Single.class);
                                if(DeleteReminderData.getRequestCode() == ReminderData.getRequestCode()) {
                                    ReminderSnapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            ReminderStorageRef = BaseStorageReference.child("Reminder").child("Voice Recording").child(ReminderData.getReminderMessage());
                                            ReminderStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Log.d(TAG,"Deleted Reminder audio file from online storage");
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG,"Failed to delete Reminder Audio from online storage");
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG,"Failed to delete Reminder data from realtime database");
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG,"Unable to get Reminder data for deletion");
                        }
                    });

                    alarmManager.cancel(pendingIntent);
                    Log.d(TAG,"AlarmManager cancellation successful");
                    Toast.makeText(this,"Alarm cancelled successfully",Toast.LENGTH_SHORT).show();

                }
                return true;
            default:
                return false;
        }
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
        ReminderAdapter.startListening();
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
        InterruptTimer = new CountDownTimer(30000,1000) {

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
        if(ReminderAdapter != null) {
            ReminderAdapter.stopListening();
        }
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