package com.gmail.altakey.joanne.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gmail.altakey.joanne.R;

import twitter4j.Status;

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

    private final static int MENTION_COLOR = COLOR_FRIEND;

    private TextView mScreenName;
    private TextView mText;

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
    }

    public void setStatus(final Status status) {
        mScreenName.setText(status.getUser().getScreenName());
        mText.setText(formatText(status.getText()));

        mScreenName.setTextColor(COLOR_NEUTRAL);
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

}
