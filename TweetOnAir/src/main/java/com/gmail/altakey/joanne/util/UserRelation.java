package com.gmail.altakey.joanne.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gmail.altakey.joanne.service.TwitterAuthService;

import java.util.Set;

import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.User;
import twitter4j.auth.AccessToken;

public class UserRelation {
    private static final Object sLock = new Object();

    private final Context mContext;
    private final TwitterStream mStream;
    private static String sCachedMyScreenName;
    private static Set<Long> sCachedFriends;
    private static Set<Long> sCachedFollowers;

    public UserRelation(final TwitterStream target) {
        mContext = null;
        mStream = target;
    }

    public UserRelation(final Context context, final TwitterStream target) {
        mContext = context;
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

    public String getMyScreenName() {
        return getMyScreenName(null);
    }

    public String getMyScreenName(final Context context) {
        try {
            final AccessToken token = getToken();
            String screenName = token.getScreenName();
            if (screenName != null) {
                return screenName;
            } else if (sCachedMyScreenName == null) {
                final Context c = context != null ? context : mContext;
                if (c != null) {
                    final SharedPreferences pref = c.getSharedPreferences(TwitterAuthService.PREFERENCE, Context.MODE_PRIVATE);
                    sCachedMyScreenName = pref.getString("screen_name", null);
                }
            }
            return sCachedMyScreenName;
        } catch (TwitterException e) {
            Log.e("SL", "got exception while testing user identity", e);
            return null;
        }
    }

    public boolean isFriend(final User user) {
        return isFriend(null, user);
    }

    public boolean isFriend(final Context context, final User user) {
        synchronized (sLock) {
            if (sCachedFriends == null) {
                final Context c = context != null ? context : mContext;
                final SharedPreferences pref = c.getSharedPreferences(TwitterAuthService.PREFERENCE, Context.MODE_PRIVATE);
                sCachedFriends = new IdListCoder().decode(pref.getString("friends", ""));
            }
            return sCachedFriends.contains(user.getId());
        }
    }

    public boolean isFollower(final User user) {
        return isFollower(null, user);
    }

    public boolean isFollower(final Context context, final User user) {
        synchronized (sLock) {
            if (sCachedFollowers == null) {
                final Context c = context != null ? context : mContext;
                final SharedPreferences pref = c.getSharedPreferences(TwitterAuthService.PREFERENCE, Context.MODE_PRIVATE);
                sCachedFollowers = new IdListCoder().decode(pref.getString("followers", ""));
            }
            return sCachedFollowers.contains(user.getId());
        }
    }

    public boolean isMutualFollower(final User user) {
        return isMutualFollower(null, user);
    }

    public boolean isMutualFollower(final Context context, final User user) {
        return isFriend(context, user) && isFollower(context, user);
    }

    public static void notifyRelationsChanged() {
        synchronized (sLock) {
            sCachedFriends = null;
            sCachedFollowers = null;
        }
    }

    private AccessToken getToken() throws TwitterException {
        return mStream.getOAuthAccessToken();
    }

}