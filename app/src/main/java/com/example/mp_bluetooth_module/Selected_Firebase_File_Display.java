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

        SelectedVideoView = findViewById(R.id.SelectedFirebaseVideoView);
        SelectedImageView = findViewById(R.id.SelectedFirebaseImageView);
        BackBtn = findViewById(R.id.SelectedImageBackBtn);
        DeleteImageBtn = findViewById(R.id.DeleteSelectedImageBtn);
        PlayAudioBtn = findViewById(R.id.SelectedImagePlayAudioBtn);

        BaseStorageReference = FirebaseStorage.getInstance().getReference().child("Album");
        BaseDatabaseReference = FirebaseDatabase.getInstance("https://image-video-album-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("Album");

        Data = (Firebase_Database_Image_Video_Audio_Upload)
                getIntent().getSerializableExtra("Data");

        if(Data.getFileName().contains("png")) {

            DataIsImage = true;
            DataIsVideo = false;

            SelectedImageView.setVisibility(View.VISIBLE);
            SelectedVideoView.setVisibility(View.INVISIBLE);

            SelectedImageUrl = Data.getFileDownloadUri();
            Picasso.get().load(SelectedImageUrl).into(SelectedImageView);

            ImageOrVideoFileDeleteName = Data.getFileName();

            SelectedImageName = Data.getFileName();
            Log.d(TAG, "Image file name is " + SelectedImageName);
            AudioFileToQuery = SelectedImageName.replace("png", "3gp");
            Log.d(TAG, "Audio file to query is " + AudioFileToQuery);
        }

        else if(Data.getFileName().contains("mp4")) {

            DataIsImage = false;
            DataIsVideo = true;

            SelectedVideoView.setVisibility(View.VISIBLE);
            SelectedImageView.setVisibility(View.INVISIBLE);

            SelectedVideoUrl = Data.getFileDownloadUri();
            SelectedVideoView.setVideoURI(Uri.parse(SelectedVideoUrl));

            VideomediaController = new MediaController(this);
            SelectedVideoView.setMediaController(VideomediaController);
            VideomediaController.setAnchorView(SelectedVideoView);

            ImageOrVideoFileDeleteName = Data.getFileName();

            SelectedVideoName = Data.getFileName();
            Log.d(TAG,"Video file name is "+SelectedVideoName);
            AudioFileToQuery = SelectedVideoName.replace("mp4","3gp");
            Log.d(TAG,"Audio file to query is "+AudioFileToQuery);
        }

        QueryForSelectedAudioFile(AudioFileToQuery);

        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DeletedImageOrVideo&&DeletedAudio) {
                    ReturnToAlbumImage();
                }
                else {
                    Toast.makeText(Selected_Firebase_File_Display.this
                            ,"Please wait for file deletion to be completed",Toast.LENGTH_SHORT).show();
                }
            }
        });

        DeleteImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DeletedImageOrVideo&&DeletedAudio) {
                    Delete_ImageOrVideo_And_Audio_From_Database_And_Storage(ImageOrVideoFileDeleteName, AudioFileToQuery);
                }
                else {
                    Toast.makeText(Selected_Firebase_File_Display.this,"Deleting in progress, Please wait", Toast.LENGTH_SHORT).show();
                }
            }
        });

        PlayAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AudioFilePlayer = new MediaPlayer();
                    AudioFilePlayer.setDataSource(AudioData.getFileDownloadUri());
                    AudioFilePlayer.prepare();
                    AudioFilePlayer.start();
                }
                catch (Exception e) {
                    Log.e(TAG,"Failed to play audio from firebase");
                }
            }
        });

    }

    public void ReturnToAlbumImage() {
        Intent returnAlbumImage = new Intent(this,Image_Video_Album.class);
        startActivity(returnAlbumImage);
    }



    public void QueryForSelectedAudioFile(String AudioFileName) {
        SearchAudioFile = BaseDatabaseReference.child("Voice Recordings");
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
                    AudioData = IndividAudioFile.getValue(Firebase_Database_Image_Video_Audio_Upload.class);
                    Log.d(TAG, "Audio file name in DataSnapshot : " + AudioData.getFileName());
                    if ((AudioData.getFileName()).equals(AudioFileName)) {
                        Log.d(TAG, "Queried file is " + AudioData.getFileName());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Selected_Firebase_File_Display.this, "Failed to get data.", Toast.LENGTH_SHORT).show();
                Log.e(TAG,"Failed to get data");
            }
        });
    }



    public void Delete_ImageOrVideo_And_Audio_From_Database_And_Storage(String DeleteImageOrVideoFileName, String DeleteAudioFileName) {

        DeletedAudio = false;
        DeletedImageOrVideo = false;

        SearchToDeleteAudioRef = BaseDatabaseReference.child("Voice Recordings");
        SearchToDeleteAudioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot TobeDeletedAudioFile : snapshot.getChildren()) {
                    DeleteAudioDataClass = TobeDeletedAudioFile.getValue(Firebase_Database_Image_Video_Audio_Upload.class);
                    if ((DeleteAudioDataClass.getFileName()).equals(DeleteAudioFileName)) {
                        TobeDeletedAudioFile.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(TAG, "Deleted Audio file from realtime database");

                                AudioStorageRef = BaseStorageReference.child("Voice Recordings").child(DeleteAudioFileName);
                                AudioStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Unable to get Audio data for deletion");
            }
        });


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