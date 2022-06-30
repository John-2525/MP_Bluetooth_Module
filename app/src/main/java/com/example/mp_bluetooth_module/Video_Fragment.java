package com.example.mp_bluetooth_module;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import Adapter.AlbumVideoAdapter;
import Classes.Firebase_Database_Image_Video_Audio_Upload;

public class Video_Fragment extends Fragment {

    /** Initializes variables */
    private static final String TAG = "CheckPoint";
    private RecyclerView FragClassVideoRecyclerView;
    private GridLayoutManager twoColumnbyOneRowGridlayout;
    private Query VideoQuery;
    private AlbumVideoAdapter VideoAdapter;
    private Firebase_Database_Image_Video_Audio_Upload SelectedVideo;

    public Video_Fragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** Inflates the layout (Means taking the xml layout and renders it) */
        View view = inflater.inflate(R.layout.video_fragment,container,false);
        /** Finds the recycler view in video_fragment.xml and references it */
        FragClassVideoRecyclerView = view.findViewById(R.id.VideoRecyclerView);
        return view;
    }

    /** Note that onViewCreated() executes immediately after onCreateView() */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /** Sets a query for all data under "Album/Videos/" */
        VideoQuery = FirebaseDatabase.getInstance("https://image-video-album-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("Album").child("Videos");

        /** A class by FirebaseUI to make a query and retrieve the appropriate data */
        FirebaseRecyclerOptions<Firebase_Database_Image_Video_Audio_Upload> options
                = new FirebaseRecyclerOptions.Builder<Firebase_Database_Image_Video_Audio_Upload>()
                .setQuery(VideoQuery,Firebase_Database_Image_Video_Audio_Upload.class)
                .build();

        /**
         * Creates a new instance of the AlbumVideoAdapter class by passing in the result of the
         * query from FirebaseRecyclerOptions
         */
        VideoAdapter = new AlbumVideoAdapter(options);
        /** Sets a listener for clicks on the card layouts in the recycler view */
        VideoAdapter.setOnItemClickListener(new AlbumVideoAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(DataSnapshot VideoSnapshot, int VideoPosition) {
                /** Converts the DataSnapshot into an instance of the java class */
                SelectedVideo = VideoSnapshot.getValue(Firebase_Database_Image_Video_Audio_Upload.class);
                Log.d(TAG,"Video Position clicked is "+VideoPosition);
                /** Create an intent to go to next activity */
                Intent SelectedVideoIntent = new Intent(getContext(), Selected_Firebase_File_Display.class);
                /**
                 * Used to add extra data to the intent to pass it to the next activity.
                 * Note that "name" is used as an identifier when getting the data passed through intent
                 * in the new activity
                 *
                 * For this case, the data being passed is the instance of the java class from
                 * the DataSnapshot
                 */
                SelectedVideoIntent.putExtra("Data",SelectedVideo);
                /** Starts the intent and go to Selected_Firebase_File_Display activity */
                startActivity(SelectedVideoIntent);
            }
        });

        /** Creates an instance of grid layout manager that will implement and display 2 columns per row */
        twoColumnbyOneRowGridlayout = new GridLayoutManager(this.getContext(), 2);
        /** Sets the created instance of grid layout manager into the recycler view */
        FragClassVideoRecyclerView.setLayoutManager(twoColumnbyOneRowGridlayout);
        /** Sets the AlbumVideoAdapter into the recycler view */
        FragClassVideoRecyclerView.setAdapter(VideoAdapter);

    }


    @Override
    public void onStart() {
        super.onStart();
        /**
         * Function to tell the app to start getting data from
         * database on starting of the activity
         */
        VideoAdapter.startListening();
    }


    @Override
    public void onStop() {
        super.onStop();
        /**
         * Function to tell the app to stop getting data from database
         * on stopping of the activity
         */
        if(VideoAdapter != null) {
            VideoAdapter.stopListening();
        }
    }
}
