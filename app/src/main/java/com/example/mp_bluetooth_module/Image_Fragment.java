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

import Adapter.AlbumImageAdapter;
import Classes.Firebase_Database_Image_Video_Audio_Upload;

public class Image_Fragment extends Fragment {

    /** Initializes variables */
    private static final String TAG = "CheckPoint";
    private RecyclerView FragClassImageRecyclerView;
    private GridLayoutManager twoColumnbyOneRowGridlayout;
    private Query ImageQuery;
    private AlbumImageAdapter ImageAdapter;
    private Firebase_Database_Image_Video_Audio_Upload SelectedImage;


    public Image_Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** Inflates the layout (Means taking the xml layout and renders it) */
        View view = inflater.inflate(R.layout.image_fragment, container, false);
        /** Finds the recycler view in image_fragment.xml and references it */
        FragClassImageRecyclerView = view.findViewById(R.id.ImageRecyclerView);
        return view;
    }

    /** Note that onViewCreated() executes immediately after onCreateView() */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /** Sets a query for all data under "Album/Images/" */
        ImageQuery = FirebaseDatabase.getInstance("https://image-video-album-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("Album").child("Images");

        /** A class by FirebaseUI to make a query and retrieve the appropriate data */
        FirebaseRecyclerOptions<Firebase_Database_Image_Video_Audio_Upload> options
                = new FirebaseRecyclerOptions.Builder<Firebase_Database_Image_Video_Audio_Upload>()
                .setQuery(ImageQuery,Firebase_Database_Image_Video_Audio_Upload.class)
                .build();

        /**
         * Creates a new instance of the AlbumImageAdapter class by passing in the result of the
         * query from FirebaseRecyclerOptions
         */
        ImageAdapter = new AlbumImageAdapter(options);
        /** Sets a listener for clicks on the card layouts in the recycler view */
        ImageAdapter.setOnItemClickListener(new AlbumImageAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(DataSnapshot ImageSnapshot, int ImagePosition) {
                /** Converts the DataSnapshot into an instance of the java class */
                SelectedImage = ImageSnapshot.getValue(Firebase_Database_Image_Video_Audio_Upload.class);
                Log.d(TAG,"Image Position clicked is "+ImagePosition);
                /** Create an intent to go to next activity */
                Intent SelectedImageIntent = new Intent(getContext(), Selected_Firebase_File_Display.class);
                /**
                 * Used to add extra data to the intent to pass it to the next activity.
                 * Note that "name" is used as an identifier when getting the data passed through intent
                 * in the new activity
                 *
                 * For this case, the data being passed is the instance of the java class from
                 * the DataSnapshot
                 */
                SelectedImageIntent.putExtra("Data",SelectedImage);
                /** Starts the intent and go to Selected_Firebase_File_Display activity */
                startActivity(SelectedImageIntent);
            }
        });

        /** Creates an instance of grid layout manager that will implement and display 2 columns per row */
        twoColumnbyOneRowGridlayout = new GridLayoutManager(this.getContext(), 2);
        /** Sets the created instance of grid layout manager into the recycler view */
        FragClassImageRecyclerView.setLayoutManager(twoColumnbyOneRowGridlayout);
        /** Sets the AlbumImageAdapter into the recycler view */
        FragClassImageRecyclerView.setAdapter(ImageAdapter);


    }

    @Override
    public void onStart() {
        super.onStart();
        /**
         * Function to tell the app to start getting data from
         * database on starting of the activity
         */
        ImageAdapter.startListening();
    }


    @Override
    public void onStop() {
        super.onStop();
        /**
         * Function to tell the app to stop getting data from database
         * on stopping of the activity
         */
        if(ImageAdapter != null) {
            ImageAdapter.stopListening();
        }
    }
}
