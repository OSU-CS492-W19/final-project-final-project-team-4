package com.example.spotifyauthentication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

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

    public static final String CLIENT_ID = "bb38e536a86e4e18a8c8300a10397733";
    public static final int AUTH_TOKEN_REQUEST_CODE = 0x10;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken;
    private Call mCall;
    private TextView mDisplayName;
    private String mdisplayName;
    private DrawerLayout mDrawerLayout;
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

                        setResponse(mdisplayName);
                    } catch (JSONException e) {
                        setResponse("Failed to parse data: " + e);
                    }
                }
            });
        }
    }

    private void setResponse(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NavigationView navigationView = (NavigationView) findViewById(R.id.nv_nav_drawer);
                View headerView = navigationView.getHeaderView(0);
                TextView navUsername = (TextView) headerView.findViewById(R.id.display_name);
                ImageView displayPic = headerView.findViewById(R.id.display_pic);
                navUsername.setText("Welcome " + text + "!");
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
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        mDrawerLayout.closeDrawers();
        switch (menuItem.getItemId()){
            case R.id.nav_generate:
                return true;
            case R.id.nav_login:
                onRequestTokenClicked();
                return true;
            case R.id.nav_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.nav_spotify:
                Intent spotifyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("spotify:"));
                startActivity(spotifyIntent);
                return true;
            default:
                return false;
        }
    }
}
