package com.gmail.altakey.joanne.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.activity.MainActivity;
import com.gmail.altakey.joanne.view.Radio;
import com.gmail.altakey.joanne.view.RadioProfile;
import com.gmail.altakey.joanne.view.TweetDisplayBuilder;

import java.util.Date;

import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class TweetService extends IntentService {
    public static final String ACTION_TWEET = "ACTION_TWEET";
    public static final String ACTION_DONE = "ACTION_DONE";

    public static final String EXTRA_STATUS = "status";
    public static final String EXTRA_TOKEN = TwitterAuthService.EXTRA_TOKEN;

    private static final String TAG = TweetService.class.getSimpleName();
    private static final String NAME = "TweetService";

    public TweetService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String status = intent.getStringExtra(EXTRA_STATUS);
        final AccessToken accessToken = (AccessToken)intent.getSerializableExtra(EXTRA_TOKEN);
        final ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(getString(R.string.consumer_key));
        builder.setOAuthConsumerSecret(getString(R.string.consumer_secret));

        try {
            try {
                new TwitterFactory(builder.build()).getInstance(accessToken).updateStatus(status);
            } catch (TwitterException e) {
                Log.e(TAG, "got exception on tweet", e);
            }
        } finally {
            final Intent i = new Intent(ACTION_DONE);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        }
    }
}
