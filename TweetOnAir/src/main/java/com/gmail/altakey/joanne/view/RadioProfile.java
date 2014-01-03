package com.gmail.altakey.joanne.view;

import android.content.Context;
import android.widget.Toast;

import com.gmail.altakey.joanne.util.UserRelation;

import twitter4j.Status;
import twitter4j.TwitterStream;
import twitter4j.User;

public class RadioProfile {
    private final static int COLOR_BUDDY = 0xff00ff00;
    private final static int COLOR_FOE = 0xffff0000;
    private final static int COLOR_FRIEND = 0xff8888ff;
    private final static int COLOR_NEUTRAL = 0xffff8800;

    private final static int TEXT_COLOR = 0xffffffff;

    private final static String FAVORITE_SCREENNAME = "Tracer 2";
    private final static String FAVORITE_TEXT = "favったか";
    private final static int FAVORITE_COLOR = COLOR_BUDDY;

    private final static String RETWEET_SCREENNAME = "Tracer 2";
    private final static String RETWEET_TEXT = "注意 拡散されているぞ";
    private final static int RETWEET_COLOR = COLOR_BUDDY;

    private final static String FOLLOW_SCREENNAME = "Tracer 2";
    private final static String FOLLOW_TEXT = "注意 敵にロックされている";
    private final static int FOLLOW_COLOR = COLOR_BUDDY;

    private final static String LISTED_SCREENNAME = "Tracer 2";
    private final static String LISTED_TEXT = "注意 レーダー照射を受けている";
    private final static int LISTED_COLOR = COLOR_BUDDY;

    private final static String UNLISTED_SCREENNAME = "AWACS";
    private final static String UNLISTED_TEXT = "トレーサー1 レーダーを回避";
    private final static int UNLISTED_COLOR = COLOR_FRIEND;

    private final static String READY_SCREENNAME = "AWACS";
    private final static String READY_TEXT = "全機 聞こえるか";
    private final static int READY_COLOR = COLOR_FRIEND;

    private final static String ERROR_SCREENNAME = "Tracer 2";
    private final static String ERROR_TEXT = "無線不調 無線不調";
    private final static int ERROR_COLOR = COLOR_BUDDY;

    private final static String BLOCKING_SCREENNAME = "Tracer 2";
    private final static String BLOCKING_TEXT = "撃墜確認 いいぞ";
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

    private String mScreenName;
    private int mScreenNameColor;
    private String mText;
    private UserRelation mRelation;
    private int mTextColor;

    public RadioProfile(final Context context, final TwitterStream stream) {
        mRelation = new UserRelation(context, stream);
    }

    public int getScreenNameSize() {
        return SCREENNAME_SIZE;
    }

    public int getTextSize() {
        return TEXT_SIZE;
    }

    public int getScreenNameColor() {
        return mScreenNameColor;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public String getText() {
        return String.format("<< %s >>", getRawText());
    }

    public String getRawText() {
        return mText;
    }

    public String getScreenName() {
        return mScreenName;
    }

    public int getDuration() {
        return Toast.LENGTH_LONG;
    }

    public RadioProfile status(final Status status) {
        Status target = status;
        
        if (status.isRetweet()) {
            target = status.getRetweetedStatus();
            if (mRelation.isMe(target.getUser())) {
                return retweet();
            } else if (mRelation.isMe(status.getUser())) {
                return retweeting();
            }
        } else {
            mScreenName = target.getUser().getScreenName();
            mText = target.getText();
            mScreenNameColor = getScreenNameColorOf(target.getUser());

            final String myScreenName = mRelation.getMyScreenName();
            if (myScreenName != null) {
                if (status.getText().contains(String.format("@%s", myScreenName))) {
                    mTextColor = MENTION_COLOR;
                } else {
                    mTextColor = TEXT_COLOR;
                }
            }
        }
        return this;
    }

    public RadioProfile favorite(final User source, final User target) {
        if (mRelation.isMe(target)) {
            mScreenName = FAVORITE_SCREENNAME;
            mText = FAVORITE_TEXT;
            mScreenNameColor = FAVORITE_COLOR;
        }
        return this;
    }

    public RadioProfile retweet() {
        mScreenName = RETWEET_SCREENNAME;
        mText = RETWEET_TEXT;
        mScreenNameColor = RETWEET_COLOR;
        mTextColor = TEXT_COLOR;
        return this;
    }

    public RadioProfile retweeting() {
        mScreenName = RETWEETING_SCREENNAME;
        mText = RETWEETING_TEXT;
        mScreenNameColor = RETWEETING_COLOR;
        mTextColor = TEXT_COLOR;
        return this;
    }

    public RadioProfile deletion() {
        mScreenName = DELETE_SCREENNAME;
        mText = DELETE_TEXT;
        mScreenNameColor = DELETE_COLOR;
        mTextColor = TEXT_COLOR;
        return this;
    }

    public RadioProfile follow(final User source, final User target) {
        if (mRelation.isMe(source)) {
            mScreenName = FOLLOWING_SCREENNAME;
            mText = FOLLOWING_TEXT;
            mScreenNameColor = FOLLOWING_COLOR;
            mTextColor = TEXT_COLOR;
        } else if (mRelation.isMe(target)) {
            mScreenName = FOLLOW_SCREENNAME;
            mText = FOLLOW_TEXT;
            mScreenNameColor = FOLLOW_COLOR;
            mTextColor = TEXT_COLOR;
        }
        return this;
    }

    public RadioProfile block(final User source, final User target) {
        if (mRelation.isMe(source)) {
            mScreenName = BLOCKING_SCREENNAME;
            mText = BLOCKING_TEXT;
            mScreenNameColor = BLOCKING_COLOR;
            mTextColor = TEXT_COLOR;
        }
        return this;
    }

    public RadioProfile listed(final User source, final User target) {
        if (mRelation.isMe(target)) {
            mScreenName = LISTED_SCREENNAME;
            mText = LISTED_TEXT;
            mScreenNameColor = LISTED_COLOR;
            mTextColor = TEXT_COLOR;
        }
        return this;
    }

    public RadioProfile unlisted(final User source, final User target) {
        if (mRelation.isMe(target)) {
            mScreenName = UNLISTED_SCREENNAME;
            mText = UNLISTED_TEXT;
            mScreenNameColor = UNLISTED_COLOR;
            mTextColor = TEXT_COLOR;
        }
        return this;
    }
    
    public RadioProfile ready() {
        mScreenName = READY_SCREENNAME;
        mText = READY_TEXT;
        mScreenNameColor = READY_COLOR;
        mTextColor = TEXT_COLOR;
        return this;
    }

    public RadioProfile error() {
        mScreenName = ERROR_SCREENNAME;
        mText = ERROR_TEXT;
        mScreenNameColor = ERROR_COLOR;
        mTextColor = TEXT_COLOR;
        return this;
    }

    private int getScreenNameColorOf(final User user) {
        if (mRelation.isMe(user)) {
            return COLOR_BUDDY;
        } else if (mRelation.isFriend(user)) {
            return COLOR_FRIEND;
        } else {
            return COLOR_NEUTRAL;
        }
    }
}
