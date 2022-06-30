package com.example.mp_bluetooth_module;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import Background_Items.BluetoothBackground;
import Background_Items.Reminder_Notification;
import Background_Items.Reminder_Time_Picker;
import Classes.Reminder_For_Weekly_And_Single;

public class Create_And_Upload_Reminder extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    private static final int PERMISSION_REQUEST_CODE = 7;
    BluetoothBackground Service;
    boolean Bound = false;
    private static final String TAG = "CheckPoint";
    private CountDownTimer InterruptTimer;
    private Button BackBtn, StartRecordingBtn, StopRecordingBtn, PlayAudioBtn,
    SelectReminderTimeBtn, CreateReminder, SelectDateBtn;
    private EditText ReminderDetailsEditText;
    private TextView DisplaySelectedTimeTextView, DisplaySelectedDate;
    private Switch WeeklyLoopingSwitch;
    private File AudioFolder;
    private Reminder_For_Weekly_And_Single ReminderDetailStorage;
    private String AudioFolderPath, SelectedTimeTextTemplate, TextToBeSet,
    ReminderDescriptionText, SelectedDateTextTemplate;
    private char ReminderTitleText;
    private String[] DayName = new String[]{"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
    private DialogFragment TimePicker;
    private int SelectedHour, SelectedMinute, SelectedYear, SelectedMonth, SelectedDay;
    private boolean IsRecurring;
    private boolean[] DayIsSelected;
    private RadioGroup DayRadioGroupBtns;
    private ScrollView CheckBoxScrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_and_upload_reminder);

        AudioFolderPath = "Recorded Audio";
        SelectedTimeTextTemplate = "Selected Timing : ";
        SelectedDateTextTemplate = "Selected Date : ";

        BackBtn = findViewById(R.id.BackToAllReminderDisplayButton);
        StartRecordingBtn = findViewById(R.id.StartRecordReminderAudioButton);
        StopRecordingBtn = findViewById(R.id.StopRecordingReminderAudioButton);
        PlayAudioBtn = findViewById(R.id.ReminderPlayRecordingAudioButton);
        SelectReminderTimeBtn = findViewById(R.id.PickTimeButton);
        CreateReminder = findViewById(R.id.CreateReminderButton);
        SelectDateBtn = findViewById(R.id.SelectDateButton);

        ReminderDetailsEditText = findViewById(R.id.EditTextCreateReminderDetails);
        DisplaySelectedTimeTextView = findViewById(R.id.DisplaySelectedTimeTextView);
        DisplaySelectedDate = findViewById(R.id.SelectedDateTextView);
        WeeklyLoopingSwitch = findViewById(R.id.LoopingReminderSwitch);
        DayRadioGroupBtns = findViewById(R.id.DayRadioGroup);
        CheckBoxScrollView = findViewById(R.id.DaysOfTheWeekScrollView);

        CheckBoxScrollView.setVisibility(View.INVISIBLE);
        SelectDateBtn.setVisibility(View.VISIBLE);
        DisplaySelectedDate.setVisibility(View.VISIBLE);

        Calendar DatePickcalendar = Calendar.getInstance();
        final int currentYear = DatePickcalendar.get(Calendar.YEAR);
        final int currentMonth = DatePickcalendar.get(Calendar.MONTH);
        final int currentDay = DatePickcalendar.get(Calendar.DAY_OF_MONTH);

        CreateFolderAndGetExternalPermission();
        getMicrophonePermission();

        WeeklyLoopingSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(WeeklyLoopingSwitch.isChecked()) {
                    CheckBoxScrollView.setVisibility(View.VISIBLE);
                    SelectDateBtn.setVisibility(View.INVISIBLE);
                    DisplaySelectedDate.setVisibility(View.INVISIBLE);
                }
                else {
                    CheckBoxScrollView.setVisibility(View.INVISIBLE);
                    SelectDateBtn.setVisibility(View.VISIBLE);
                    DisplaySelectedDate.setVisibility(View.VISIBLE);
                }
            }
        });


        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnToAllReminderDisplay();
            }
        });

        SelectReminderTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePicker = new Reminder_Time_Picker();
                TimePicker.show(getSupportFragmentManager(),"Time Picker");
            }
        });

        StartRecordingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        StopRecordingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        PlayAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        CreateReminder.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    CreateReminder(CheckReminderIsToBeRecurring());
                }
            }
        });


        SelectDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        Create_And_Upload_Reminder.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        SelectedYear = year;
                        SelectedMonth = month;
                        SelectedDay = dayOfMonth;
                        month = month+1;
                        String date = dayOfMonth+"/"+month+"/"+year;
                        DisplaySelectedDate.setText(SelectedDateTextTemplate + date);
                    }
                },currentYear,currentMonth,currentDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });


    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        SelectedHour = hourOfDay;
        SelectedMinute = minute;
        TextToBeSet = SelectedTimeTextTemplate+hourOfDay+":"+minute;
        DisplaySelectedTimeTextView.setText(TextToBeSet);
    }

    public void ReturnToAllReminderDisplay() {
        Intent intentReturn = new Intent(this, Display_All_Reminders.class);
        startActivity(intentReturn);
    }

    public String getReminderDescription() {
        ReminderDescriptionText = ReminderDetailsEditText.getText().toString();
        Log.d(TAG,"Description of reminder is : "+ReminderDescriptionText);
        return ReminderDescriptionText;
    }

    public boolean CheckReminderIsToBeRecurring() {
        IsRecurring = WeeklyLoopingSwitch.isChecked();
        Log.d(TAG,"Reminder will loop recurring : "+IsRecurring);
        return IsRecurring;
    }

    public void onDayRadioButtonClicked() {
        DayIsSelected = new boolean[]{false,false,false,false,false,false,false};

       if(DayRadioGroupBtns.getCheckedRadioButtonId() == -1) {
           Toast.makeText(this,"Please select a day",Toast.LENGTH_SHORT).show();
       }
       else {
           int selectedID = DayRadioGroupBtns.getCheckedRadioButtonId();
           View radioBtn = DayRadioGroupBtns.findViewById(selectedID);
           int position = DayRadioGroupBtns.indexOfChild(radioBtn);
           DayIsSelected[position] = true;
           Log.d(TAG,DayName[position]+" is selected");
       }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void CreateReminder(boolean Loop) {
        if(Loop) {
            onDayRadioButtonClicked();
        }
        else {
            if(!getReminderDescription().isEmpty() &&
            SelectedYear > 0 && SelectedMonth >0 && SelectedDay >0) {
                Calendar ReminderCalender = Calendar.getInstance();
                ReminderCalender.set(Calendar.YEAR, SelectedYear);
                ReminderCalender.set(Calendar.MONTH, 6);
                ReminderCalender.set(Calendar.DAY_OF_MONTH, SelectedDay);
                /** Use .HOUR_OF_DAY as it is used for 24H clock while .HOUR is used for 12H clock */
                ReminderCalender.set(Calendar.HOUR_OF_DAY, SelectedHour);
                ReminderCalender.set(Calendar.MINUTE, SelectedMinute);
                ReminderCalender.set(Calendar.SECOND, 0);

                ReminderDetailStorage = new Reminder_For_Weekly_And_Single();
                ReminderDetailStorage.setReminderCalendar(ReminderCalender);
                ReminderDetailStorage.setReminderMessage(getReminderDescription());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    StartNonLoopingAlarm(ReminderCalender, ReminderDetailStorage);
                }
            }
            else {
                Toast.makeText(this,"Please ensure that date, time are selected and message has been typed", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void StartNonLoopingAlarm(Calendar calendar, Reminder_For_Weekly_And_Single RDetail) {
        Intent intentReminder = new Intent(this, Reminder_Notification.class);
        intentReminder.putExtra("ReminderData",RDetail);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1,intentReminder,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);
        }
    }



    /** Function to ask user permission to write to external storage */
    private void askExtStoragePermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
    }


    /** Override method on what to do if the user gives/does not give permission to write to external storage */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        /** Checks if the code is for requesting permissions (?) */
        if(requestCode == PERMISSION_REQUEST_CODE) {
            /** Checks if the permission to write external storage is granted by user */
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                /** Creates a folder to house audio recordings */
                createDirectory(AudioFolderPath, AudioFolder);
            }
        }
        /** If users dont give permission to write to external storage */
        else {
            Toast.makeText(Create_And_Upload_Reminder.this,"Permission Denied",Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    /** Function to check if permission to write external storage is given when coming to this activity */
    private void CreateFolderAndGetExternalPermission() {
        /** Checks if permission to write to external storage is granted */
        if(ContextCompat.checkSelfPermission(Create_And_Upload_Reminder.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            createDirectory(AudioFolderPath, AudioFolder);
        }

        else {
            /** Gets permission to do so if it is not granted */
            askExtStoragePermission();
        }
    }

    private void getMicrophonePermission() {
        /** Check whether the given feature name is one of the available features as returned by getSystemAvailableFeatures() */
        if(this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            /** Checks if permission to use the microphone is denied or not granted yet */
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
                /** Requests microphone permission to be granted to this application */
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
            }
        }
    }



    /** Function to create a folder within the com.example.~~ folder within the phone storage, located at Android/data/~~*/
    private void createDirectory(String Folder_Name, File PathOfFolder) {

        /** ContextWrapper == Proxying implementation of Context that simply delegates all of its calls to another
         *  Context. Can be subclassed to modify behavior without changing the original Context.
         *
         *  getApplicationContext == Return the context of the single, global Application object of the current process.
         *  This generally should only be used if you need a Context whose lifecycle is separate from the current context, that
         *  is tied to the lifetime of the process rather than the current component.
         *
         *  From https://developer.android.com/reference/android/content/ContextWrapper
         *  TODO Need to understand and rewrite this part
         */
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        /**
         * According to https://stackoverflow.com/questions/23049100/getexternalfilesdir-not-creating-new-directory,
         * when calling .getExternalFilesDir(), the custom directory will automatically be created, works fine as
         * folder to store Audio recordings is recorded when switching to this activity
         *
         * Documentation on it ( https://developer.android.com/reference/android/content/ContextWrapper#getExternalFilesDirs(java.lang.String) )
         * doesn't say anything related to that, forum reply from May 2021 claims that the online documentation needs to be updated regarding this
         */
        PathOfFolder = contextWrapper.getExternalFilesDir(Folder_Name);
        Log.d(TAG,PathOfFolder.getPath());
        /** Checks if the folder has been made or already exists */
        if(!PathOfFolder.exists()) {
            Toast.makeText(Create_And_Upload_Reminder.this, "Error "+Folder_Name+" folder not made", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(Create_And_Upload_Reminder.this, "Folder Made/Exists", Toast.LENGTH_SHORT).show();
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