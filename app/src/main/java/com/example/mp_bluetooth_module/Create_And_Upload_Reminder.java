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
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

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
    private File AudioFolder, ArrayDataFolder, ArrayTxtFile, ArrayPath, RecordingAudioPath, RecordingAudioFile;
    private File[] ReminderAudioFileList;
    private Reminder_For_Weekly_And_Single ReminderDetailStorage;
    private String RecordingAudioFolderPath, SelectedTimeTextTemplate, TextToBeSet,
    ReminderDescriptionText, SelectedDateTextTemplate, ArrayFolderPath, ExtReminderAudioSpecificFolder,
            mFileName;
    private char ReminderTitleText;
    private String[] DayName = new String[]{"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
    private DialogFragment TimePicker;
    private int SelectedHour, SelectedMinute, SelectedYear, SelectedMonth, SelectedDay;
    private boolean IsRecurring, EmptyText;
    private boolean[] DayIsSelected;
    private RadioGroup DayRadioGroupBtns;
    private ScrollView CheckBoxScrollView;
    private ArrayList<Integer> GeneratedNumber;
    private MediaRecorder mRecorder;
    private MediaPlayer mediaPlayer;
    private StorageReference mstorageReference;
    private DatabaseReference mDataBaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_and_upload_reminder);

        ArrayFolderPath = "Generated Number File";
        RecordingAudioFolderPath = "Reminder Recorded Audio";
        SelectedTimeTextTemplate = "Selected Timing : ";
        SelectedDateTextTemplate = "Selected Date : ";
        ExtReminderAudioSpecificFolder = "/Android/data/com.example.mp_bluetooth_module/files/Reminder Recorded Audio/";
        mstorageReference = FirebaseStorage.getInstance().getReference();
        mDataBaseReference = FirebaseDatabase.getInstance("https://image-video-album-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();


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
        CreateNumberTextFile("Generated_Number");

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
                startRecording();
            }
        });

        StopRecordingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        PlayAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayAudio();
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
            RNJesus();
        }
        else {
            if(!getReminderDescription().isEmpty() &&
            SelectedYear > 0 && SelectedMonth >0 && SelectedDay >0 && RecordingAudioFile.exists()) {
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
                    StartNonLoopingAlarm(ReminderDetailStorage);
                }
            }
            else {
                Toast.makeText(this,"Please ensure that date, time are selected," +
                        " message has been typed and an audio recording has been made", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void StartNonLoopingAlarm(Reminder_For_Weekly_And_Single RDetail) {

        int RequestCode = RNJesus();
        Intent intentReminder = new Intent(this, Reminder_Notification.class);
        intentReminder.putExtra("ReminderMsg",RDetail.getReminderMessage());
        intentReminder.putExtra("RequestCode",RequestCode);
        RDetail.setRequestCode(RequestCode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,RequestCode,intentReminder,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, RDetail.getReminderCalendar().getTimeInMillis(),pendingIntent);
        }

        UploadRecordedAudioAndCustomClass(RDetail);

        Toast.makeText(this,"Reminder Set",Toast.LENGTH_SHORT).show();
    }


    /** Creates an empty text file to help store and track integers used for pendingIntents' request code */
    public void CreateNumberTextFile(String fileName) {
        ContextWrapper contextWrapper = new ContextWrapper(this);
        ArrayPath = contextWrapper.getExternalFilesDir(ArrayFolderPath);
        ArrayTxtFile = new File(ArrayPath, fileName+".txt");
        if(!ArrayTxtFile.exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(ArrayTxtFile);
                PrintWriter pw = new PrintWriter(fos);
                pw.close();
                fos.close();
            }
            catch (Exception e) {
                Log.e(TAG,"Could not create text file");
            }
        }
    }


    /**
     * Function to create random +ve integer for request code and store them into a txt file to prevent
     * overlapping integer request code when making more pendingIntents
     */
    public int RNJesus() {
        Random rng = new Random();
        Integer nextNumber;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            GeneratedNumber = ReadIntegersFromTxT();
        }
        Log.d(TAG,"Number array length is "+GeneratedNumber.size());
        while (true) {
            nextNumber = rng.nextInt();
            if(nextNumber >=0) {
                if (!GeneratedNumber.contains(nextNumber)) {
                    GeneratedNumber.add(nextNumber);
                    SaveArrayListToSD(nextNumber);
                    break;
                }
            }
        }
        return nextNumber;
    }


    public void SaveArrayListToSD(Integer newNumber) {
        try {
            FileWriter fw = new FileWriter(ArrayTxtFile,true);
            PrintWriter pw = new PrintWriter(fw);
            pw.write(newNumber+",");
            Log.d(TAG,"Written "+newNumber+" to the txt file successfully");
            pw.close();
            fw.close();
        }
        catch (Exception e) {
            Log.e(TAG,"Could not save generated integer to txt file");
        }
    }


    public ArrayList<Integer> ReadIntegersFromTxT() {

        ArrayList<Integer> NumberArrayList = new ArrayList<Integer>();
        try {

            InputStream is = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                is = Files.newInputStream(Paths.get(ArrayTxtFile.getAbsolutePath()));
            }
            Scanner scanner = new Scanner(is);
            scanner.useDelimiter(",");
            while(scanner.hasNextInt()) {
                int i = scanner.nextInt();
                NumberArrayList.add(i);
            }
            for(int ii=0;ii<NumberArrayList.size();ii++) {
                Log.d(TAG, "From array : "+NumberArrayList.get(ii));
            }
            Log.d(TAG, "Read txt file and returned an array of integer successfully");
        }
        catch (IOException e) {
            Log.e(TAG,"Could not read ArrayList from file, IOException");
        }
        return NumberArrayList;
    }


    public void UploadRecordedAudioAndCustomClass(Reminder_For_Weekly_And_Single CustomClass) {

    }





    /** Function to check if input for Reminder Description from EditText exists already */
    public String checkFileName(String FileName) {
        /** Sets/resets Boolean check for startRecording() to prevent overlapping messages */
        EmptyText = false;
        /** Checks if a string name is actually inputted */
        if(!FileName.equals(null)&&!FileName.isEmpty()) {
            /** Gets a list of files from the folder containing all audio recording */
            ReminderAudioFileList = Environment.getExternalStoragePublicDirectory(ExtReminderAudioSpecificFolder).listFiles();
            /** Check if any of the file names from the list matches with inputted file name */
            for(File AF : ReminderAudioFileList) {
                Log.d(TAG,AF.getName());
                if((AF.getName()).equals(FileName+".3gp")) {
                    Toast.makeText(this,"Reminder description matches an existing description",Toast.LENGTH_SHORT).show();
                    return null;
                }
            }
            return FileName;
        }
        else {
            /** Sets the Boolean flag for startRecording() */
            EmptyText = true;
            Toast.makeText(this,"Please key in message for reminder",Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    /** Function to start audio recording using device's built in microphone */
    private void startRecording() {
        /** Gets the file name from the EditText and checks if it already exists */
        mFileName = checkFileName(getReminderDescription());
        /** Prevents overlapping message by skipping this function if Boolean check is true */
        if(!(EmptyText == true)) {
            /** Checks if the EditText is not null name or error when .setOutputFile */
            if (mFileName != null) {
                Log.d(TAG, "File name is " + mFileName);
                /**
                 * When writing the codes below, there is a certain order to them, which is stated in
                 * the documentation due to the MediaRecorder state diagram in
                 * https://developer.android.com/reference/android/media/MediaRecorder
                 */

                /** Creates a new instance of MediaRecorder */
                mRecorder = new MediaRecorder();
                /**
                 * Sets the audio source for recording, The source needs to be specified before setting
                 * recording-parameters or encoders. Call this only before setOutputFormat()
                 */
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                /**
                 * Sets the format of the output file produced during recording.
                 * Call this after setAudioSource()/setVideoSource() but before prepare().
                 *
                 * It is recommended to always use 3GP format when using the H.263 video encoder and AMR audio encoder.
                 * Using an MPEG-4 container format may confuse some desktop players
                 */
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                /** Pass in the file object to be written. Call this after setOutputFormat() but before prepare() */
                mRecorder.setOutputFile(getAudioRecordingFilePath(RecordingAudioFolderPath));
                /**
                 * Sets the audio encoder to be used for recording. If this method is not called,
                 * the output file will not contain an audio track.
                 * Call this after setOutputFormat() but before prepare().
                 */
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                try {
                    /**
                     * Prepares the recorder to begin capturing and encoding data.
                     * This method must be called after setting up the desired audio and video sources,
                     * encoders, file format, etc., but before start().
                     */
                    mRecorder.prepare();
                } catch (Exception e) {
                    Log.e(TAG, "Recorder prepare() failed");
                    e.printStackTrace();
                }

                try {
                    /**
                     * Begins capturing and encoding data to the file specified with setOutputFile().
                     * Call this after prepare().
                     */
                    mRecorder.start();
                } catch (Exception e) {
                    Log.e(TAG, "Recorder start() failed");
                    e.printStackTrace();
                }
                Toast.makeText(Create_And_Upload_Reminder.this, "Recording Started", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /** Function to stop audio recording from device's built in microphone */
    private void stopRecording() {
        /**
         * Stops recording. Call this after start().
         * Once recording is stopped, you will have to configure it again as if it has just been constructed.
         *
         * Note that a RuntimeException is intentionally thrown to the application, if no valid audio/video data has
         * been received when stop() is called.
         *
         * This can happen for instance if stop() is called immediately after start().
         * The failure lets the application take action accordingly to clean up the output file like
         * for instance, delete th output file. As the output file will not be properly constructed when this happens.
         */
        mRecorder.stop();
        /**
         * Releases resources associated with this MediaRecorder object.
         * It is good practice to call this method when you're done using the MediaRecorder
         *
         * If not called, unnecessary resources like memory and codec will be held, increase
         * battery consumption and recording failure for other applications due to running multiple
         * instances of the same codec being unsupported
         * or degraded performance if multiple codec instances are supported
         */
        mRecorder.release();
        /** Nullifies the mediarecorder to mark it for the Garbage collector to collect the object */
        mRecorder = null;
        Toast.makeText(Create_And_Upload_Reminder.this, "Recording Stopped", Toast.LENGTH_SHORT).show();
    }


    private void PlayAudio() {
        try {
            /** Create new instance of media player */
            mediaPlayer = new MediaPlayer();
            /** Sets the data source as a content Uri (content URI of data to be played) */
            mediaPlayer.setDataSource(getAudioRecordingFilePath(RecordingAudioFolderPath));
            /**
             * Prepares the player for playback, synchronously.
             * After setting the datasource and the display surface, you need to either call prepare() or prepareAsync().
             * For files, it is OK to call prepare(), which blocks until MediaPlayer is ready for playback.
             */
            mediaPlayer.prepare();
            /** Start or resumes playback of media */
            mediaPlayer.start();
            Toast.makeText(Create_And_Upload_Reminder.this, "Playing Recording", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Log.e(TAG,"Audio failed to play");
            e.printStackTrace();
        }
    }


    private String getAudioRecordingFilePath(String FolderPath) {
        /** See createDirectory(String Folder_Name) for the explanation of the 2 lines below this documentation */
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        RecordingAudioPath = contextWrapper.getExternalFilesDir(FolderPath);
        /**
         * Creates a new File instance from a parent pathname string
         * or parent abstract pathname and a child pathname string (from the EditText).
         */
        RecordingAudioFile = new File(RecordingAudioPath,getReminderDescription()+".3gp");
        /** Converts this abstract pathname into a pathname string before returning it */
        return RecordingAudioFile.getPath();
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
                createDirectory(RecordingAudioFolderPath, AudioFolder);
                createDirectory(ArrayFolderPath,ArrayDataFolder);
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
            createDirectory(RecordingAudioFolderPath, AudioFolder);
            createDirectory(ArrayFolderPath,ArrayDataFolder);
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