package com.example.pantry;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

public class TutorialsPage extends Fragment {
    View view;

    public TutorialsPage() {
        // Required empty public constructor
    }

    private VideoView videoView;
    private Button storeroom, addItemM, addItemB, findRecipe, back;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tutorials_page, container, false);
        videoView = (VideoView) view.findViewById(R.id.videoView);
        storeroom = view.findViewById(R.id.storeroom);
        addItemM = view.findViewById(R.id.addingManual);
        addItemB=view.findViewById(R.id.addingBarcode);
        findRecipe = view.findViewById(R.id.finding);
        back = view.findViewById(R.id.back);
        back.setOnClickListener(listener);
        storeroom.setOnClickListener(listener);
        addItemM.setOnClickListener(listener);
        addItemB.setOnClickListener(listener);
        findRecipe.setOnClickListener(listener);

        //by default, show how to use storeroom video
        videoView.setVideoURI(Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.storeroom));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                videoView.start();
            }
        });

        return view;
    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.storeroom:
                    Toast.makeText(getContext(), "Playing: Storeroom Tutorial", Toast.LENGTH_SHORT).show();
                    videoView.setVideoURI(Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.storeroom));
                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.setLooping(true);
                            videoView.start();
                        }
                    });
                    break;
                case R.id.finding:
                    Toast.makeText(getContext(), "Playing: Finding Recipes Tutorial", Toast.LENGTH_SHORT).show();
                    videoView.setVideoURI(Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.recipes));
                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.setLooping(true);
                            videoView.start();
                        }
                    });
                    break;
                case R.id.addingManual:
                    Toast.makeText(getContext(), "Playing: Adding Products Manually Tutorial", Toast.LENGTH_SHORT).show();
                    videoView.setVideoURI(Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.manually));
                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.setLooping(true);
                            videoView.start();
                        }
                    });
                    break;
                case R.id.addingBarcode:
                    Toast.makeText(getContext(), "Playing: Adding Products by Barcode Tutorial", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.back:
                    Fragment home = new Home();
                    AppCompatActivity reload = (AppCompatActivity) view.getContext();
                    reload.getSupportFragmentManager().beginTransaction().replace(R.id.frame, home).addToBackStack(null).commit();
                    break;

            }
        }
    };

}