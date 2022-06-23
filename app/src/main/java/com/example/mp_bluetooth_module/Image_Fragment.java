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
        View view = inflater.inflate(R.layout.image_fragment, container, false);
        FragClassImageRecyclerView = view.findViewById(R.id.ImageRecyclerView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageQuery = FirebaseDatabase.getInstance("https://image-video-album-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("Album").child("Images");

        FirebaseRecyclerOptions<Firebase_Database_Image_Video_Audio_Upload> options
                = new FirebaseRecyclerOptions.Builder<Firebase_Database_Image_Video_Audio_Upload>()
                .setQuery(ImageQuery,Firebase_Database_Image_Video_Audio_Upload.class)
                .build();

        ImageAdapter = new AlbumImageAdapter(options);
        ImageAdapter.setOnItemClickListener(new AlbumImageAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(DataSnapshot ImageSnapshot, int ImagePosition) {
                SelectedImage = ImageSnapshot.getValue(Firebase_Database_Image_Video_Audio_Upload.class);
                Log.d(TAG,"Image Position clicked is "+ImagePosition);
                Intent SelectedImageIntent = new Intent(getContext(), Selected_Firebase_File_Display.class);
                SelectedImageIntent.putExtra("Data",SelectedImage);
                startActivity(SelectedImageIntent);
            }
        });

        twoColumnbyOneRowGridlayout = new GridLayoutManager(this.getContext(), 2);
        FragClassImageRecyclerView.setLayoutManager(twoColumnbyOneRowGridlayout);
        FragClassImageRecyclerView.setAdapter(ImageAdapter);

    }

    /**
     * Function to tell the app to start getting data from
     * database on starting of the activity
     */

    @Override
    public void onStart() {
        super.onStart();
        ImageAdapter.startListening();
    }

    /**
     * Function to tell the app to stop getting data from database
     * on stopping of the activity
     */

    @Override
    public void onStop() {
        super.onStop();
        if(ImageAdapter != null) {
            ImageAdapter.stopListening();
        }
    }
}
