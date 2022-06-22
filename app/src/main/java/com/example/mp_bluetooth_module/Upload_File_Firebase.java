package com.example.mp_bluetooth_module;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.media.Image;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URI;

import Background_Items.BluetoothBackground;
import Classes.Firebase_Database_Image_Video_Audio_Upload;

public class Upload_File_Firebase extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 7;
    BluetoothBackground Service;
    boolean Bound = false, EmptyText = false, AudioUploadDone = true, VideoUploadDone = true, ImageUploadDone = true;
    private static final String TAG = "CheckPoint";
    private static final int SELECT_IMAGE_OR_VIDEO_FROM_GALLERY = 3;
    private CountDownTimer InterruptTimer;
    private Button UploadBtn, BackBtn, RecordBtn, StopRecordingBtn,PlayAudioBtn
            , SelectImageOrVideoBtn;
    private MediaRecorder mRecorder;
    private MediaPlayer mediaPlayer;
    private MediaController mediaController;
    private EditText FilesName;
    private ImageView ImageDisplay;
    private VideoView VideoDisplay;
    private Context context;
    private DatabaseReference mDataBaseReference;
    private StorageReference mstorageReference;
    private String mFileName, FolderPath, ExtAppSpecificFolder, InputAudioFileName;
    private File AudioFolder, AudioFile, AudioPath;
    private File[] AudioFileList;
    public Uri selectedMediaUri;



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
         * " InterruptTimer = new CountDownTimer(...) " is to create a new instance of a timer
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

        if(mediaPlayer != null) {
            /** Stops playback after playback has been started or paused */
            mediaPlayer.stop();
            /**
             * Its important to call release() for mediaplayer as it can consume significant amount of system
             * resources so just keep it until no longer needed before calling release() to
             * release any system resources allocated to it are properly released
             */
            mediaPlayer.release();
            /** Nullifies the mediaplayer to mark it for the Garbage collector to collect the object */
            mediaPlayer = null;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file_firebase);

        /** Finds the view by the ID given and sets it to the variable */
        UploadBtn = findViewById(R.id.UploadUploadBtn);
        BackBtn = findViewById(R.id.UploadBackBtn);
        RecordBtn = findViewById(R.id.RecordAudioBtn);
        FilesName = findViewById(R.id.FileName);
        StopRecordingBtn = findViewById(R.id.StopAudioRecordingBtn);
        PlayAudioBtn = findViewById(R.id.AudioPlayBtn);
        SelectImageOrVideoBtn = findViewById(R.id.selectImgVidBtn);
        ImageDisplay = findViewById(R.id.UploadimageView);
        VideoDisplay = findViewById(R.id.UploadvideoView);

        /** Hides the ImageView and VideoDisplay when creating this activity */
        ImageDisplay.setVisibility(View.INVISIBLE);
        VideoDisplay.setVisibility(View.INVISIBLE);

        FolderPath = "Recorded Audio";
        ExtAppSpecificFolder = "/Android/data/com.example.mp_bluetooth_module/files/Recorded Audio/";
        mstorageReference = FirebaseStorage.getInstance().getReference();
        mDataBaseReference = FirebaseDatabase.getInstance("https://image-video-album-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();


        /**
         * Functions to get permission for storage and permission respectively
         * ( Read below for more )
         */
        CreateFolderAndGetExternalPermission();
        getMicrophonePermission();

        /** Listens for any clicks on record button */
        RecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /** Function to start audio recording from phone mic */
                startRecording();
            }
        });

        /** Listens for any clicks on stop recording button */
        StopRecordingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /** Function to stop audio recording from phone mic */
                stopRecording();
            }
        });

        /** Listens for any clicks on play audio button */
        PlayAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /** Function to play recorded audio from storage */
                PlayAudio();
            }
        });

        /** Listens for any clicks on back button */
        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(AudioUploadDone&&ImageUploadDone&&VideoUploadDone) {
                    /** Function to return to Image Video Album */
                    ReturnToAlbumFragments();
                }
                else {
                    Toast.makeText(Upload_File_Firebase.this,"Please wait for uploads to be done",Toast.LENGTH_SHORT).show();
                }
            }
        });

        /** Listens for any clicks on select image or video button */
        SelectImageOrVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /** Function to allow user to select either an image or video */
                PickingImageOrVideo();
            }
        });

        /** Listens for any clicks on upload button */
        UploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AudioUploadDone&&ImageUploadDone&&VideoUploadDone) {
                    /** Uploads the selected image/video and audio file to firebase with custom file name */
                    UploadImageOrVideoWithAudio(getFileName(), selectedMediaUri, AudioFile);
                }
                else {
                    Toast.makeText(Upload_File_Firebase.this,"Please wait for uploads to be done",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    /** Function to get String text from EditText in Upload_File_Firebase layout */
    private String getFileName() {
        InputAudioFileName = FilesName.getText().toString();
        return InputAudioFileName;

        //TODO Put in a check for invalid special characters
    }


    /** Function to check if input file name from EditText exists already */
    public String checkFileName(String FileName) {
        /** Sets/resets Boolean check for startRecording() to prevent overlapping messages */
        EmptyText = false;
        /** Checks if a string name is actually inputted */
        if(!FileName.equals(null)&&!FileName.isEmpty()) {
            /** Gets a list of files from the folder containing all audio recording */
            AudioFileList = Environment.getExternalStoragePublicDirectory(ExtAppSpecificFolder).listFiles();
            /** Check if any of the file names from the list matches with inputted file name */
            for(File AF : AudioFileList) {
                Log.d(TAG,AF.getName());
                if((AF.getName()).equals(FileName+".3gp")) {
                    Toast.makeText(this,"Please enter a new name",Toast.LENGTH_SHORT).show();
                    return null;
                }
            }
            return FileName;
        }
        else {
            /** Sets the Boolean flag for startRecording() */
            EmptyText = true;
            Toast.makeText(this,"Please key in a file name",Toast.LENGTH_SHORT).show();
            return null;
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
         * doesn't say anything related to that, forum reply from May 2021 claims that the online documentation needs to be updated regarding this
         */
        AudioFolder = contextWrapper.getExternalFilesDir(Folder_Name);
        Log.d(TAG,AudioFolder.getPath());
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
        /** Gets the file name from the EditText and checks if it already exists */
        mFileName = checkFileName(getFileName());
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
                Toast.makeText(Upload_File_Firebase.this, "Recording Started", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(Upload_File_Firebase.this, "Recording Stopped", Toast.LENGTH_SHORT).show();
    }

    private void PlayAudio() {
        try {
            /** Create new instance of media player */
            mediaPlayer = new MediaPlayer();
            /** Sets the data source as a content Uri (content URI of data to be played) */
            mediaPlayer.setDataSource(getAudioRecordingFilePath(FolderPath));
            /**
             * Prepares the player for playback, synchronously.
             * After setting the datasource and the display surface, you need to either call prepare() or prepareAsync().
             * For files, it is OK to call prepare(), which blocks until MediaPlayer is ready for playback.
             */
            mediaPlayer.prepare();
            /** Start or resumes playback of media */
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
        /**
         * Creates a new File instance from a parent pathname string
         * or parent abstract pathname and a child pathname string (from the EditText).
         */
        AudioFile = new File(AudioPath,getFileName()+".3gp");
        /** Converts this abstract pathname into a pathname string before returning it */
        return AudioFile.getPath();
    }


    /** Return back to the Image_Video_Album activity */
    public void ReturnToAlbumFragments() {
        Intent albumIntent = new Intent(this,Image_Video_Album.class);
        startActivity(albumIntent);
    }


    /** Function to select either an image or video from the phone's gallery */
    public void PickingImageOrVideo() {

        /**
         * What MediaStore.Images.Media does is that it queries for images and media
         * that are stored in the external storage due to .EXTERNAL_CONTENT_URI
         * While ACTION_PICK is to allow a user to select an image from any of the installed apps which registered for such an action
         */
        Intent galleryIntent = new Intent (Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        /**
         * .setType sets an explicit MIME data type, its used to create
         * intents that only specify a type and not data
         *
         * Note : This method automatically clears any data that was previously set
         */
        galleryIntent.setType("image/* video/*");
        /**
         *
         * The request code is just a number used to identify an intent,
         * especially useful when having multiple intents within an activity as it makes it easier
         * for onActivityResult() to handle the logic for the different intents
         * when their finish() activates by filtering them based on their request code, which is why
         * onActivityResult() has the int requestCode by default
         *
         * For this case, since one intent is using startActivityForResult(), it doesnt not really
         * matter if you add a filter by request code
         * But I decided to add it for good habits purpose
         */
        startActivityForResult(galleryIntent, SELECT_IMAGE_OR_VIDEO_FROM_GALLERY);
    }

    /**
     * Override method onActivityResult() is a callback for when finish() is called within
     * the gallery activity that is triggered by startActivityForResult() within this current activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /**
         * resultCode == RESULT_OK checks if the app succeeded, failed, or something different
         * while data != null is to check in case the app closes correctly (not cancelling) without
         * returning an image at all
         */
        if(resultCode == RESULT_OK && data != null) {
            /** Checks if the request code belongs to selecting photo or video intent */
            if (requestCode == SELECT_IMAGE_OR_VIDEO_FROM_GALLERY) {
                /** Used to get the URI of the data this intent is targeting */
                selectedMediaUri = data.getData();
                Log.d(TAG, selectedMediaUri.toString());

                /** Checks if the data is an image */
                if (selectedMediaUri.toString().contains("image")) {
                    /** Sets the image view visible while hiding video view */
                    ImageDisplay.setVisibility(View.VISIBLE);
                    VideoDisplay.setVisibility(View.INVISIBLE);
                    /** Loads the image onto the image view via Uri */
                    ImageDisplay.setImageURI(selectedMediaUri);
                    Log.d(TAG, "Image Displayed");
                }

                /** Checks if the data is a video */
                else if (selectedMediaUri.toString().contains("video")) {
                    /** Sets the video view visible while hiding image view */
                    VideoDisplay.setVisibility(View.VISIBLE);
                    ImageDisplay.setVisibility(View.INVISIBLE);
                    /** Loads the video onto the video view via Uri */
                    VideoDisplay.setVideoURI(selectedMediaUri);
                    Log.d(TAG, "Video Displayed");

                    /** Function used to control the video the video */

                    /** Creates new instance of MediaController */
                    mediaController = new MediaController(this);
                    VideoDisplay.setMediaController(mediaController);
                    /** Designates which view the MediaController will be attached to */
                    mediaController.setAnchorView(VideoDisplay);
                }
            }
        }
    }

    /**
     *  Function to allow user to upload image/video with audio file to firebase
     *  using a custom file name
     */
    private void UploadImageOrVideoWithAudio(String fileName, Uri MediaUri, File AudioFilePath) {

        /**
         * Checks if filename is not null or empty, image/video is selected by checking if Uri is
         * not null and checks audio file has been recorded through existence of audio file in
         * Uri converted from audio file path
         */
        if(MediaUri != null && AudioFilePath != null && !fileName.isEmpty() && fileName != null) {

            /** Converts audio file path to a Uri */
            Uri AudioUri = Uri.fromFile(AudioFilePath);

            /** Creates a reference to firebase storage folders for images, videos and audios respectively using custom file path and name */
            /**
             * child() gets a reference for the location at the specified relative path, Ex: child("Games") == "/Games"" so
             * by having multiple child(), it will become a file path like "/.../.../..."
             */
            StorageReference ImageStorageReference = mstorageReference.child("Album").child("Images").child(fileName + ".png");
            StorageReference VideoStorageReference = mstorageReference.child("Album").child("Videos").child(fileName + ".mp4");
            StorageReference AudioStorageReference = mstorageReference.child("Album").child("Voice Recordings").child(fileName + ".3gp");


            /** Checks if the Uri points to an image */
            if (MediaUri.toString().contains("image")) {

                try {
                    /** Upload the image file to the cloud storage */
                    /** Also uploads an object containing file name and download uri to firebase database */

                    /** putFile() uploads the file through its Uri into the online storage */
                    /** addOnSuccessListener() checks if the task has been successfully completed */
                    ImageStorageReference.putFile(MediaUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                        /** What to do once response for successful uploading of file to firebase storage is returned */
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            /**
                             * Calling StorageReference.getDownloadUrl() returns a Task,
                             * as it needs to retrieve the download URL from the server.
                             * So you will need a completion listener to get the actual URL
                             */
                            ImageStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri Imageuri) {
                                    /** Creates an instance of custom object containing name and download uri to be uploaded to database */
                                    Firebase_Database_Image_Video_Audio_Upload ImageData = new
                                            Firebase_Database_Image_Video_Audio_Upload(fileName+".png",Imageuri.toString());
                                    /**
                                     * Uploads the custom object to the firebase database to a specific location in the database as denoted by child(),
                                     * push() generates a unique key every time a new child is added to the specified Firebase reference
                                     * this is to prevent overwriting of data in database due to data being sent to the same location database
                                     */
                                    mDataBaseReference.child("Album").child("Images").push().setValue(ImageData);
                                    ImageUploadDone = true;
                                    Toast.makeText(Upload_File_Firebase.this,"Image Uploaded Successfully",Toast.LENGTH_SHORT).show();
                                    Log.d(TAG,"Image uploaded successfully");
                                }
                            });
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            ImageUploadDone = false;
                        }
                    });
                }
                catch(Exception e){
                    Log.e(TAG,"Could not upload image");
                }

                try {
                    /** Upload the image file to the cloud storage */
                    /** Also uploads an object containing file name and download uri to firebase database */

                    /** putFile() uploads the file through its Uri into the online storage */
                    /** addOnSuccessListener() checks if the task has been successfully completed */
                    AudioStorageReference.putFile(AudioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                        /** What to do once response for successful uploading of file to firebase storage is returned */
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            /**
                             * Calling StorageReference.getDownloadUrl() returns a Task,
                             * as it needs to retrieve the download URL from the server.
                             * So you will need a completion listener to get the actual URL
                             */
                            AudioStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri Audiouri) {
                                    /** Creates an instance of custom object containing name and download uri to be uploaded to database */
                                    Firebase_Database_Image_Video_Audio_Upload AudioData = new
                                            Firebase_Database_Image_Video_Audio_Upload(fileName+".3gp",Audiouri.toString());
                                    /**
                                     * Uploads the custom object to the firebase database to a specific location in the database as denoted by child(),
                                     * push() generates a unique key every time a new child is added to the specified Firebase reference
                                     * this is to prevent overwriting of data in database due to data being sent to the same location database
                                     */
                                    mDataBaseReference.child("Album").child("Voice Recordings").push().setValue(AudioData);
                                    AudioUploadDone = true;
                                    Toast.makeText(Upload_File_Firebase.this,"Audio Uploaded Successfully",Toast.LENGTH_SHORT).show();
                                    Log.d(TAG,"Audio uploaded successfully");
                                }
                            });
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            AudioUploadDone = false;
                        }
                    });
                }
                catch(Exception e){
                    Log.e(TAG,"Could not upload audio");
                }
            }

            /** Checks if the Uri points to a video */
            else if (MediaUri.toString().contains("video")) {

                try {
                    /** Upload the image file to the cloud storage */
                    /** Also uploads an object containing file name and download uri to firebase database */

                    /** putFile() uploads the file through its Uri into the online storage */
                    /** addOnSuccessListener() checks if the task has been successfully completed */
                    VideoStorageReference.putFile(MediaUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                        /** What to do once response for successful uploading of file to firebase storage is returned */
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            /**
                             * Calling StorageReference.getDownloadUrl() returns a Task,
                             * as it needs to retrieve the download URL from the server.
                             * So you will need a completion listener to get the actual URL
                             */
                            VideoStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri Videouri) {
                                    /** Creates an instance of custom object containing name and download uri to be uploaded to database */
                                    Firebase_Database_Image_Video_Audio_Upload VideoData = new
                                            Firebase_Database_Image_Video_Audio_Upload(fileName+".mp4",Videouri.toString());
                                    /**
                                     * Uploads the custom object to the firebase database to a specific location in the database as denoted by child(),
                                     * push() generates a unique key every time a new child is added to the specified Firebase reference
                                     * this is to prevent overwriting of data in database due to data being sent to the same location database
                                     */
                                    mDataBaseReference.child("Album").child("Videos").push().setValue(VideoData);
                                    VideoUploadDone = true;
                                    Toast.makeText(Upload_File_Firebase.this,"Video Uploaded Successfully",Toast.LENGTH_SHORT).show();
                                    Log.d(TAG,"Video uploaded successfully");
                                }
                            });
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            VideoUploadDone = false;
                        }
                    });
                }
                catch(Exception e){
                    Log.e(TAG,"Could not upload video");
                }

                try {
                    /** Upload the image file to the cloud storage */
                    /** Also uploads an object containing file name and download uri to firebase database */

                    /** putFile() uploads the file through its Uri into the online storage */
                    /** addOnSuccessListener() checks if the task has been successfully completed */
                    AudioStorageReference.putFile(AudioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                        /** What to do once response for successful uploading of file to firebase storage is returned */
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            /**
                             * Calling StorageReference.getDownloadUrl() returns a Task,
                             * as it needs to retrieve the download URL from the server.
                             * So you will need a completion listener to get the actual URL
                             */
                            AudioStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri Audiouri) {
                                    /** Creates an instance of custom object containing name and download uri to be uploaded to database */
                                    Firebase_Database_Image_Video_Audio_Upload AudioData = new
                                            Firebase_Database_Image_Video_Audio_Upload(fileName+".3gp",Audiouri.toString());
                                    /**
                                     * Uploads the custom object to the firebase database to a specific location in the database as denoted by child(),
                                     * push() generates a unique key every time a new child is added to the specified Firebase reference
                                     * this is to prevent overwriting of data in database due to data being sent to the same location database
                                     */
                                    mDataBaseReference.child("Album").child("Voice Recordings").push().setValue(AudioData);
                                    AudioUploadDone = true;
                                    Toast.makeText(Upload_File_Firebase.this,"Audio Uploaded Successfully",Toast.LENGTH_SHORT).show();
                                    Log.d(TAG,"Audio uploaded successfully");
                                }
                            });
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            AudioUploadDone = false;
                        }
                    });
                }
                catch(Exception e){
                    Log.e(TAG,"Could not upload audio");
                }
            }
        }

        else {
            /** Sends a short message to user what to check before trying to upload again */
            Toast.makeText(this,"Please record an audio, select an image or video" +
                    " and key a file name before uploading",Toast.LENGTH_SHORT).show();
        }
    }
}