package com.chunchiehliang.lhd.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.chunchiehliang.lhd.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DownloadSpotifyDialogFragment extends DialogFragment {
    private Context mContext;

    public static DownloadSpotifyDialogFragment newInstance() {
        return new DownloadSpotifyDialogFragment();


    }

    @Override
    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.dialog_download_spotify)
                .setPositiveButton(R.string.dialog_download, (dialog, id) -> {
                    final String appPackageName = "com.spotify.music";
                    final String referrer = "adjust_campaign=PACKAGE_NAME&adjust_tracker=ndjczk&utm_source=adjust_preinstall";

                    try {
                        Uri uri = Uri.parse("market://details")
                                .buildUpon()
                                .appendQueryParameter("id", appPackageName)
                                .appendQueryParameter("referrer", referrer)
                                .build();
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    } catch (android.content.ActivityNotFoundException ignored) {
                        Uri uri = Uri.parse("https://play.google.com/store/apps/details")
                                .buildUpon()
                                .appendQueryParameter("id", appPackageName)
                                .appendQueryParameter("referrer", referrer)
                                .build();
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, (dialog, id) -> {

                });
        return builder.create();
    }
}
