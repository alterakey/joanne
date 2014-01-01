package com.gmail.altakey.joanne.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.hack.ToastAnimationCanceler;
import com.gmail.altakey.joanne.view.TweetView;

import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class TweetBroadcastService extends Service {
    public static final String ACTION_STATE_CHANGING = "ACTION_STATE_CHANGING";
    public static final String ACTION_STATE_CHANGED = "ACTION_STATE_CHANGED";

    public static final String EXTRA_TOKEN = TwitterAuthService.EXTRA_TOKEN;

    public static boolean sActive = false;
    private static Handler sHandler = new Handler();

    private TwitterStream mStream;

    private final IBinder mBinder = new Binder () {
        TweetBroadcastService getService() {
            return TweetBroadcastService.this;
        }
    };


    @Override
    public void onCreate() {
        sActive = true;
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_STATE_CHANGED));
    }

    @Override
    public void onDestroy() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                LocalBroadcastManager.getInstance(TweetBroadcastService.this).sendBroadcast(new Intent(ACTION_STATE_CHANGING));
            }

            @Override
            protected Void doInBackground(Void... voids) {
                if (mStream != null) {
                    mStream.shutdown();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mStream = null;
                sActive = false;
                LocalBroadcastManager.getInstance(TweetBroadcastService.this).sendBroadcast(new Intent(ACTION_STATE_CHANGED));
            }
        }.execute();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (mStream == null) {
            final AccessToken accessToken = (AccessToken)intent.getSerializableExtra(EXTRA_TOKEN);
            final ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(getString(R.string.consumer_key));
            builder.setOAuthConsumerSecret(getString(R.string.consumer_secret));
            mStream = new TwitterStreamFactory(builder.build()).getInstance(accessToken);
            mStream.addListener(new StreamListener());
            mStream.user();
        }
        return START_STICKY;
    }

    private class StreamListener implements UserStreamListener {
        private Toast buildDisplay(final Context context, final TweetView content) {
            final Toast message = Toast.makeText(context, "", Toast.LENGTH_LONG);
            message.setView(content);
            message.setGravity(Gravity.TOP, 0, 0);
            message.setMargin(0.0f, 0.0f);
            new ToastAnimationCanceler(message).apply();
            return message;
        }

        private boolean shouldDisplay() {
            final PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            return pm.isScreenOn();
        }

        @Override
        public void onStatus(final Status status) {
            if (shouldDisplay()) {
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final Context context = getApplicationContext();
                        final TweetView content = new TweetView(context);
                        content.setStatus(status, mStream);
                        buildDisplay(context, content).show();
                    }
                });
            }
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            if (shouldDisplay()) {
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final Context context = getApplicationContext();
                        final TweetView content = new TweetView(context);
                        content.setDeletion();
                        buildDisplay(context, content).show();
                    }
                });
            }
        }

        @Override
        public void onTrackLimitationNotice(int i) { }

        @Override
        public void onScrubGeo(long l, long l2) { }

        @Override
        public void onStallWarning(StallWarning stallWarning) { }

        @Override
        public void onException(Exception e) {
            Log.w("SL", "got exception while tracing up stream", e);
        }

        @Override
        public void onDeletionNotice(long l, long l2) { }

        @Override
        public void onFriendList(long[] longs) { }

        @Override
        public void onFavorite(final User source, final User target, Status status) {
            if (shouldDisplay()) {
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final Context context = getApplicationContext();
                        final TweetView content = new TweetView(context);
                        content.setFavorite(source, target, mStream);
                        buildDisplay(context, content).show();
                    }
                });
            }
        }

        @Override
        public void onUnfavorite(User user, User user2, Status status) { }

        @Override
        public void onFollow(final User source, final User target) {
            if (shouldDisplay()) {
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final Context context = getApplicationContext();
                        final TweetView content = new TweetView(context);
                        content.setFollow(source, target, mStream);
                        buildDisplay(context, content).show();
                    }
                });
            }
        }

        @Override
        public void onDirectMessage(DirectMessage directMessage) { }

        @Override
        public void onUserListMemberAddition(User user, User user2, UserList userList) { }

        @Override
        public void onUserListMemberDeletion(User user, User user2, UserList userList) { }

        @Override
        public void onUserListSubscription(User user, User user2, UserList userList) { }

        @Override
        public void onUserListUnsubscription(User user, User user2, UserList userList) { }

        @Override
        public void onUserListCreation(User user, UserList userList) { }

        @Override
        public void onUserListUpdate(User user, UserList userList) { }

        @Override
        public void onUserListDeletion(User user, UserList userList) { }

        @Override
        public void onUserProfileUpdate(User user) { }

        @Override
        public void onBlock(final User source, final User target) {
            if (shouldDisplay()) {
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final Context context = getApplicationContext();
                        final TweetView content = new TweetView(context);
                        content.setBlock(source, target, mStream);
                        buildDisplay(context, content).show();
                    }
                });
            }
        }

        @Override
        public void onUnblock(User user, User user2) { }
    }
}
