package com.gmail.altakey.joanne.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.gmail.altakey.joanne.hack.ToastAnimationCanceler;

import twitter4j.Status;
import twitter4j.TwitterStream;
import twitter4j.User;

public class TweetDisplayBuilder {
    private TwitterStream mStream;
    private Context mContext;

    public TweetDisplayBuilder(final Context context, final TwitterStream stream) {
        mStream = stream;
        mContext = context;
    }

    public Toast status(final Status status) {
        return profile(new RadioProfile(mContext, mStream).status(status));
    }

    public Toast deletion() {
        return profile(new RadioProfile(mContext, mStream).deletion());
    }

    public Toast favorite(final User source, final User target) {
        return profile(new RadioProfile(mContext, mStream).favorite(source, target));
    }

    public Toast follow(final User source, final User target) {
        return profile(new RadioProfile(mContext, mStream).follow(source, target));
    }

    public Toast block(final User source, final User target) {
        return profile(new RadioProfile(mContext, mStream).block(source, target));
    }

    public Toast listed(final User source, final User target) {
        return profile(new RadioProfile(mContext, mStream).listed(source, target));
    }

    public Toast unlisted(final User source, final User target) {
        return profile(new RadioProfile(mContext, mStream).unlisted(source, target));
    }

    public Toast ready() {
        return profile(new RadioProfile(mContext, mStream).ready());
    }

    public Toast error() {
        return profile(new RadioProfile(mContext, mStream).error());
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