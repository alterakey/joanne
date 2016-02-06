package com.gmail.altakey.joanne.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.gmail.altakey.joanne.Joanne;

import twitter4j.RateLimitStatus;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

public class TweetService extends IntentService {
    private static final String ACTION_TWEET = "ACTION_TWEET";
    public static final String ACTION_DONE = "ACTION_DONE";

    private static final String EXTRA_STATUS = "status";
    private static final String EXTRA_TOKEN = AuthService.EXTRA_TOKEN;

    public static final String EXTRA_SUCCESS = "success";
    public static final String EXTRA_MESSAGE = "message";

    private static final String TAG = TweetService.class.getSimpleName();
    private static final String NAME = "TweetService";

    public TweetService() {
        super(NAME);
    }

    public static Intent call(final String status, final AccessToken token) {
        final Intent i = new Intent(TweetService.ACTION_TWEET);
        i.setClass(Joanne.getInstance(), TweetService.class);
        i.putExtra(TweetService.EXTRA_STATUS, status);
        i.putExtra(TweetService.EXTRA_TOKEN, token);
        return i;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String status = intent.getStringExtra(EXTRA_STATUS);
        final AccessToken accessToken = (AccessToken)intent.getSerializableExtra(EXTRA_TOKEN);

        final Intent i = new Intent(ACTION_DONE);
        try {
            try {
                AuthService.twitterWithAccessToken(accessToken).updateStatus(status);
                i.putExtra(EXTRA_SUCCESS, true);
            } catch (TwitterException e) {
                Log.e(TAG, "got exception on tweet", e);

                final RateLimitStatus ratelimit = e.getRateLimitStatus();

                i.putExtra(EXTRA_SUCCESS, false);
                if (ratelimit != null) {
                    i.putExtra(EXTRA_MESSAGE, String.format("%s (%ds to reset)", e.getErrorMessage(), ratelimit.getSecondsUntilReset()));
                } else {
                    if (e.isErrorMessageAvailable()) {
                        i.putExtra(EXTRA_MESSAGE, e.getErrorMessage());
                    } else {
                        i.putExtra(EXTRA_MESSAGE, "radio failure, please try again");
                    }
                }
            }
        } finally {
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        }
    }
}
