package com.example.mp_bluetooth_module;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

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
import com.squareup.picasso.Picasso;

import Background_Items.BluetoothBackground;
import Classes.Firebase_Database_Image_Video_Audio_Upload;

public class Selected_Firebase_File_Display extends AppCompatActivity {

    /** Initializes variables */
    private static final String TAG = "CheckPoint";
    private CountDownTimer InterruptTimer;
    BluetoothBackground Service;
    boolean Bound = false, DeletedAudio = true, DeletedImageOrVideo = true,
            DataIsImage = false, DataIsVideo = false;
    private FloatingActionButton BackBtn, DeleteImageBtn;
    private Button PlayAudioBtn;
    private ImageView SelectedImageView;
    private VideoView SelectedVideoView;
    private MediaController VideomediaController;
    private Firebase_Database_Image_Video_Audio_Upload Data, AudioData, DeleteAudioDataClass
            , DeleteImageDataClass, DeleteVideoDataClass;
    private String SelectedImageName,SelectedImageUrl,AudioFileToQuery, ActualImageFileName
            , SelectedVideoName, SelectedVideoUrl, ImageOrVideoFileDeleteName;
    private Query SearchAudioFile, SearchToDeleteAudioRef, SearchToDeleteImageRef, SearchToDeleteVideoRef;
    private MediaPlayer AudioFilePlayer;
    private DatabaseReference BaseDatabaseReference;
    private StorageReference AudioStorageRef, ImageStorageRef, VideoStorageRef, BaseStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_firebase_file);

        /** Finds the view by the ID given and sets it to the variable */
        SelectedVideoView = findViewById(R.id.SelectedFirebaseVideoView);
        SelectedImageView = findViewById(R.id.SelectedFirebaseImageView);
        BackBtn = findViewById(R.id.SelectedImageBackBtn);
        DeleteImageBtn = findViewById(R.id.DeleteSelectedImageBtn);
        PlayAudioBtn = findViewById(R.id.SelectedImagePlayAudioBtn);

        /**
         * Create and initialize part of the references to Firebase Storage and Realtime database
         * for easier and shorter references below
         */
        BaseStorageReference = FirebaseStorage.getInstance().getReference().child("Album");
        BaseDatabaseReference = FirebaseDatabase.getInstance("https://image-video-album-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("Album");

        /** Gets the java class data passed through intent from Image_Fragment.java or Video_Fragment.java */
        Data = (Firebase_Database_Image_Video_Audio_Upload)
                getIntent().getSerializableExtra("Data");

        /** Checks if java class data is of an image */
        if(Data.getFileName().contains("png")) {

            /** Sets the boolean checks for later */
            DataIsImage = true;
            DataIsVideo = false;

            /** Sets the image view visible and video view invisible */
            SelectedImageView.setVisibility(View.VISIBLE);
            SelectedVideoView.setVisibility(View.INVISIBLE);

            /** Gets and stores the image download url from the java class data */
            SelectedImageUrl = Data.getFileDownloadUri();
            /** Loads the image into the image view using the download url */
            Picasso.get().load(SelectedImageUrl).into(SelectedImageView);

            /**
             * Gets and stores file name from the the java class data for
             * referencing later if this file is to be deleted
             */
            ImageOrVideoFileDeleteName = Data.getFileName();

            /** Gets and stores file name from the the java class data */
            SelectedImageName = Data.getFileName();
            Log.d(TAG, "Image file name is " + SelectedImageName);
            /**
             * Modifying the image file name into the audio file name later when
             * searching for the corresponding audio file
             */
            AudioFileToQuery = SelectedImageName.replace("png", "3gp");
            Log.d(TAG, "Audio file to query is " + AudioFileToQuery);
        }

        /** Checks if java class data is of a video */
        else if(Data.getFileName().contains("mp4")) {

            /** Sets the boolean checks for later */
            DataIsImage = false;
            DataIsVideo = true;

            /** Sets the video view visible and image view invisible */
            SelectedVideoView.setVisibility(View.VISIBLE);
            SelectedImageView.setVisibility(View.INVISIBLE);

            /** Gets and stores the video download url from the java class data */
            SelectedVideoUrl = Data.getFileDownloadUri();
            /**
             * Loads the video into the video view by converting the video download
             * url into Uri
             */
            SelectedVideoView.setVideoURI(Uri.parse(SelectedVideoUrl));

            /** Create new instance of media controller to control the video in video view */
            VideomediaController = new MediaController(this);
            /** Sets the media controller into the video view in the xml layout */
            SelectedVideoView.setMediaController(VideomediaController);
            /** Sets the overlay of the media controls using the parent of VideoView */
            VideomediaController.setAnchorView(SelectedVideoView);

            /**
             * Gets and stores file name from the the java class data for
             * referencing later if this file is to be deleted
             */
            ImageOrVideoFileDeleteName = Data.getFileName();

            /** Gets and stores file name from the the java class data */
            SelectedVideoName = Data.getFileName();
            Log.d(TAG,"Video file name is "+SelectedVideoName);
            /**
             * Modifying the image file name into the audio file name later when
             * searching for the corresponding audio file
             */
            AudioFileToQuery = SelectedVideoName.replace("mp4","3gp");
            Log.d(TAG,"Audio file to query is "+AudioFileToQuery);
        }

        /** See below for how it works */
        QueryForSelectedAudioFile(AudioFileToQuery);

        /** Sets a listener for any clicks on the back button */
        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Check is to prevent the user from exiting this current activity
                 * while the deletion of data is in progress
                 * This is to prevent any interruption and causing incomplete deleting
                 * of the file
                 */
                if(DeletedImageOrVideo&&DeletedAudio) {
                    /** See below */
                    ReturnToAlbumImage();
                }
                else {
                    Toast.makeText(Selected_Firebase_File_Display.this
                            ,"Please wait for file deletion to be completed",Toast.LENGTH_SHORT).show();
                }
            }
        });

        /** Sets a listener for any click on the delete button */
        DeleteImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Check is to prevent user from spamming delete button while the
                 * delete function is in progress to prevent any errors from occurring
                 * like null reference error for trying to delete non-existent data
                 */
                if(DeletedImageOrVideo&&DeletedAudio) {
                    /** See below */
                    Delete_ImageOrVideo_And_Audio_From_Database_And_Storage(ImageOrVideoFileDeleteName, AudioFileToQuery);
                }
                else {
                    Toast.makeText(Selected_Firebase_File_Display.this,"Deleting in progress, Please wait", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /** Sets a listener for any click on the play audio button */
        PlayAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    /**
                     * Refer to https://developer.android.com/reference/android/media/MediaPlayer
                     * or Upload_File_Firebase.java for the order of the functions in media player are to be called
                     */

                    /** Create new instance of media player */
                    AudioFilePlayer = new MediaPlayer();
                    /**
                     * Sets the data source of the corresponding audio file using its download url
                     * which is obtained from QueryForSelectedAudioFile() function below
                     */
                    AudioFilePlayer.setDataSource(AudioData.getFileDownloadUri());
                    /** Prepares the player for playback */
                    AudioFilePlayer.prepare();
                    /** Starts the playback of the audio file */
                    AudioFilePlayer.start();
                }
                catch (Exception e) {
                    Log.e(TAG,"Failed to play audio from firebase");
                }
            }
        });

    }

    /** Function is to return to the Image_Video_Album activity using intent */
    public void ReturnToAlbumImage() {
        Intent returnAlbumImage = new Intent(this,Image_Video_Album.class);
        startActivity(returnAlbumImage);
    }


    /**
     * Searches and returns the corresponding audio file of the java data class
     * passed to this activity through intent using the corresponding audio's file name
     */
    public void QueryForSelectedAudioFile(String AudioFileName) {
        /** Creates a query for all data under "Album/Voice Recordings/" */
        SearchAudioFile = BaseDatabaseReference.child("Voice Recordings");
        /**
         * addListenerForSingleValueEvent() is used to listen to the query/database reference above once
         * and returns with the data from the queried location/database reference
         */
        SearchAudioFile.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                /**
                 * DataSnapShot snapshot returns a list of all data under the child reference
                 * from .getReference().getChild().getChild(),....
                 *
                 * So a for loop is needed to scan through the list of data to determine which data
                 * is the corresponding audio file to the image or video
                 *
                 * getChildren() =  Gives access to all of the immediate children of this snapshot
                 */
                for(DataSnapshot IndividAudioFile : snapshot.getChildren()) {
                    /** Converts the DataSnapshot into an instance of the java class */
                    AudioData = IndividAudioFile.getValue(Firebase_Database_Image_Video_Audio_Upload.class);
                    Log.d(TAG, "Audio file name in DataSnapshot : " + AudioData.getFileName());
                    /**
                     * Checks if the file name of the DataSnapshot children is the same as the file name
                     * of the corresponding audio file
                     */
                    if ((AudioData.getFileName()).equals(AudioFileName)) {
                        /** If it matches, it exits the for loop immediately */
                        Log.d(TAG, "Queried file is " + AudioData.getFileName());
                        break;
                    }
                }
            }

            /** A Function that logs in the event it is unable to return the data queried from the query/database reference */
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Selected_Firebase_File_Display.this, "Failed to get data.", Toast.LENGTH_SHORT).show();
                Log.e(TAG,"Failed to get data");
            }
        });
    }


    /**
     * Deletes the image/video and its corresponding audio file from Firebase storage and realtime database
     * using the file name of the image/video file and corresponding audio file
     */
    public void Delete_ImageOrVideo_And_Audio_From_Database_And_Storage(String DeleteImageOrVideoFileName, String DeleteAudioFileName) {

        /**
         * boolean check to prevent users from spamming delete button or exiting
         * from this activity while this function is still executing
         */
        DeletedAudio = false;
        DeletedImageOrVideo = false;

        /** Creates a query for all data under "Album/Voice Recordings/" */
        SearchToDeleteAudioRef = BaseDatabaseReference.child("Voice Recordings");
        /**
         * addListenerForSingleValueEvent() is used to listen to the query/database reference above once
         * and returns with the data from the queried location/database reference
         */
        SearchToDeleteAudioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                /**
                 * DataSnapShot snapshot returns a list of all data under the child reference
                 * from .getReference().getChild().getChild(),....
                 *
                 * So a for loop is needed to scan through the list of data to determine which data
                 * is the corresponding audio file to the image or video
                 *
                 * getChildren() =  Gives access to all of the immediate children of this snapshot
                 */
                for (DataSnapshot TobeDeletedAudioFile : snapshot.getChildren()) {
                    /** Converts the DataSnapshot into an instance of the java class */
                    DeleteAudioDataClass = TobeDeletedAudioFile.getValue(Firebase_Database_Image_Video_Audio_Upload.class);
                    /**
                     * Checks if the file name of the DataSnapshot children is the same as the
                     * file name of the to be deleted audio file
                     */
                    if ((DeleteAudioDataClass.getFileName()).equals(DeleteAudioFileName)) {
                        /**
                         * If so, it gets the realtime database reference of the particular DataSnapshot children
                         * and remove it
                         *
                         * An onSuccessListener is added to check if the removal of the data from database is
                         * successful
                         *
                         * An onFailureListener is added to execute a log in the event the removal of the data
                         * from the database is unsuccessful
                         */
                        TobeDeletedAudioFile.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                /**
                                 * If removal of the data is successful from the database, it will delete the same
                                 * data but from Firebase Storage
                                 */
                                Log.d(TAG, "Deleted Audio file from realtime database");

                                /**
                                 * Create reference to the particular to be deleted audio file in Firebase Storage using the
                                 * to be deleted audio file name
                                 */
                                AudioStorageRef = BaseStorageReference.child("Voice Recordings").child(DeleteAudioFileName);
                                /**
                                 * Deletes the audio file using the Firebase Storage reference to the audio file
                                 *
                                 * An onSuccessListener is added to check if the deletion of the audio file from
                                 * Firebase Storage is successful
                                 *
                                 * An onFailureListener is added to execute a log in the event the removal of the audio
                                 * file from Firebase Storage is unsuccessful
                                 */
                                AudioStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        /**
                                         * If deletion of the file is successful,
                                         * the boolean check is set back to true
                                         */
                                        Log.d(TAG, "Deleted Audio file from online storage");
                                        DeletedAudio = true;
                                        Log.d(TAG, String.valueOf(DeletedAudio));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "Failed to delete Audio file from online storage");
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Failed to delete Audio file from realtime database");
                            }
                        });
                    }
                }
            }

            /** Function that executes a log in the event that it cannot get the data from query/database reference */
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Unable to get Audio data for deletion");
            }
        });


        /**
         * Checks if the data to be deleted is an image
         *
         * The rest of the code within if(DataIsImage) {...} is the same as the code
         * for deleting the corresponding audio file from Firebase Storage and realtime
         * database (above) but with some slight changes to the variables and statements
         */
        if (DataIsImage) {
            SearchToDeleteImageRef = BaseDatabaseReference.child("Images");
            SearchToDeleteImageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot TobeDeletedImageFile : snapshot.getChildren()) {
                        DeleteImageDataClass = TobeDeletedImageFile.getValue(Firebase_Database_Image_Video_Audio_Upload.class);
                        if ((DeleteImageDataClass.getFileName()).equals(DeleteImageOrVideoFileName)) {
                            TobeDeletedImageFile.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "Deleted Image file from realtime database");

                                    ActualImageFileName = DeleteImageOrVideoFileName.replace("png", "jpg");
                                    ImageStorageRef = BaseStorageReference.child("Images").child(ActualImageFileName);
                                    ImageStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d(TAG, "Deleted Image file from online storage");
                                            DeletedImageOrVideo = true;
                                            Log.d(TAG, String.valueOf(DeletedImageOrVideo));
                                            Toast.makeText(Selected_Firebase_File_Display.this,
                                                    "Image And Audio Deleted from online storage and database successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "Failed to Delete Image file from online storage");
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Failed to delete Image file from realtime database");
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Unable to get Image data for deletion");
                }
            });
        }

        /**
         * Checks if the data to be deleted is a video
         *
         * The rest of the code within if(DataIsVideo) {...} is the same as the code
         * for deleting the corresponding audio file from Firebase Storage and realtime
         * database (above) but with some slight changes to the variables and statements
         */
        if(DataIsVideo) {
            SearchToDeleteVideoRef = BaseDatabaseReference.child("Videos");
            SearchToDeleteVideoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot TobeDeletedVideoFile: snapshot.getChildren()) {
                        DeleteVideoDataClass = TobeDeletedVideoFile.getValue(Firebase_Database_Image_Video_Audio_Upload.class);
                        if((DeleteVideoDataClass.getFileName()).equals(DeleteImageOrVideoFileName)) {
                            TobeDeletedVideoFile.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "Deleted Video file from realtime database");

                                    VideoStorageRef = BaseStorageReference.child("Videos").child(DeleteImageOrVideoFileName);
                                    VideoStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d(TAG, "Deleted Video file from online storage");
                                            DeletedImageOrVideo = true;
                                            Log.d(TAG, String.valueOf(DeletedImageOrVideo));
                                            Toast.makeText(Selected_Firebase_File_Display.this,
                                                    "Video And Audio Deleted from online storage and database successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "Failed to Delete Video file from online storage");
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Failed to delete Video file from realtime database");
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Unable to get Video data for deletion");
                }
            });
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