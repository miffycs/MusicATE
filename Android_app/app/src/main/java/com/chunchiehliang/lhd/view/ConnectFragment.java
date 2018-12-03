package com.chunchiehliang.lhd.view;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.chunchiehliang.lhd.R;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;
import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.PlayerState;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.ChangeBounds;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import static com.chunchiehliang.lhd.util.PaletteUtil.getDominantColor;


public class ConnectFragment extends androidx.fragment.app.Fragment {

    private static final String TAG = ConnectFragment.class.getSimpleName();

    private static final String CLIENT_ID = "1677a1a90df448b1b226285a45dc2a48";
    private static final String REDIRECT_URI = "lhddemo://callback";
    private SpotifyAppRemote mSpotifyAppRemote;

    private Subscription<PlayerState> mPlayerStateSubscription;

    private final Subscription.EventCallback<PlayerState> mPlayerStateEventCallback = new Subscription.EventCallback<PlayerState>() {
        @Override
        public void onEvent(PlayerState playerState) {
            // Get image from track
            mSpotifyAppRemote.getImagesApi()
                    .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                    .setResultCallback(bitmap -> {
                        mImageViewCoverArt.setImageBitmap(bitmap);

                        int dominantColor = getDominantColor(bitmap);

                        Window window = getActivity().getWindow();
                        if (dominantColor != 0) {
                            mFragmentConnect.setBackgroundColor(dominantColor);
                            window.setStatusBarColor(dominantColor);
                            window.setNavigationBarColor(dominantColor);
                        }
                    });


            // play the track
            Log.d(TAG, "track: " + playerState.track.uri);
            mSpotifyAppRemote.getPlayerApi().play(playerState.track.uri);
        }
    };

    private ConstraintLayout mFragmentConnect;
    private CardView mCardCoverContainer;
    private Context mContext;
    private Button mBtnConnect;
    private ImageView mImageViewCoverArt;

    public ConnectFragment() {
    }

    public static ConnectFragment newInstance() {
        return new ConnectFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mBtnConnect.setText(R.string.reconnect);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connect, container, false);
        mFragmentConnect = view.findViewById(R.id.fragment_connect);
        mCardCoverContainer = view.findViewById(R.id.card_cover_container);
        mImageViewCoverArt = view.findViewById(R.id.image_cover_art);
        mBtnConnect = view.findViewById(R.id.btn_connect);
        mBtnConnect.setOnClickListener(v -> {
            onConnecting();
            connect(true);


        });
        onDisconnected();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        onDisconnected();
    }

    private void onConnecting() {
        mBtnConnect.setEnabled(false);
        mBtnConnect.setText(R.string.connecting);
    }

    private void onConnected() {
        mBtnConnect.setEnabled(false);
        mBtnConnect.setText(R.string.connected);


        if (mPlayerStateSubscription != null && !mPlayerStateSubscription.isCanceled()) {
            mPlayerStateSubscription.cancel();
            mPlayerStateSubscription = null;
        }

        mPlayerStateSubscription = (Subscription<PlayerState>) mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(mPlayerStateEventCallback)
                .setLifecycleCallback(new Subscription.LifecycleCallback() {
                    @Override
                    public void onStart() {
                        Log.d(TAG, "Event: start");
                    }

                    @Override
                    public void onStop() {
                        Log.d(TAG, "Event: end");
                    }
                })
                .setErrorCallback(new ErrorCallback() {
                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, throwable.getMessage());
                        onDisconnected();
                    }
                });

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mContext, R.layout.fragment_connected);

        Transition transition = new ChangeBounds();
        transition.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
        transition.setDuration(1000);
        TransitionManager.beginDelayedTransition(mFragmentConnect, transition);
        constraintSet.applyTo(mFragmentConnect);
    }

    private void onDisconnected() {
//        mCardCoverContainer.setVisibility(View.GONE);
        mBtnConnect.setEnabled(true);
        mBtnConnect.setText(R.string.connect);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mContext, R.layout.fragment_connect);

        Transition transition = new ChangeBounds();
        transition.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
        transition.setDuration(1000);
        TransitionManager.beginDelayedTransition(mFragmentConnect, transition);
        constraintSet.applyTo(mFragmentConnect);
        Log.e(TAG, "Disconnected");
    }


    private void connect(boolean showAuthView) {
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        SpotifyAppRemote.connect(mContext,
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(showAuthView)
                        .build(),
                new Connector.ConnectionListener() {
                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        ConnectFragment.this.onConnected();
                    }

                    @Override
                    public void onFailure(Throwable error) {
//                        logMessage(String.format("Connection failed: %s", error));
                        if (error instanceof CouldNotFindSpotifyApp) {
                            // Show button to download Spotify
                            new DownloadSpotifyDialogFragment().show(getActivity().getSupportFragmentManager(), "Download");
                        } else if (error instanceof NotLoggedInException || error instanceof UserNotAuthorizedException) {
                            Toast.makeText(mContext, "Please login Spotify and try again", Toast.LENGTH_SHORT).show();
                        }
                        ConnectFragment.this.onDisconnected();
                    }
                });
    }


}
