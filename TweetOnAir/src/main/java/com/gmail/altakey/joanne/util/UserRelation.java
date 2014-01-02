package com.gmail.altakey.joanne.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gmail.altakey.joanne.service.TwitterAuthService;

import java.util.HashSet;
import java.util.Set;

import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.User;
import twitter4j.auth.AccessToken;

public class UserRelation {
    private static final Object sLock = new Object();

    private final TwitterStream mStream;
    private static String sCachedMyScreenName;
    private static Set<Long> sCachedFriends;

    public UserRelation(final TwitterStream target) {
        mStream = target;
    }

    public boolean isMe(final User user) {
        if (user != null) {
            try {
                return user.getId() == getToken().getUserId();
            } catch (TwitterException e) {
                Log.e("SL", "got exception while testing user identity", e);
                return false;
            }
        } else {
            return false;
        }
    }

    public String getMyScreenName(final Context context) {
        try {
            final AccessToken token = getToken();
            String screenName = token.getScreenName();
            if (screenName != null) {
                return screenName;
            } else if (sCachedMyScreenName == null) {
                if (context != null) {
                    final SharedPreferences pref = context.getSharedPreferences(TwitterAuthService.PREFERENCE, Context.MODE_PRIVATE);
                    sCachedMyScreenName = pref.getString("screen_name", null);
                }
            }
            return sCachedMyScreenName;
        } catch (TwitterException e) {
            Log.e("SL", "got exception while testing user identity", e);
            return null;
        }
    }

    public boolean isFriend(final Context context, final User user) {
        synchronized (sLock) {
            if (sCachedFriends == null) {
                final SharedPreferences pref = context.getSharedPreferences(TwitterAuthService.PREFERENCE, Context.MODE_PRIVATE);
                sCachedFriends = new IdListCoder().decode(pref.getString("friends", ""));
            }
            return sCachedFriends.contains(user.getId());
        }
    }

    public static void notifyFriendsChanged() {
        synchronized (sLock) {
            sCachedFriends = null;
        }
    }

    private AccessToken getToken() throws TwitterException {
        return mStream.getOAuthAccessToken();
    }

}