package com.example.spotifyauthentication;

import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PlaylistUtility {

    private Call mCall;
    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken;
    private String mURI = "";
    public static final String EXTRA_CURRENT_SONG = "PlaylistUtility.Song";
    public static final String EXTRA_CURRENT_ARTIST = "PlaylistUtility.Artist";
    public static final String EXTRA_CURRENT_DUR = "PlaylistUtility.Dur";
    public static final String EXTRA_CURRENT_IMAGE = "PlaylistUtility.ImageUrl";

    public PlaylistUtility(){

    }

    String getPlaylistTracks(String AccessToken, String genre, String numSongs, String popularity){

        mAccessToken = AccessToken;
        System.out.println(genre + numSongs + popularity);
        int offset;
        if(popularity == "high"){
            offset = (int )(Math.random() * 10 + 1);
        }
        else if(popularity == "med"){
            offset = (int )(Math.random() * 3000 + 100);
        }
        else if(popularity == "low"){
            offset = (int )(Math.random() * 3000 + 9900);
        }
        else{
            offset = (int )(Math.random() * 1000 + 1);
        }

        mURI = "https://api.spotify.com/v1/search?q=genre%3A" + genre + "&type=track&market=US&limit=" + numSongs + "&offset=" + offset;
        return mURI;
    }

}
