package com.example.spotifyauthentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, PlaylistAdapter.OnPlaylistItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String CLIENT_ID = "4b4b430bd9d743fa9a00bfb99caa671c";
    public static final int AUTH_TOKEN_REQUEST_CODE = 0x10;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private RecyclerView mPlaylistItemsRV;
    private PlaylistAdapter mPlaylistAdapter;
    private String mAccessToken;
    private Call mCall;
    private TextView mDisplayName;
    private String mdisplayPic;
    private String mdisplayName;
    private DrawerLayout mDrawerLayout;
    private String mPlaylistName;
    private int mLogout = 0;
    private int mInit = 0;
    private ImageView mDisplayPic;
    private String mGenre;
    private String mPopularity;
    private String mNumSongs;
    private ArrayList<String> mArtists = new ArrayList<String>();
    private ArrayList<String> mTracks = new ArrayList<String>();
    private ArrayList<String> mDuration = new ArrayList<String>();
    private ArrayList<String> mID = new ArrayList<String>();
    private ArrayList<String> mImageURL = new ArrayList<String>();
    private ProgressBar mLoadingIndicatorPB;
    private TextView mLoadingErrorMessageTV;
    private static final String SEARCH_TRACK_LIST_KEY = "searchtrackList";
    private static final String SEARCH_ARTIST_LIST_KEY = "searchartistList";
    private static final String SEARCH_DURATION_LIST_KEY = "searchdurationList";
    private static final String SEARCH_ID_LIST_KEY = "searchidList";
    private static final String SEARCH_IMAGE_LIST_KEY = "searchimageList";
    private static final String SEARCH_PLAYLIST_LIST_KEY = "searchplaylistList";
    private static final String SEARCH_ACCESS_LIST_KEY = "searchaccessList";
    private static final String SEARCH_DISPLAY_LIST_KEY = "searchdisplayList";
    private static final String SEARCH_PIC_LIST_KEY = "searchpicList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLoadingIndicatorPB = findViewById(R.id.pb_loading_indicator);
        mLoadingErrorMessageTV = findViewById(R.id.tv_loading_error_message);
        mLoadingErrorMessageTV.setVisibility(View.INVISIBLE);
        mLoadingIndicatorPB.setVisibility(View.INVISIBLE);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nv_nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPlaylistItemsRV = findViewById(R.id.rv_playlist_items);
        mPlaylistItemsRV.setVisibility(View.INVISIBLE);
        mPlaylistAdapter = new PlaylistAdapter(this);
        mPlaylistItemsRV.setAdapter(mPlaylistAdapter);
        mPlaylistItemsRV.setLayoutManager(new LinearLayoutManager(this));
        mPlaylistItemsRV.setHasFixedSize(true);


        if (savedInstanceState != null && savedInstanceState.containsKey(SEARCH_TRACK_LIST_KEY)) {
            mAccessToken = (String)savedInstanceState.getString(SEARCH_ACCESS_LIST_KEY);
            mTracks = (ArrayList)savedInstanceState.getStringArrayList(SEARCH_TRACK_LIST_KEY);
            if(mAccessToken != null){
                mdisplayName = (String)savedInstanceState.getString(SEARCH_DISPLAY_LIST_KEY);
                setResponse(mdisplayName);
                mdisplayPic = (String)savedInstanceState.getString(SEARCH_PIC_LIST_KEY);
                setPicture(mdisplayPic);
            }
            if(mAccessToken != null && mTracks.size() >= 1){
                mPlaylistItemsRV.setVisibility(View.VISIBLE);
                ImageView spotifyImage = findViewById(R.id.spot_img);
                spotifyImage.setVisibility(View.INVISIBLE);
                TextView randifyText = findViewById(R.id.randify_text);
                randifyText.setVisibility(View.INVISIBLE);
                TextView playlistText = findViewById(R.id.playlist_text);
                playlistText.setVisibility(View.VISIBLE);
                playlistText.setBackgroundColor(Color.LTGRAY);
                mPlaylistName = (String)savedInstanceState.getString(SEARCH_PLAYLIST_LIST_KEY);
                playlistText.setText(mPlaylistName);
                mArtists = (ArrayList)savedInstanceState.getStringArrayList(SEARCH_ARTIST_LIST_KEY);
                mDuration = (ArrayList)savedInstanceState.getStringArrayList(SEARCH_DURATION_LIST_KEY);
                mID = (ArrayList)savedInstanceState.getStringArrayList(SEARCH_ID_LIST_KEY);
                mImageURL = (ArrayList)savedInstanceState.getStringArrayList(SEARCH_IMAGE_LIST_KEY);
                mPlaylistAdapter.updatePlaylistItems(mTracks, mDuration, mArtists, mImageURL);
            }
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_nav_menu);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mGenre = preferences.getString("key_genre", "rap");
        mNumSongs = preferences.getString("key_numsongs", "50");
        mPopularity = preferences.getString("key_popu", "high");
        mPlaylistName = preferences.getString("key_name", "Default");
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mTracks != null) {
            outState.putStringArrayList(SEARCH_TRACK_LIST_KEY,
                    mTracks);
        }
        if (mArtists != null) {
            outState.putStringArrayList(SEARCH_ARTIST_LIST_KEY,
                    mArtists);
        }
        if (mDuration != null) {
            outState.putStringArrayList(SEARCH_DURATION_LIST_KEY,
                    mDuration);
        }
        if (mID != null) {
            outState.putStringArrayList(SEARCH_ID_LIST_KEY,
                    mID);
        }
        if (mImageURL != null) {
            outState.putStringArrayList(SEARCH_IMAGE_LIST_KEY,
                    mImageURL);
        }
        if(mPlaylistName != null){
            outState.putString(SEARCH_PLAYLIST_LIST_KEY, mPlaylistName);
        }
        if(mAccessToken != null){
            outState.putString(SEARCH_ACCESS_LIST_KEY, mAccessToken);
        }
        if(mdisplayName != null){
            outState.putString(SEARCH_DISPLAY_LIST_KEY, mdisplayName);
        }
        if(mdisplayPic != null){
            outState.putString(SEARCH_PIC_LIST_KEY, mdisplayPic);
        }
    }

    @Override
    protected void onDestroy() {
        cancelCall();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_spotify, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login:
                onRequestTokenClicked();
                mLogout = 0;
                return true;
            case android.R.id.home:
                if(mAccessToken != null){
                    NavigationView navigationView = (NavigationView) findViewById(R.id.nv_nav_drawer);
                    Menu nav_Menu = navigationView.getMenu();
                    nav_Menu.findItem(R.id.nav_login).setVisible(false);
                    nav_Menu.findItem(R.id.nav_logout).setVisible(true);
                }
                else{
                    NavigationView navigationView = (NavigationView) findViewById(R.id.nv_nav_drawer);
                    Menu nav_Menu = navigationView.getMenu();
                    nav_Menu.findItem(R.id.nav_login).setVisible(true);
                    nav_Menu.findItem(R.id.nav_logout).setVisible(false);
                }
                mDrawerLayout.openDrawer(Gravity.START);
                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_spot:
                Intent spotifyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("spotify:"));
                if(spotifyIntent.resolveActivity(getPackageManager()) != null){
                    startActivity(spotifyIntent);
                }
                else{
                    AuthenticationClient.openDownloadSpotifyActivity(this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onRequestTokenClicked() {
        final AuthenticationRequest request = getAuthenticationRequest(AuthenticationResponse.Type.TOKEN);
        AuthenticationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request);

    }

    private AuthenticationRequest getAuthenticationRequest(AuthenticationResponse.Type type) {
        return new AuthenticationRequest.Builder(CLIENT_ID, type, getRedirectUri())
                .setShowDialog(false)
                .setScopes(new String[]{"user-read-email"})
                .setCampaign("your-campaign-token")
                .build();
    }

    private AuthenticationRequest getAuthenticationLogoutRequest(AuthenticationResponse.Type type) {
        return new AuthenticationRequest.Builder(CLIENT_ID, type, getRedirectUri())
                .setShowDialog(true)
                .setScopes(new String[]{"user-read-email"})
                .setCampaign("your-campaign-token")
                .build();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

        if (AUTH_TOKEN_REQUEST_CODE == requestCode) {
            mAccessToken = response.getAccessToken();
            final Request request = new Request.Builder()
                    .url("https://api.spotify.com/v1/me")
                    .addHeader("Authorization","Bearer " + mAccessToken)
                    .build();

            cancelCall();
            mCall = mOkHttpClient.newCall(request);

            mCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    setResponse("Failed to fetch data: " + e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        final JSONObject jsonObject = new JSONObject(response.body().string());
                        System.out.println(jsonObject.toString(3));
                        mdisplayName = jsonObject.getString("display_name");
                        JSONArray images = jsonObject.getJSONArray("images");
                        mdisplayPic = images.getJSONObject(0).getString("url");
                        System.out.println(mdisplayPic);
                        setResponse(mdisplayName);
                    } catch (JSONException e) {
                        setResponse("Failed to parse data: " + e);
                    }
                }
            });
        }
    }

    private void setPicture(final String pic){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nv_nav_drawer);
        View headerView = navigationView.getHeaderView(0);
        ImageView displayPic = headerView.findViewById(R.id.display_pic);
        Glide.with(displayPic.getContext()).load(pic).into(displayPic);
    }

    private void setResponse(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NavigationView navigationView = (NavigationView) findViewById(R.id.nv_nav_drawer);
                View headerView = navigationView.getHeaderView(0);
                TextView navUsername = (TextView) headerView.findViewById(R.id.display_name);
                navUsername.setText(text);
                setPicture(mdisplayPic);
                invalidateOptionsMenu();
            }
        });
    }


    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private String getRedirectUri() {
        return "http://com.example.spotifyauthentication/callback";
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        System.out.println("INSIDE ONPREPAREOPTIONS");
        final MenuItem logoItem = menu.findItem(R.id.login);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nv_nav_drawer);
        if(mLogout == 0){
            System.out.println("INSIDE HERE!");
            Menu nav_Menu = navigationView.getMenu();
            if (mdisplayPic != null) {
                Glide.with(this).asBitmap().load(mdisplayPic).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        logoItem.setIcon(new BitmapDrawable(getResources(), resource));
                    }
                });
            }
        }
        else{
            System.out.println("INSIDE HERE2!");
            logoItem.setIcon(R.drawable.ic_action_name);
            View headerView = navigationView.getHeaderView(0);
            ImageView displayPic = headerView.findViewById(R.id.display_pic);
            Menu nav_Menu = navigationView.getMenu();
            displayPic.setImageResource(R.drawable.user);
            TextView navUsername = (TextView) headerView.findViewById(R.id.display_name);
            navUsername.setText("");
            mdisplayName = null;
            mdisplayPic = null;
            mAccessToken = null;
        }
        if(mInit == 0){
            Menu nav_Menu = navigationView.getMenu();
            nav_Menu.findItem(R.id.nav_login).setVisible(true);
            nav_Menu.findItem(R.id.nav_logout).setVisible(false);
            mInit = 1;
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        mDrawerLayout.closeDrawers();
        switch (menuItem.getItemId()){
            case R.id.nav_generate:
                return true;
            case R.id.nav_login:
                onRequestTokenClicked();
                mLogout = 0;
                return true;
            case R.id.nav_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.nav_logout:
                onRequestTokenClicked();
                final AuthenticationRequest request = getAuthenticationLogoutRequest(AuthenticationResponse.Type.TOKEN);
                AuthenticationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request);
                mPlaylistItemsRV.setVisibility(View.INVISIBLE);
                ImageView spotifyImage = findViewById(R.id.spot_img);
                spotifyImage.setVisibility(View.VISIBLE);
                TextView randifyText = findViewById(R.id.randify_text);
                randifyText.setVisibility(View.VISIBLE);
                TextView playlistText = findViewById(R.id.playlist_text);
                playlistText.setBackgroundColor(Color.WHITE);
                playlistText.setVisibility(View.INVISIBLE);
                mLogout = 1;
                return true;
            case R.id.nav_help:
                Intent intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                return true;
            case R.id.nav_spotify:
                Intent spotifyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("spotify:"));
                if(spotifyIntent.resolveActivity(getPackageManager()) != null){
                    startActivity(spotifyIntent);
                }
                else{
                    AuthenticationClient.openDownloadSpotifyActivity(this);
                }
                return true;
            default:
                return false;
        }
    }

    public void generatePlaylist(View view) {
        if (mAccessToken == null) {
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.drawer_layout), R.string.warning_need_token, Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
            snackbar.show();
            return;
        }

        PlaylistUtility playlist = new PlaylistUtility();
        String holdURI;
        holdURI = playlist.getPlaylistTracks(mAccessToken, mGenre, mNumSongs, mPopularity);
        playlistJson(holdURI);

    }

    public void playlistJson(String URI){
        mPlaylistItemsRV.setVisibility(View.VISIBLE);
        ImageView spotifyImage = findViewById(R.id.spot_img);
        spotifyImage.setVisibility(View.INVISIBLE);
        TextView randifyText = findViewById(R.id.randify_text);
        randifyText.setVisibility(View.INVISIBLE);
        final Request request = new Request.Builder()
                .url(URI)
                .addHeader("Authorization","Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);
        mLoadingIndicatorPB.setVisibility(View.VISIBLE);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Failed to fetch data: " + e);
                mLoadingErrorMessageTV.setVisibility(View.VISIBLE);
                mLoadingIndicatorPB.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mLoadingIndicatorPB.setVisibility(View.INVISIBLE);
                mLoadingErrorMessageTV.setVisibility(View.INVISIBLE);
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray playListInfo = jsonObject.getJSONObject("tracks").getJSONArray("items");
                    System.out.println(playListInfo.getJSONObject(0).toString(3));

                    for(int i = 0; i < playListInfo.length(); i++){
                        mArtists.add("by " + playListInfo.getJSONObject(i).getJSONArray("artists").getJSONObject(0).getString("name"));
                        mTracks.add(playListInfo.getJSONObject(i).getString("name"));
                        int milliseconds = Integer.parseInt(playListInfo.getJSONObject(i).getString("duration_ms"));
                        int seconds = (int) (milliseconds / 1000) % 60 ;
                        int minutes = (int) ((milliseconds / (1000*60)) % 60);
                        String convertSec = String.valueOf(seconds);
                        String convertMin = String.valueOf(minutes);
                        if (seconds < 10){
                            convertSec = "0" + seconds;
                        }
                        mDuration.add(convertMin+":"+convertSec);
                        mID.add(playListInfo.getJSONObject(i).getString("id"));
                        mImageURL.add(playListInfo.getJSONObject(i).getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url"));

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView playlistText = findViewById(R.id.playlist_text);
                            playlistText.setText(mPlaylistName);
                            playlistText.setVisibility(View.VISIBLE);
//                            playlistText.setBackgroundColor(Color.LTGRAY);
                            playlistText.setBackgroundColor(Color.rgb(34,32,32));
                            mPlaylistAdapter.updatePlaylistItems(mTracks, mDuration, mArtists, mImageURL);
                        }
                    });

                } catch (JSONException e) {
                    System.out.println("Failed to parse data: " + e);
                }
            }
        });
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mGenre = sharedPreferences.getString("key_genre", "rap");
        mNumSongs = sharedPreferences.getString("key_numsongs", "50");
        mPopularity = sharedPreferences.getString("key_popu", "high");
        mPlaylistName = sharedPreferences.getString("key_name", "Default");
        System.out.println(mGenre + mNumSongs + mPopularity);
        mArtists.removeAll(mArtists);
        mTracks.removeAll(mTracks);
        mDuration.removeAll(mDuration);
        mID.removeAll(mID);
        mImageURL.removeAll(mImageURL);
    }

    @Override
    public void onPlaylistItemClick(String song, String artist, String duration, String ImageUrl) {
//        System.out.println("Song: " + song + " By: " + artist + " Duration: " + duration + " ImageAsset Found: " + ImageUrl);

        Intent intent = new Intent(this,SongDetailActivity.class);
        intent.putExtra(PlaylistUtility.EXTRA_CURRENT_SONG,song);
        intent.putExtra(PlaylistUtility.EXTRA_CURRENT_ARTIST,artist);
        intent.putExtra(PlaylistUtility.EXTRA_CURRENT_DUR,duration);
        intent.putExtra(PlaylistUtility.EXTRA_CURRENT_IMAGE,ImageUrl);
        startActivity(intent);

    }



}
