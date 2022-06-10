package com.example.mp_bluetooth_module;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import Background_Items.BluetoothBackground;

public class Upload_File_Firebase extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 7;
    BluetoothBackground Service;
    boolean Bound = false;
    private static final String TAG = "CheckPoint";
    private CountDownTimer InterruptTimer;
    private Button UploadBtn, BackBtn, RecordBtn, StopRecordingBtn,PlayAudioBtn;
    private MediaRecorder mRecorder;
    private EditText FilesName;
    private Context context;
    private StorageReference mstorageReference;
    private String mFileName, FolderPath;
    private File file, AudioFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file_firebase);

        UploadBtn = findViewById(R.id.UploadUploadBtn);
        BackBtn = findViewById(R.id.UploadBackBtn);
        RecordBtn = findViewById(R.id.RecordAudioBtn);
        FilesName = findViewById(R.id.FileName);
        StopRecordingBtn = findViewById(R.id.StopAudioRecordingBtn);
        PlayAudioBtn = findViewById(R.id.AudioPlayBtn);

        FolderPath = "Recorded Audio";
        mstorageReference = FirebaseStorage.getInstance().getReference();

        CreateFolderAndGetExternalPermission();
        getMicrophonePermission();

    }

    private void askExtStoragePermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == PERMISSION_REQUEST_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createDirectory(FolderPath);
            }
        }
        else {
            Toast.makeText(Upload_File_Firebase.this,"Permission Denied",Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void createDirectory(String Folder_Name) {
        file = new File(Environment.getExternalStorageDirectory(), Folder_Name);
        Log.d(TAG,Environment.getDataDirectory().toString());
        /** Checks if the folder already exists in the external storage of the phone */
        if(!file.exists()) {
            /** If it does not exist then it creates it */
            file.mkdirs();
            Toast.makeText(Upload_File_Firebase.this, "Folder Made", Toast.LENGTH_SHORT).show();
        }
    //    else {
    //        Toast.makeText(Upload_File_Firebase.this, "Folder is already made", Toast.LENGTH_SHORT).show();
    //    }
    }


    private void CreateFolderAndGetExternalPermission() {
        /** Checks if permission to write to external storage is granted */
        if(ContextCompat.checkSelfPermission(Upload_File_Firebase.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            createDirectory(FolderPath);
        }

        else {
            /** Gets permisiion to do so if it is not granted */
            askExtStoragePermission();
        }
    }


    private void getMicrophonePermission() {
        if(this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
            }
        }
    }



    /** Function to start audio recording using device's built in microphone */
    private void startRecording() {
        /** Checks if the EditText is null to prevent null name or error when .setOutputFile */
        mFileName = getFileName();
        if(mFileName != null) {
            Log.d(TAG,"File name is "+mFileName);
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile();

            try {
                mRecorder.prepare();
            }
            catch(Exception e) {
                Log.e(TAG,"Recorder prepare() failed");
            }

            mRecorder.start();
        }
        else {
            Toast.makeText(context,"Please key in a file name",Toast.LENGTH_SHORT).show();
        }
    }


    /** Function to stop audio recording from device's built in microphone */
    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

        //uploadAudio();
    }

    private void uploadAudio() {
        //StorageReference filepath = mstorageReference

    }


    /** Function to get String text from EditText in Upload_File_Firebase layout */
    public String getFileName() {
        String FileName = FilesName.getText().toString();
        if(FileName == null) {
            return null;
        }
        else {
            return FileName;
        }
    }

    private String getAudioRecordingFilePath() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        AudioFile = contextWrapper.getExternalFilesDir(Environment.);

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
        /** Unbinds the service from the caller and destroys it*/
        unbindService(connection);
        Bound = false;
        Log.d(TAG,"Bound service is unbounded or destroyed");


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
    }
}