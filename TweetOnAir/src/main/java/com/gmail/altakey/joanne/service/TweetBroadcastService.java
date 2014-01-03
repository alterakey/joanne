package com.gmail.altakey.joanne.service;

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
import com.gmail.altakey.joanne.view.RadioProfile;
import com.gmail.altakey.joanne.view.TweetDisplayBuilder;

import java.util.Date;

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

    public static final int SERVICE_ID = 1;
    private final NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this);

    @Override
    public void onCreate() {
        final String title = getString(R.string.app_name);
        final String status = "ready";

        final Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startForeground(SERVICE_ID, mNotificationBuilder
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker(String.format("%s: %s", title, status))
                .setContentTitle(title)
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
                .setContentInfo(status)
                .build());

        sActive = true;
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_STATE_CHANGED));
    }

    @Override
    public void onDestroy() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                Toast.makeText(getApplicationContext(), "Quit in progress...", Toast.LENGTH_SHORT).show();
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
                stopForeground(true);

                mStream = null;
                sActive = false;
                LocalBroadcastManager.getInstance(TweetBroadcastService.this).sendBroadcast(new Intent(ACTION_STATE_CHANGED));
                Toast.makeText(getApplicationContext(), getString(R.string.terminating), Toast.LENGTH_SHORT).show();
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

            present(new RadioProfile(getApplicationContext(), mStream).ready());
        }
        return START_STICKY;
    }

    private void present(final RadioProfile radio) {
        final PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        if (pm.isScreenOn()) {
            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    new TweetDisplayBuilder(radio.getContext()).profile(radio).show();
                }
            });
        }

        final NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(SERVICE_ID, mNotificationBuilder
                .setContentText(radio.getRawText())
                .setContentInfo(radio.getScreenName())
                .setWhen(new Date().getTime())
                .build());
    }

    private class StreamListener implements UserStreamListener {
        @Override
        public void onStatus(final Status status) {
            present(new RadioProfile(getApplicationContext(), mStream).status(status));
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            present(new RadioProfile(getApplicationContext(), mStream).deletion());
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
            present(new RadioProfile(getApplicationContext(), mStream).error());
        }

        @Override
        public void onDeletionNotice(long l, long l2) { }

        @Override
        public void onFriendList(long[] longs) { }

        @Override
        public void onFavorite(final User source, final User target, Status status) {
            present(new RadioProfile(getApplicationContext(), mStream).favorite(source, target));
        }

        @Override
        public void onUnfavorite(User user, User user2, Status status) { }

        @Override
        public void onFollow(final User source, final User target) {
            present(new RadioProfile(getApplicationContext(), mStream).follow(source, target));
            try {
                TwitterAuthService.updateFriendsList(TweetBroadcastService.this, mStream.getOAuthAccessToken());
            } catch (TwitterException e) {
                Log.w("TBS", "cannot update friends list", e);
            }
        }

        @Override
        public void onDirectMessage(DirectMessage directMessage) { }

        @Override
        public void onUserListMemberAddition(final User addedMember, final User listOwner, UserList userList) {
            present(new RadioProfile(getApplicationContext(), mStream).listed(listOwner, addedMember));
        }

        @Override
        public void onUserListMemberDeletion(final User deletedMember, final User listOwner, UserList userList) {
            present(new RadioProfile(getApplicationContext(), mStream).unlisted(listOwner, deletedMember));
        }

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
            present(new RadioProfile(getApplicationContext(), mStream).block(source, target));
        }

        @Override
        public void onUnblock(User user, User user2) { }
    }
}
