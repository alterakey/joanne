package com.gmail.altakey.joanne.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gmail.altakey.joanne.service.TwitterAuthService;

import java.util.HashSet;
import java.util.Set;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.User;
import twitter4j.auth.AccessToken;

public class UserRelation {
    private static final String TAG = "UR";
    private static final Object sLock = new Object();

    private final Context mContext;
    private final TwitterStream mStream;
    private static String sCachedMyScreenName;
    private static Set<Long> sCachedFriends;
    private static Set<Long> sCachedFollowers;

    private static final String KEY_FRIENDS = "friends";
    private static final String KEY_FOLLOWERS = "followers";

    public UserRelation(final Context context, final TwitterStream target) {
        mContext = context;
        mStream = target;
    }

    public boolean isMe(final User user) {
        if (user != null) {
            try {
                return user.getId() == getToken().getUserId();
            } catch (TwitterException e) {
                Log.e(TAG, "got exception while testing user identity", e);
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
                    final SharedPreferences pref = getSharedPreferences();
                    sCachedMyScreenName = pref.getString("screen_name", null);
                }
            }
            return sCachedMyScreenName;
        } catch (TwitterException e) {
            Log.e(TAG, "got exception while testing user identity", e);
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
                final SharedPreferences pref = getSharedPreferences();
                sCachedFriends = new IdListCoder().decode(pref.getString(KEY_FRIENDS, ""));
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
                final SharedPreferences pref = getSharedPreferences();
                sCachedFollowers = new IdListCoder().decode(pref.getString(KEY_FOLLOWERS, ""));
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

    public static void update(final Context c, final AccessToken token) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        try {
            final Twitter twitter = TwitterAuthService.twitterWithAccessToken(token);

            final Set<Long> friends = new HashSet<>();
            for (IDs ids = twitter.getFriendsIDs(-1); ; ids = twitter.getFriendsIDs(ids.getNextCursor())) {
                for (Long id : ids.getIDs()) {
                    friends.add(id);
                }
                if (!ids.hasNext()) {
                    break;
                }
            }

            final Set<Long> followers = new HashSet<>();
            for (IDs ids = twitter.getFollowersIDs(-1); ; ids = twitter.getFollowersIDs(ids.getNextCursor())) {
                for (Long id : ids.getIDs()) {
                    followers.add(id);
                }
                if (!ids.hasNext()) {
                    break;
                }
            }

            prefs
                    .edit()
                    .putString(KEY_FRIENDS, new IdListCoder().encode(friends))
                    .putString(KEY_FOLLOWERS, new IdListCoder().encode(followers))
                    .commit();

            UserRelation.notifyRelationsChanged();
            Log.d(TAG, String.format("got %d friends", friends.size()));
            Log.d(TAG, String.format("got %d followers", followers.size()));
        } catch (TwitterException e) {
            Log.w(TAG, "cannot get follower list", e);
        }
    }

    private AccessToken getToken() throws TwitterException {
        return mStream.getOAuthAccessToken();
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

}
