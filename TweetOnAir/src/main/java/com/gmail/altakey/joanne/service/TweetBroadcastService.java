package com.gmail.altakey.joanne.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.activity.TweetDisplayActivity;

import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
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
        @Override
        public void onStatus(Status status) {
            final Intent intent = new Intent(TweetBroadcastService.this, TweetDisplayActivity.class);
            intent.setAction(TweetDisplayActivity.ACTION_INCOMING);
            intent.putExtra(TweetDisplayActivity.EXTRA_SCREEN_NAME, status.getUser().getScreenName());
            intent.putExtra(TweetDisplayActivity.EXTRA_TEXT, status.getText());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) { }

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
        public void onFavorite(User user, User user2, Status status) { }

        @Override
        public void onUnfavorite(User user, User user2, Status status) { }

        @Override
        public void onFollow(User user, User user2) { }

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
        public void onBlock(User user, User user2) { }

        @Override
        public void onUnblock(User user, User user2) { }
    }
}
