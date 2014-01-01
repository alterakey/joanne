package com.gmail.altakey.joanne.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.service.TwitterAuthService;

import java.lang.ref.WeakReference;

import twitter4j.Status;
import twitter4j.StatusStream;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.User;
import twitter4j.UserStream;
import twitter4j.auth.AccessToken;

public class TweetView extends LinearLayout {
    private final static int COLOR_FRIEND = 0xff00ff00;
    private final static int COLOR_FOE = 0xffff0000;
    private final static int COLOR_NEUTRAL = 0xff8888ff;

    private final static String FAVORITE_SCREENNAME = "Tracer 2";
    private final static String FAVORITE_TEXT = "favったか";
    private final static int FAVORITE_COLOR = COLOR_FRIEND;

    private final static String RETWEET_SCREENNAME = "Tracer 2";
    private final static String RETWEET_TEXT = "注意！拡散されているぞ";
    private final static int RETWEET_COLOR = COLOR_FRIEND;

    private final static String FOLLOW_SCREENNAME = "Tracer 2";
    private final static String FOLLOW_TEXT = "注意！敵にロックされている";
    private final static int FOLLOW_COLOR = COLOR_FRIEND;

    private final static String DELETE_SCREENNAME = "AWACS";
    private final static String DELETE_TEXT = "消滅 消滅";
    private final static int DELETE_COLOR = COLOR_NEUTRAL;

    private final static String RETWEETING_SCREENNAME = "AWACS";
    private final static String RETWEETING_TEXT = "拡散 拡散";
    private final static int RETWEETING_COLOR = COLOR_NEUTRAL;

    private final static int MENTION_COLOR = COLOR_FRIEND;

    private static String sCachedMyScreenName;

    private TextView mScreenName;
    private TextView mText;
    private WeakReference<Context> mContextRef;

    public TweetView(Context context) {
        super(context);
        init(context);
    }

    public TweetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TweetView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(final Context context) {
        inflate(context, R.layout.tweet, this);
        mScreenName = (TextView)findViewById(R.id.screen_name);
        mText = (TextView)findViewById(R.id.text);
        mContextRef = new WeakReference<Context>(context);
    }

    public void setStatus(final Status status, final TwitterStream stream) {
        Status target = status;

        if (status.isRetweet()) {
            target = status.getRetweetedStatus();
            if (isMe(target.getUser(), stream)) {
                setRetweet();
                return;
            } else if (isMe(status.getUser(), stream)) {
                setRetweeting();
                return;
            }
        } else {
            final String myScreenName = getMyScreenName(stream);
            if (myScreenName != null) {
                if (status.getText().contains(String.format("@%s", myScreenName))) {
                    mText.setTextColor(MENTION_COLOR);
                }
            }
        }

        mScreenName.setText(target.getUser().getScreenName());
        mText.setText(formatText(target.getText()));

        mScreenName.setTextColor(getScreenNameColor(target.getUser()));
    }

    public void setFavorite() {
        mScreenName.setText(FAVORITE_SCREENNAME);
        mText.setText(formatText(FAVORITE_TEXT));
        mScreenName.setTextColor(FAVORITE_COLOR);
    }

    public void setRetweet() {
        mScreenName.setText(RETWEET_SCREENNAME);
        mText.setText(formatText(RETWEET_TEXT));
        mScreenName.setTextColor(RETWEET_COLOR);
    }

    public void setRetweeting() {
        mScreenName.setText(RETWEETING_SCREENNAME);
        mText.setText(formatText(RETWEETING_TEXT));
        mScreenName.setTextColor(RETWEETING_COLOR);
    }

    public void setDeletion() {
        mScreenName.setText(DELETE_SCREENNAME);
        mText.setText(formatText(DELETE_TEXT));
        mScreenName.setTextColor(DELETE_COLOR);
    }

    public void setFollow() {
        mScreenName.setText(FOLLOW_SCREENNAME);
        mText.setText(formatText(FOLLOW_TEXT));
        mScreenName.setTextColor(FOLLOW_COLOR);
    }

    private static String formatText(final String text) {
        return String.format("<< %s >>", text);
    }

    private static boolean isMe(final User user, final TwitterStream stream) {
        if (user != null) {
            try {
                return user.getId() == stream.getOAuthAccessToken().getUserId();
            } catch (TwitterException e) {
                Log.e("SL", "got exception while testing user identity", e);
                return false;
            }
        } else {
            return false;
        }
    }

    private String getMyScreenName(final TwitterStream stream) {
        try {
            final AccessToken token = stream.getOAuthAccessToken();
            String screenName = token.getScreenName();
            if (screenName != null) {
                return screenName;
            } else if (sCachedMyScreenName == null) {
                final Context context = mContextRef.get();
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

    private static int getScreenNameColor(final User user) {
        return COLOR_NEUTRAL;
    }
}
