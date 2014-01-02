package com.gmail.altakey.joanne.view;

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
        final TweetView content = new TweetView(mContext);
        content.setStatus(status, mStream);
        return on(content);
    }

    public Toast deletion() {
        final TweetView content = new TweetView(mContext);
        content.setDeletion();
        return on(content);
    }

    public Toast favorite(final User source, final User target) {
        final TweetView content = new TweetView(mContext);
        content.setFavorite(source, target, mStream);
        return on(content);
    }

    public Toast follow(final User source, final User target) {
        final TweetView content = new TweetView(mContext);
        content.setFollow(source, target, mStream);
        return on(content);
    }

    public Toast block(final User source, final User target) {
        final TweetView content = new TweetView(mContext);
        content.setBlock(source, target, mStream);
        return on(content);
    }

    public Toast listed(final User source, final User target) {
        final TweetView content = new TweetView(mContext);
        content.setListed(source, target, mStream);
        return on(content);
    }

    public Toast unlisted(final User source, final User target) {
        final TweetView content = new TweetView(mContext);
        content.setUnlisted(source, target, mStream);
        return on(content);
    }

    public Toast ready() {
        final TweetView content = new TweetView(mContext);
        content.setReady();
        return on(content);
    }

    public Toast error() {
        final TweetView content = new TweetView(mContext);
        content.setError();
        return on(content);
    }

    public Toast on(final TweetView content) {
        final Toast message = Toast.makeText(mContext, "", Toast.LENGTH_LONG);
        message.setView(content);
        message.setGravity(Gravity.TOP, 0, 0);
        message.setMargin(0.0f, 0.0f);
        new ToastAnimationCanceler(message).apply();
        return message;
    }
}