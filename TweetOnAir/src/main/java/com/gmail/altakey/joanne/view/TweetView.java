package com.gmail.altakey.joanne.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.util.UserRelation;

import java.lang.ref.WeakReference;

import twitter4j.Status;
import twitter4j.TwitterStream;
import twitter4j.User;

public class TweetView extends LinearLayout {
    private final static int COLOR_BUDDY = 0xff00ff00;
    private final static int COLOR_FOE = 0xffff0000;
    private final static int COLOR_FRIEND = 0xff8888ff;
    private final static int COLOR_NEUTRAL = 0xffff8800;

    private final static String FAVORITE_SCREENNAME = "Tracer 2";
    private final static String FAVORITE_TEXT = "favったか";
    private final static int FAVORITE_COLOR = COLOR_BUDDY;

    private final static String RETWEET_SCREENNAME = "Tracer 2";
    private final static String RETWEET_TEXT = "注意！拡散されているぞ";
    private final static int RETWEET_COLOR = COLOR_BUDDY;

    private final static String FOLLOW_SCREENNAME = "Tracer 2";
    private final static String FOLLOW_TEXT = "注意！敵にロックされている";
    private final static int FOLLOW_COLOR = COLOR_BUDDY;

    private final static String LISTED_SCREENNAME = "Tracer 2";
    private final static String LISTED_TEXT = "注意！レーダー照射を受けている";
    private final static int LISTED_COLOR = COLOR_BUDDY;

    private final static String UNLISTED_SCREENNAME = "AWACS";
    private final static String UNLISTED_TEXT = "トレーサー1 レーダーを回避";
    private final static int UNLISTED_COLOR = COLOR_FRIEND;

    private final static String BLOCKING_SCREENNAME = "Tracer 2";
    private final static String BLOCKING_TEXT = "撃墜確認！いいぞ";
    private final static int BLOCKING_COLOR = COLOR_BUDDY;

    private final static String FOLLOWING_SCREENNAME = "AWACS";
    private final static String FOLLOWING_TEXT = "レーダーロック";
    private final static int FOLLOWING_COLOR = COLOR_FRIEND;

    private final static String DELETE_SCREENNAME = "AWACS";
    private final static String DELETE_TEXT = "消滅 消滅";
    private final static int DELETE_COLOR = COLOR_FRIEND;

    private final static String RETWEETING_SCREENNAME = "AWACS";
    private final static String RETWEETING_TEXT = "拡散 拡散";
    private final static int RETWEETING_COLOR = COLOR_FRIEND;

    private final static int MENTION_COLOR = COLOR_BUDDY;
    private final static int SCREENNAME_SIZE = 14;
    private final static int TEXT_SIZE = 16;

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

        mScreenName.setTextSize(TypedValue.COMPLEX_UNIT_SP, SCREENNAME_SIZE);
        mText.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
    }

    public void setStatus(final Status status, final TwitterStream stream) {
        Status target = status;
        final UserRelation relation = new UserRelation(stream);

        if (status.isRetweet()) {
            target = status.getRetweetedStatus();
            if (relation.isMe(target.getUser())) {
                setRetweet();
                return;
            } else if (relation.isMe(status.getUser())) {
                setRetweeting();
                return;
            }
        } else {
            final String myScreenName = relation.getMyScreenName(mContextRef.get());
            if (myScreenName != null) {
                if (status.getText().contains(String.format("@%s", myScreenName))) {
                    mText.setTextColor(MENTION_COLOR);
                }
            }
        }

        mScreenName.setText(target.getUser().getScreenName());
        mText.setText(formatText(target.getText()));

        mScreenName.setTextColor(getScreenNameColor(target.getUser(), stream));
    }

    public void setFavorite(final User source, final User target, final TwitterStream stream) {
        if (new UserRelation(stream).isMe(target)) {
            mScreenName.setText(FAVORITE_SCREENNAME);
            mText.setText(formatText(FAVORITE_TEXT));
            mScreenName.setTextColor(FAVORITE_COLOR);
        }
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

    public void setFollow(final User source, final User target, final TwitterStream stream) {
        final UserRelation relation = new UserRelation(stream);

        if (relation.isMe(source)) {
            mScreenName.setText(FOLLOWING_SCREENNAME);
            mText.setText(formatText(FOLLOWING_TEXT));
            mScreenName.setTextColor(FOLLOWING_COLOR);
        } else if (relation.isMe(target)) {
            mScreenName.setText(FOLLOW_SCREENNAME);
            mText.setText(formatText(FOLLOW_TEXT));
            mScreenName.setTextColor(FOLLOW_COLOR);
        }
    }

    public void setBlock(final User source, final User target, final TwitterStream stream) {
        if (new UserRelation(stream).isMe(source)) {
            mScreenName.setText(BLOCKING_SCREENNAME);
            mText.setText(formatText(BLOCKING_TEXT));
            mScreenName.setTextColor(BLOCKING_COLOR);
        }
    }

    public void setListed(final User source, final User target, final TwitterStream stream) {
        if (new UserRelation(stream).isMe(target)) {
            mScreenName.setText(LISTED_SCREENNAME);
            mText.setText(formatText(LISTED_TEXT));
            mScreenName.setTextColor(LISTED_COLOR);
        }
    }

    public void setUnlisted(final User source, final User target, final TwitterStream stream) {
        if (new UserRelation(stream).isMe(target)) {
            mScreenName.setText(UNLISTED_SCREENNAME);
            mText.setText(formatText(UNLISTED_TEXT));
            mScreenName.setTextColor(UNLISTED_COLOR);
        }
    }

    private static String formatText(final String text) {
        return String.format("<< %s >>", text);
    }

    private int getScreenNameColor(final User user, final TwitterStream stream) {
        final UserRelation relation = new UserRelation(stream);

        if (relation.isMe(user)) {
            return COLOR_BUDDY;
        } else if (relation.isFriend(mContextRef.get(), user)) {
            return COLOR_FRIEND;
        } else {
            return COLOR_NEUTRAL;
        }
    }

}
