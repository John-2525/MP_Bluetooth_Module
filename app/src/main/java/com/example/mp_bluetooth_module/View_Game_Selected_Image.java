package com.example.mp_bluetooth_module;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import Adapter.SelectedGameImageAdapter;
import Background_Items.BluetoothBackground;

public class View_Game_Selected_Image extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private RecyclerView SelectedGameImageRecyclerView;
    private FloatingActionButton BackFAB, GalleryFAB;
    private static final String TAG = "CheckPoint";
    BluetoothBackground Service;
    boolean Bound = false;
    private CountDownTimer InterruptTimer;
    private int SELECT_PICTURE = 200;
    private Uri SelectedImageUri, ToBeRemovedUri;
    private static final int PERMISSION_REQUEST_CODE = 7;
    private File UriArrayTxtFolder, UriArrayTxtFile, UriArrayPath;
    private String UriArrayFolderPath;
    private ArrayList<Uri> UriArrayList = new ArrayList<Uri>();
    private GridLayoutManager gridLayoutManager;
    private SelectedGameImageAdapter SGIA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_game_selected_image);

        UriArrayFolderPath = "Selected Image Uri";

        CreateFolderAndGetExternalPermission();
        createDirectory(UriArrayFolderPath,UriArrayTxtFolder);
        CreateArrayTextFile("Image_Uri");

        SelectedGameImageRecyclerView = findViewById(R.id.SelectedGameImageRecyclerView);
        BackFAB = findViewById(R.id.SelectedGameImageBackfloatingActionButton);
        GalleryFAB = findViewById(R.id.SelectedGameImageGalleryfloatingActionButton);

        gridLayoutManager = new GridLayoutManager(this,2);
        SGIA = new SelectedGameImageAdapter(ReadUriFromTxT());

        SGIA.setOnItemClickListener(new SelectedGameImageAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int Position, View v) {
                ArrayList<Uri> TobeRemoved = ReadUriFromTxT();
                ToBeRemovedUri = TobeRemoved.get(Position);
                showPopupMenu(v);
            }
        });

        SelectedGameImageRecyclerView.setLayoutManager(gridLayoutManager);
        SelectedGameImageRecyclerView.setAdapter(SGIA);

        BackFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoBackToGameStartSettingPage();
            }
        });

        GalleryFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickImage();
            }
        });
    }


    public void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this,v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.remove_custom_game_image_popup_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.RemoveCustomImageMenuOption:

                ArrayList<Uri> UriArrayList = ReadUriFromTxT();
                for(Uri ImageUri:UriArrayList) {
                    if(ImageUri.equals(ToBeRemovedUri)) {
                        UriArrayList.remove(ImageUri);
                        SaveArrayListToSD(UriArrayList);
                        SGIA = new SelectedGameImageAdapter(ReadUriFromTxT());
                        SGIA.setOnItemClickListener(new SelectedGameImageAdapter.OnItemClickListener() {
                            @Override
                            public void OnItemClick(int Position, View v) {
                                ArrayList<Uri> TobeRemoved = ReadUriFromTxT();
                                ToBeRemovedUri = TobeRemoved.get(Position);
                                showPopupMenu(v);
                            }
                        });
                        SelectedGameImageRecyclerView.setAdapter(SGIA);
                        break;
                    }
                }
                return true;

            default:
                return false;
        }
    }


    public void GoBackToGameStartSettingPage() {
        Intent returnIntent = new Intent(this,Game_Start_Setting_Page.class);
        startActivity(returnIntent);
    }


    public void PickImage() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null) {
            if(requestCode == SELECT_PICTURE) {
                SelectedImageUri = data.getData();
                UriArrayList = ReadUriFromTxT();
                UriArrayList.add(SelectedImageUri);
                SaveArrayListToSD(UriArrayList);
                SGIA = new SelectedGameImageAdapter(UriArrayList);
                SGIA.setOnItemClickListener(new SelectedGameImageAdapter.OnItemClickListener() {
                    @Override
                    public void OnItemClick(int Position, View v) {
                        ArrayList<Uri> TobeRemoved = ReadUriFromTxT();
                        ToBeRemovedUri = TobeRemoved.get(Position);
                        showPopupMenu(v);
                    }
                });
                SelectedGameImageRecyclerView.setAdapter(SGIA);
            }
        }
    }

    /** Creates an empty text file to help store and track integers used for pendingIntents' request code */
    public void CreateArrayTextFile(String fileName) {
        ContextWrapper contextWrapper = new ContextWrapper(this);
        UriArrayPath = contextWrapper.getExternalFilesDir(UriArrayFolderPath);
        UriArrayTxtFile = new File(UriArrayPath, fileName+".txt");
        if(!UriArrayTxtFile.exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(UriArrayTxtFile);
                PrintWriter pw = new PrintWriter(fos);
                pw.close();
                fos.close();
            }
            catch (Exception e) {
                Log.e(TAG,"Could not create text file");
            }
        }
    }

    public void SaveArrayListToSD(ArrayList<Uri> NewUriArray) {
        try {
            FileWriter fw = new FileWriter(UriArrayTxtFile,false);
            PrintWriter pw = new PrintWriter(fw);
            for(Uri uri:NewUriArray) {
                pw.write(uri+",,");
                Log.d(TAG,"Written "+uri+" to the txt file successfully");
            }
            pw.close();
            fw.close();
        }
        catch (Exception e) {
            Log.e(TAG,"Could not save uri to txt file");
        }
    }


    public ArrayList<Uri> ReadUriFromTxT() {

        ArrayList<Uri> UriArrayList = new ArrayList<Uri>();
        try {

            InputStream is = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                is = Files.newInputStream(Paths.get(UriArrayTxtFile.getAbsolutePath()));
            }
            Scanner scanner = new Scanner(is);
            scanner.useDelimiter(",,");
            while(scanner.hasNext()) {
                String i = scanner.next();
                UriArrayList.add(Uri.parse(i));
            }

            //  for(int ii=0;ii<NumberArrayList.size();ii++) {
            //      Log.d(TAG, "From array : "+NumberArrayList.get(ii));
            //  }

            Log.d(TAG, "Read txt file and returned an array of uri successfully");
        }
        catch (IOException e) {
            Log.e(TAG,"Could not read ArrayList from file, IOException");
        }
        return UriArrayList;
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
                createDirectory(UriArrayFolderPath, UriArrayTxtFolder);
            }
        }
        /** If users dont give permission to write to external storage */
        else {
            Toast.makeText(View_Game_Selected_Image.this,"Permission Denied",Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    /** Function to check if permission to write external storage is given when coming to this activity */
    private void CreateFolderAndGetExternalPermission() {
        /** Checks if permission to write to external storage is granted */
        if(ContextCompat.checkSelfPermission(View_Game_Selected_Image.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            createDirectory(UriArrayFolderPath, UriArrayTxtFolder);
        }

        else {
            /** Gets permission to do so if it is not granted */
            askExtStoragePermission();
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
            Toast.makeText(View_Game_Selected_Image.this, "Error "+Folder_Name+" folder not made", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(View_Game_Selected_Image.this, "Folder Made/Exists", Toast.LENGTH_SHORT).show();
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