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
        View view = inflater.inflate(R.layout.video_fragment,container,false);
        FragClassVideoRecyclerView = view.findViewById(R.id.VideoRecyclerView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        VideoQuery = FirebaseDatabase.getInstance("https://image-video-album-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("Album").child("Videos");

        FirebaseRecyclerOptions<Firebase_Database_Image_Video_Audio_Upload> options
                = new FirebaseRecyclerOptions.Builder<Firebase_Database_Image_Video_Audio_Upload>()
                .setQuery(VideoQuery,Firebase_Database_Image_Video_Audio_Upload.class)
                .build();

        VideoAdapter = new AlbumVideoAdapter(options);
        VideoAdapter.setOnItemClickListener(new AlbumVideoAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(DataSnapshot VideoSnapshot, int VideoPosition) {
                SelectedVideo = VideoSnapshot.getValue(Firebase_Database_Image_Video_Audio_Upload.class);
                Log.d(TAG,"Video Position clicked is "+VideoPosition);
                Intent SelectedVideoIntent = new Intent(getContext(), Selected_Firebase_File_Display.class);
                SelectedVideoIntent.putExtra("Data",SelectedVideo);
                startActivity(SelectedVideoIntent);
            }
        });

        twoColumnbyOneRowGridlayout = new GridLayoutManager(this.getContext(), 2);
        FragClassVideoRecyclerView.setLayoutManager(twoColumnbyOneRowGridlayout);
        FragClassVideoRecyclerView.setAdapter(VideoAdapter);

    }

    /**
     * Function to tell the app to start getting data from
     * database on starting of the activity
     */

    @Override
    public void onStart() {
        super.onStart();
        VideoAdapter.startListening();
    }

    /**
     * Function to tell the app to stop getting data from database
     * on stopping of the activity
     */

    @Override
    public void onStop() {
        super.onStop();
        if(VideoAdapter != null) {
            VideoAdapter.stopListening();
        }
    }
}
