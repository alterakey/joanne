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

    private UserRelation mRelation;

    public RadioProfile(final Context context, final TwitterStream stream) {
        mRelation = new UserRelation(context, stream);
    }

    private Radio getRadio() {
        final Radio r = new Radio();
        r.setDuration(Toast.LENGTH_LONG);
        r.setTextSize(TEXT_SIZE);
        r.setTextColor(TEXT_COLOR);
        r.setScreenNameSize(SCREENNAME_SIZE);
        return r;
    }

    public Radio status(final Status status) {
        Status target = status;
        
        if (status.isRetweet()) {
            target = status.getRetweetedStatus();
            if (mRelation.isMe(target.getUser())) {
                return retweet();
            } else if (mRelation.isMe(status.getUser())) {
                return retweeting();
            }
        }

        final Radio r = getRadio();
        r.setScreenName(target.getUser().getScreenName());
        r.setText(target.getText());
        r.setScreenNameColor(getScreenNameColorOf(target.getUser()));

        final String myScreenName = mRelation.getMyScreenName();
        if (myScreenName != null) {
            if (status.getText().contains(String.format("@%s", myScreenName))) {
                r.setTextColor(MENTION_COLOR);
            }
        }
        return r;
    }

    public Radio favorite(final User source, final User target) {
        if (mRelation.isMe(target)) {
            final Radio r = getRadio();
            r.setScreenName(FAVORITE_SCREENNAME);
            r.setText(FAVORITE_TEXT);
            r.setScreenNameColor(FAVORITE_COLOR);
            return r;
        } else {
            return null;
        }
    }

    public Radio retweet() {
        final Radio r = getRadio();
        r.setScreenName(RETWEET_SCREENNAME);
        r.setText(RETWEET_TEXT);
        r.setScreenNameColor(RETWEET_COLOR);
        return r;
    }

    public Radio retweeting() {
        final Radio r = getRadio();
        r.setScreenName(RETWEETING_SCREENNAME);
        r.setText(RETWEETING_TEXT);
        r.setScreenNameColor(RETWEETING_COLOR);
        return r;
    }

    public Radio deletion() {
        final Radio r = getRadio();
        r.setScreenName(DELETE_SCREENNAME);
        r.setText(DELETE_TEXT);
        r.setScreenNameColor(DELETE_COLOR);
        return r;
    }

    public Radio follow(final User source, final User target) {
        if (mRelation.isMe(source)) {
            final Radio r = getRadio();
            r.setScreenName(FOLLOWING_SCREENNAME);
            r.setText(FOLLOWING_TEXT);
            r.setScreenNameColor(FOLLOWING_COLOR);
            return r;
        } else if (mRelation.isMe(target)) {
            final Radio r = getRadio();
            r.setScreenName(FOLLOW_SCREENNAME);
            r.setText(FOLLOW_TEXT);
            r.setScreenNameColor(FOLLOW_COLOR);
            return r;
        } else {
            return null;
        }
    }

    public Radio block(final User source, final User target) {
        if (mRelation.isMe(source)) {
            final Radio r = getRadio();
            r.setScreenName(BLOCKING_SCREENNAME);
            r.setText(BLOCKING_TEXT);
            r.setScreenNameColor(BLOCKING_COLOR);
            return r;
        } else {
            return null;
        }
    }

    public Radio listed(final User source, final User target) {
        if (mRelation.isMe(target)) {
            final Radio r = getRadio();
            r.setScreenName(LISTED_SCREENNAME);
            r.setText(LISTED_TEXT);
            r.setScreenNameColor(LISTED_COLOR);
            return r;
        } else {
            return null;
        }
    }

    public Radio unlisted(final User source, final User target) {
        if (mRelation.isMe(target)) {
            final Radio r = getRadio();
            r.setScreenName(UNLISTED_SCREENNAME);
            r.setText(UNLISTED_TEXT);
            r.setScreenNameColor(UNLISTED_COLOR);
            return r;
        } else {
            return null;
        }
    }
    
    public Radio ready() {
        final Radio r = getRadio();
        r.setScreenName(READY_SCREENNAME);
        r.setText(READY_TEXT);
        r.setScreenNameColor(READY_COLOR);
        return r;
    }

    public Radio error() {
        final Radio r = getRadio();
        r.setIsError(true);
        r.setScreenName(ERROR_SCREENNAME);
        r.setText(ERROR_TEXT);
        r.setScreenNameColor(ERROR_COLOR);
        return r;
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
