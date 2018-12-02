package com.chunchiehliang.lhd;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import static com.chunchiehliang.lhd.util.PaletteUtil.getColorText;
import static com.chunchiehliang.lhd.util.PaletteUtil.getDominantColor;
import static com.chunchiehliang.lhd.util.PaletteUtil.getVibrantColor;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String CLIENT_ID = "1677a1a90df448b1b226285a45dc2a48";
    private static final String REDIRECT_URI = "lhddemo://callback";
    private SpotifyAppRemote mSpotifyAppRemote;

    private static final String ARTIST_NAME_KEY = "artist";
    private static final String ALBUM_NAME_KEY = "album";
    private static final String TRACK_NAME_KEY = "track";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    Subscription<PlayerState> mPlayerStateSubscription;

    //    private final ErrorCallback mErrorCallback = throwable -> logError(throwable, "Boom!");
    private CoordinatorLayout mCoordinator;
    private CardView mContainerInfo;
    private Button mBtnConnect, mSubscribeToPlayerStateButton;
    private ImageView mImageViewCoverArt;
    private TextView mTextViewTrackName, mTextViewAlbumName, mTextViewArtistName;

    private final Subscription.EventCallback<PlayerContext> mPlayerContextEventCallback = new Subscription.EventCallback<PlayerContext>() {
        @Override
        public void onEvent(PlayerContext playerContext) {
//            mPlayerContextButton.setText(String.format(Locale.US, "%s\n%s", playerContext.title, playerContext.subtitle));
//            mPlayerContextButton.setTag(playerContext);
        }
    };

    private final Subscription.EventCallback<PlayerState> mPlayerStateEventCallback = new Subscription.EventCallback<PlayerState>() {
        @Override
        public void onEvent(PlayerState playerState) {



            // Set track info
            mTextViewTrackName.setText(playerState.track.name);
            mTextViewAlbumName.setText(playerState.track.album.name);
            mTextViewArtistName.setText(playerState.track.artist.name);

            // Get image from track
            mSpotifyAppRemote.getImagesApi()
                    .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                    .setResultCallback(bitmap -> {
                        mImageViewCoverArt.setImageBitmap(bitmap);

                        int dominantColor = getDominantColor(bitmap);

                        Window window = getWindow();
                        if (dominantColor != 0) {
                            mCoordinator.setBackgroundColor(dominantColor);
                            window.setStatusBarColor(dominantColor);
                            window.setNavigationBarColor(dominantColor);
                        }
                        mTextViewTrackName.setTextColor(getColorText(bitmap));
                        mTextViewAlbumName.setTextColor(getColorText(bitmap));
                        mTextViewArtistName.setTextColor(getColorText(bitmap));
                        mContainerInfo.setCardBackgroundColor(getVibrantColor(bitmap));
                    });

            // play the track
            Log.d(TAG, "track: " + playerState.track.uri);
            mSpotifyAppRemote.getPlayerApi().play(playerState.track.uri);

            // save to firestore
            MainActivity.this.saveInfo();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCoordinator = findViewById(R.id.coordinator_main);
        mContainerInfo = findViewById(R.id.container_track_info);
        mBtnConnect = findViewById(R.id.btn_connect);
//        mSubscribeToPlayerStateButton = findViewById(R.id.btn_subscribe);
        mImageViewCoverArt = findViewById(R.id.image_cover_art);
        mTextViewTrackName = findViewById(R.id.tv_track_name);
        mTextViewAlbumName = findViewById(R.id.tv_album_name);
        mTextViewArtistName = findViewById(R.id.tv_artist_name);
        onDisconnected();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        onDisconnected();
    }

    private void connect(boolean showAuthView) {
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        SpotifyAppRemote.connect(
                getApplication(),
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(showAuthView)
                        .build(),
                new Connector.ConnectionListener() {
                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        MainActivity.this.onConnected();
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        logMessage(String.format("Connection failed: %s", error));
                        MainActivity.this.onDisconnected();
                    }
                });
    }


    private void saveInfo() {
        String artistName = mTextViewArtistName.getText().toString();
        String albumName = mTextViewAlbumName.getText().toString();
        String trackName = mTextViewTrackName.getText().toString();

        if (artistName.isEmpty() || albumName.isEmpty() || trackName.isEmpty()) {
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put(ARTIST_NAME_KEY, artistName);
        data.put(ALBUM_NAME_KEY, albumName);
        data.put(TRACK_NAME_KEY, trackName);

        db.collection("playlist")
                .add(data)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Document has been saved!");
                        } else {
                            Log.d(TAG, "Document was not saved!", task.getException());
                        }
                    }
                });
    }

    public void onConnectClicked(View v) {
        onConnecting();
        connect(true);
    }

    private void onConnected() {
        mBtnConnect.setEnabled(false);
        mBtnConnect.setText(R.string.connected);


        if (mPlayerStateSubscription != null && !mPlayerStateSubscription.isCanceled()) {
            mPlayerStateSubscription.cancel();
            mPlayerStateSubscription = null;
        }

//        mSubscribeToPlayerStateButton.setText(R.string.connected);
        mPlayerStateSubscription = (Subscription<PlayerState>) mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(mPlayerStateEventCallback)
                .setLifecycleCallback(new Subscription.LifecycleCallback() {
                    @Override
                    public void onStart() {
                        logMessage("Event: start");
                    }

                    @Override
                    public void onStop() {
                        logMessage("Event: end");
                    }
                })
                .setErrorCallback(throwable -> {
                    mSubscribeToPlayerStateButton.setText(R.string.connect);
                });
    }

    private void onConnecting() {
        mBtnConnect.setEnabled(false);
        mBtnConnect.setText(R.string.connecting);
    }

    private void onDisconnected() {
        mBtnConnect.setEnabled(true);
        mBtnConnect.setText(R.string.connect);
    }

    private void logMessage(String msg) {
        logMessage(msg, Toast.LENGTH_LONG);
    }

    private void logMessage(String msg, int duration) {
        Toast.makeText(this, msg, duration).show();
        Log.d(TAG, msg);
    }
}
