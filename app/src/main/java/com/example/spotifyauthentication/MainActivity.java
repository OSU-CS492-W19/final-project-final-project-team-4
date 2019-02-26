package com.example.spotifyauthentication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String CLIENT_ID = "4b4b430bd9d743fa9a00bfb99caa671c";
    public static final int AUTH_TOKEN_REQUEST_CODE = 0x10;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken;
    private Call mCall;
    private TextView mDisplayName;
    private String mdisplayPic;
    private String mdisplayName;
    private DrawerLayout mDrawerLayout;
    private int mLogout = 0;
    private int mInit = 0;
    private ImageView mDisplayPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nv_nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_nav_menu);
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
                mDrawerLayout.openDrawer(Gravity.START);
                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_spot:
                Intent spotifyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("spotify:"));
                startActivity(spotifyIntent);
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
        final MenuItem logoItem = menu.findItem(R.id.login);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nv_nav_drawer);
        if(mLogout == 0){
            Menu nav_Menu = navigationView.getMenu();
            nav_Menu.findItem(R.id.nav_login).setVisible(false);
            nav_Menu.findItem(R.id.nav_logout).setVisible(true);
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
            logoItem.setIcon(R.drawable.ic_action_name);
            View headerView = navigationView.getHeaderView(0);
            ImageView displayPic = headerView.findViewById(R.id.display_pic);
            Menu nav_Menu = navigationView.getMenu();
            nav_Menu.findItem(R.id.nav_login).setVisible(true);
            nav_Menu.findItem(R.id.nav_logout).setVisible(false);
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
                mLogout = 1;
                return true;
            case R.id.nav_spotify:
                Intent spotifyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("spotify:"));
                startActivity(spotifyIntent);
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
    }
}
