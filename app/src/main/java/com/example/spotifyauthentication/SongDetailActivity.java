package com.example.spotifyauthentication;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class SongDetailActivity extends AppCompatActivity {

    private TextView mSongName;
    private TextView mSongDur;
    private TextView mSongArtist;
    private ImageView mSongImage;


    @Override
    protected  void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail);

        mSongName = findViewById(R.id.song_name);
        mSongDur = findViewById(R.id.song_duration);
        mSongImage = findViewById(R.id.song_image);
        mSongArtist = findViewById(R.id.song_arist);


        Intent intent = getIntent();
        if(intent != null && intent.hasExtra(PlaylistUtility.EXTRA_CURRENT_SONG)){
            mSongName.setText(intent.getStringExtra(PlaylistUtility.EXTRA_CURRENT_SONG));
            mSongDur.setText(intent.getStringExtra(PlaylistUtility.EXTRA_CURRENT_DUR));
            mSongArtist.setText(intent.getStringExtra(PlaylistUtility.EXTRA_CURRENT_ARTIST));
            Glide.with(mSongImage.getContext()).load(intent.getStringExtra(PlaylistUtility.EXTRA_CURRENT_IMAGE)).into(mSongImage);
        }
    }

}
