package com.example.mp_bluetooth_module;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import Adapter.MemoryCardGameAdapter;
import Background_Items.BluetoothBackground;

public class Memory_Card_Game extends AppCompatActivity {

    private int CountPair = 8, currentPosition = -1;
    private ImageView currentView;
    private static final String TAG = "CheckPoint";
    BluetoothBackground Service;
    boolean Bound = false;
    private CountDownTimer InterruptTimer;
    private GridView GameGridView;
    private MemoryCardGameAdapter ImageAdapter;
    private FloatingActionButton GameRefreshFAB, BackFAB;
    final int[] preset_drawable = new int[] {R.drawable.one_img,R.drawable.two_img,R.drawable.three_img,
            R.drawable.four_img,R.drawable.five_img,R.drawable.six_img,R.drawable.seven_img,R.drawable.eight_img};
    private int[] img_position = new int[] {0,1,2,3,4,5,6,7,0,1,2,3,4,5,6,7};
    private List<Integer> img_position_array = new ArrayList<Integer>(img_position.length);
    private String UriArrayFolderPath;
    private File UriArrayPath, UriArrayTxtFile;
    private ArrayList<Uri> SelectedImageUriArray = new ArrayList<Uri>();
    private int[] completedTracker = new int[] {0,0,0,0,0,0,0,0};
    private boolean gameStart = false;
    private TextView countDownText;



    private void hideAllCard(){
        for(int i = 0; i < 16; ++i) {
            ImageView cardImage = ImageAdapter.getCardImageViewByPosition(i);
            Picasso.get().load(R.drawable.hidden_card_img).fit().into(cardImage);
        }
    }

    private void showAllCard(){

        for(int i = 0; i < 16; ++i) {
            ImageView currentView = ImageAdapter.getCardImageViewByPosition(i);
            if (SelectedImageUriArray.size() > img_position_array.get(i)) {
                Picasso.get().load(SelectedImageUriArray.get(img_position_array.get(i))).placeholder(preset_drawable[img_position_array.get(i)]).fit().into(currentView);
            } else {
                Picasso.get().load(preset_drawable[img_position_array.get(i)]).fit().into(currentView);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_card_game);
        gameStart = false;
        UriArrayFolderPath = "Selected Image Uri";

        CheckTxtFileExists();
        SelectedImageUriArray = ReadUriFromTxT();
        if(SelectedImageUriArray.size() >0) {
            Collections.shuffle(SelectedImageUriArray);
        }

        populateArrayList(img_position);
        Collections.shuffle(img_position_array);

        GameGridView = findViewById(R.id.MemoryCardGameGridview);
        GameRefreshFAB = findViewById(R.id.InGameRefreshButton);
        BackFAB = findViewById(R.id.InGameBackButton);
        countDownText = findViewById(R.id.countDown);
        ImageAdapter = new MemoryCardGameAdapter(this);
        GameGridView.setAdapter(ImageAdapter);
        showAllCard();


        new CountDownTimer(5000, 1000){
            public void onTick(long millisUntilFinish){
                countDownText.setText("Game Starts In : "+millisUntilFinish/1000);
            }
            public void onFinish(){
                hideAllCard();
                gameStart = true;
            }
        }.start();

        GameGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(gameStart == false) return;
                if(currentPosition < 0) {
                    if(completedTracker[img_position_array.get(position)] == 1){
                        return;
                    }
                    currentPosition = position;
                    Log.d(TAG,"Selected position is "+position);
                    currentView = (ImageView) view;
                    if(SelectedImageUriArray.size() > img_position_array.get(position)) {
                        Picasso.get().load(SelectedImageUriArray.get(img_position_array.get(position))).placeholder(preset_drawable[img_position_array.get(position)]).fit().into((ImageView) view);
                    }
                    else {
                        Picasso.get().load(preset_drawable[img_position_array.get(position)]).fit().into((ImageView) view);
                    }
                }
                else {
                    // if u click back the same image, close it
                    if(currentPosition == position) {
                        Picasso.get().load(R.drawable.hidden_card_img).fit().into((ImageView) view);
                    }
                    else if(img_position_array.get(currentPosition) != img_position_array.get(position)) {
                        Picasso.get().load(R.drawable.hidden_card_img).fit().into(currentView);
                        //Toast.makeText(Memory_Card_Game.this,"Incorrect Match",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if(SelectedImageUriArray.size() > img_position_array.get(position)) {
                            Picasso.get().load(SelectedImageUriArray.get(img_position_array.get(position))).placeholder(preset_drawable[img_position_array.get(position)]).fit().into((ImageView) view);
                        }
                        else {
                            Picasso.get().load(preset_drawable[img_position_array.get(position)]).fit().into((ImageView) view);
                        }
                        int completedCardNo = img_position_array.get(currentPosition);
                        completedTracker[completedCardNo] = 1;
                        Toast.makeText(Memory_Card_Game.this,"Correct Match",Toast.LENGTH_SHORT).show();
                        CountPair--;

                        if(CountPair ==0 ) {
                            Toast.makeText(Memory_Card_Game.this,"You Win, Please press the reset button to play again",Toast.LENGTH_LONG).show();
                        }
                    }
                    currentPosition = -1;
                }
            }
        });

        BackFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnGameSettingPage();
            }
        });

        GameRefreshFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RefreshGame();
            }
        });
    }


    public void ReturnGameSettingPage() {
        Intent returnIntent = new Intent(this,Game_Start_Setting_Page.class);
        startActivity(returnIntent);
    }

    public void RefreshGame() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    public void populateArrayList(int[] list) {
        for(int i:list) {
            img_position_array.add(i);
        }
    }


    public ArrayList<Uri> ReadUriFromTxT() {

        ArrayList<Uri> UriArrayList = new ArrayList<Uri>();

        ContextWrapper contextWrapper = new ContextWrapper(this);
        UriArrayPath = contextWrapper.getExternalFilesDir(UriArrayFolderPath);
        UriArrayTxtFile = new File(UriArrayPath,"Image_Uri.txt");
        Log.d(TAG,"Reading from "+UriArrayTxtFile.toString());

        try {

            InputStream is = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                is = Files.newInputStream(Paths.get(UriArrayTxtFile.getAbsolutePath()));
            }
            Scanner scanner = new Scanner(is);
            scanner.useDelimiter(",,");
            while (scanner.hasNext()) {
                String i = scanner.next();
                UriArrayList.add(Uri.parse(i));
            }

            //  for(int ii=0;ii<NumberArrayList.size();ii++) {
            //      Log.d(TAG, "From array : "+NumberArrayList.get(ii));
            //  }

            Log.d(TAG, "Read txt file and returned an array of uri successfully");
        } catch (IOException e) {
            Log.e(TAG, "Could not read ArrayList from file, IOException");
        }
        return UriArrayList;
    }


    public void CheckTxtFileExists() {
        ContextWrapper contextWrapper = new ContextWrapper(this);
        UriArrayPath = contextWrapper.getExternalFilesDir(UriArrayFolderPath);
        UriArrayTxtFile = new File(UriArrayPath,"Image_Uri.txt");
        Log.d(TAG,"Checking if "+UriArrayTxtFile.toString()+" exists");

        if(!UriArrayTxtFile.exists()) {
            CreateArrayTextFile("Image_Uri");
        }
        else {
            Log.d(TAG,"Txt file exists");
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
                Log.d(TAG,"Created text file");
            }
            catch (Exception e) {
                Log.e(TAG,"Could not create text file");
            }
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
        InterruptTimer = new CountDownTimer(5000,1000) {

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