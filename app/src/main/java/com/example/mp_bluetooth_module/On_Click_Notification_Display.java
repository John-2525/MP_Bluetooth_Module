package com.example.mp_bluetooth_module;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
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
}