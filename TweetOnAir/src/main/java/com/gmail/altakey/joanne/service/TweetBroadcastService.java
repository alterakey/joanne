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
import com.gmail.altakey.joanne.view.Radio;
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
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_QUIT = "ACTION_QUIT";
    public static final String ACTION_STATE_CHANGED = "ACTION_STATE_CHANGED";

    public static final String EXTRA_TOKEN = TwitterAuthService.EXTRA_TOKEN;
    public static final String TWITTER_URL = "https://twitter.com/";

    public static boolean sActive = false;
    private static Handler sHandler = new Handler();

    private TwitterStream mStream;
    private RadioProfile mProfile;

    private final IBinder mBinder = new Binder () {
        TweetBroadcastService getService() {
            return TweetBroadcastService.this;
        }
    };

    public static final int SERVICE_ID = 1;
    private final NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this);

    public static void requestQuit(final Context context) {
        final Intent stopIntent = new Intent(context, TweetBroadcastService.class);
        stopIntent.setAction(ACTION_QUIT);
        context.startService(stopIntent);
    }

    @Override
    public void onCreate() {
        final String title = getString(R.string.app_name);
        final String status = "ready";

        final Intent quitIntent = new Intent(this, MainActivity.class);
        quitIntent.setAction(MainActivity.ACTION_QUIT);

        final Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        viewIntent.setData(Uri.parse(TWITTER_URL));

        startForeground(SERVICE_ID, mNotificationBuilder
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker(String.format("%s: %s", title, status))
                .setContentTitle(title)
                .setContentIntent(PendingIntent.getActivity(this, 0, viewIntent, 0))
                .setContentInfo(status)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Quit", PendingIntent.getActivity(this, 0, quitIntent, 0))
                .build());

        sActive = true;
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_STATE_CHANGED));
    }

    @Override
    public void onDestroy() {
        sActive = false;
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        final String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            if (mStream == null) {
                final AccessToken accessToken = (AccessToken)intent.getSerializableExtra(EXTRA_TOKEN);
                final ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(getString(R.string.consumer_key));
                builder.setOAuthConsumerSecret(getString(R.string.consumer_secret));
                mStream = new TwitterStreamFactory(builder.build()).getInstance(accessToken);
                mProfile = new RadioProfile(getApplicationContext(), mStream);
                
                mStream.addListener(new StreamListener());
                mStream.user();

                present(mProfile.ready());
            }
        } else if (ACTION_QUIT.equals(action)) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected void onPreExecute() {
                    Toast.makeText(getApplicationContext(), getString(R.string.terminate_in_progress), Toast.LENGTH_SHORT).show();
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
                    mProfile = null;
                    mStream = null;
                    Toast.makeText(getApplicationContext(), getString(R.string.terminating), Toast.LENGTH_SHORT).show();
                    LocalBroadcastManager.getInstance(TweetBroadcastService.this).sendBroadcast(new Intent(ACTION_STATE_CHANGED));
                    stopSelf(startId);
                }
            }.execute();
        }
        return START_STICKY;
    }

    private void present(final Radio radio) {
        if (radio != null) {
            final PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            if (pm.isScreenOn()) {
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        new TweetDisplayBuilder(getApplicationContext(), radio).build().show();
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
    }

    private class StreamListener implements UserStreamListener {
        @Override
        public void onStatus(final Status status) {
            present(mProfile.status(status));
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            present(mProfile.deletion());
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
            present(mProfile.error());
        }

        @Override
        public void onDeletionNotice(long l, long l2) { }

        @Override
        public void onFriendList(long[] longs) { }

        @Override
        public void onFavorite(final User source, final User target, Status status) {
            present(mProfile.favorite(source, target));
        }

        @Override
        public void onUnfavorite(User user, User user2, Status status) { }

        @Override
        public void onFollow(final User source, final User target) {
            present(mProfile.follow(source, target));
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
            present(mProfile.listed(listOwner, addedMember));
        }

        @Override
        public void onUserListMemberDeletion(final User deletedMember, final User listOwner, UserList userList) {
            present(mProfile.unlisted(listOwner, deletedMember));
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
            present(mProfile.block(source, target));
        }

        @Override
        public void onUnblock(User user, User user2) { }
    }
}
