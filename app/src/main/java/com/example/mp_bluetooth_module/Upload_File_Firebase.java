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
import android.media.MediaPlayer;
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
    private MediaPlayer mediaPlayer;
    private EditText FilesName;
    private Context context;
    private StorageReference mstorageReference;
    private String mFileName, FolderPath;
    private File AudioFolder, AudioFile, AudioPath;


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

        /** Listens to any clicks on record button */
        RecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /** Function to start audio recording from phone mic */
                startRecording();
            }
        });

        /** Listens to any clicks on stop recording button */
        StopRecordingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /** Function to stop audio recording from phone mic */
                stopRecording();
            }
        });

        /** Listens to any clicks on play audio button */
        PlayAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /** Function to play recorded audio from storage */
                PlayAudio();
            }
        });

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
                createDirectory(FolderPath);
            }
        }
        /** If users dont give permission to write to external storage */
        else {
            Toast.makeText(Upload_File_Firebase.this,"Permission Denied",Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    /** Function to check if permission to write external storage is given when coming to this activity */
    private void CreateFolderAndGetExternalPermission() {
        /** Checks if permission to write to external storage is granted */
        if(ContextCompat.checkSelfPermission(Upload_File_Firebase.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            createDirectory(FolderPath);
        }

        else {
            /** Gets permission to do so if it is not granted */
            askExtStoragePermission();
        }
    }


    /** Function to create a folder within the com.example.~~ folder within the phone storage, located at Android/data/~~*/
    private void createDirectory(String Folder_Name) {

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
         * doesnt say anything related to that, forum reply from May 2021 claims that the online documentation needs to be updated regarding this
         */
        AudioFolder = contextWrapper.getExternalFilesDir(Folder_Name);
        /** Checks if the folder has been made or already exists */
        if(!AudioFolder.exists()) {
            Toast.makeText(Upload_File_Firebase.this, "Error folder not made", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(Upload_File_Firebase.this, "Folder Made/Exists", Toast.LENGTH_SHORT).show();
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

    /** Function to start audio recording using device's built in microphone */
    private void startRecording() {
        /** Gets the file name from the EditText and set it to a variable */
        mFileName = getFileName();
        /** Checks if the EditText is empty to prevent null name or error when .setOutputFile */
        if(!mFileName.isEmpty()) {
            Log.d(TAG,"File name is "+mFileName);
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
            mRecorder.setOutputFile(getAudioRecordingFilePath(FolderPath));
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
            }
            catch(Exception e) {
                Log.e(TAG,"Recorder prepare() failed");
                e.printStackTrace();
            }

            try {
                /**
                 * Begins capturing and encoding data to the file specified with setOutputFile().
                 * Call this after prepare().
                 */
                mRecorder.start();
            }
            catch (Exception e) {
                Log.e(TAG,"Recorder start() failed");
                e.printStackTrace();
            }
            Toast.makeText(Upload_File_Firebase.this, "Recording Started", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(Upload_File_Firebase.this,"Please key in a file name",Toast.LENGTH_SHORT).show();
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
        mRecorder = null;
        Toast.makeText(Upload_File_Firebase.this, "Recording Stopped", Toast.LENGTH_SHORT).show();

        //uploadAudio();
    }

    private void PlayAudio() {
        try {
            /** Create new instance of media player */
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setDataSource(getAudioRecordingFilePath(FolderPath));

            mediaPlayer.prepare();

            mediaPlayer.start();
            Toast.makeText(Upload_File_Firebase.this, "Playing Recording", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Log.e(TAG,"Audio failed to play");
            e.printStackTrace();
        }
    }

    private String getAudioRecordingFilePath(String FolderPath) {
        /** See createDirectory(String Folder_Name) for the explanation of the 2 lines below this documentation */
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        AudioPath = contextWrapper.getExternalFilesDir(FolderPath);

        AudioFile = new File(AudioPath,getFileName()+".3gp");
        return AudioFile.getPath();
    }


    private void uploadAudio() {
        //StorageReference filepath = mstorageReference

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
     * ( TODO Confirm this theory )
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