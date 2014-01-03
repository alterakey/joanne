package com.gmail.altakey.joanne.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.gmail.altakey.joanne.hack.ToastAnimationCanceler;

import twitter4j.TwitterStream;

public class TweetDisplayBuilder {
    private Context mContext;

    public TweetDisplayBuilder(final Context context) {
        mContext = context;
    }

    public Toast profile(final RadioProfile profile) {
        return on(profile, new TweetView(mContext).radio(profile));
    }

    @SuppressLint("ShowToast")
    public Toast on(final RadioProfile profile, final TweetView content) {
        final Toast message = Toast.makeText(mContext, "", Toast.LENGTH_LONG);
        message.setView(content);
        message.setGravity(Gravity.TOP, 0, 0);
        message.setMargin(0.0f, 0.0f);
        message.setDuration(profile.getDuration());
        new ToastAnimationCanceler(message).apply();
        return message;
    }
}