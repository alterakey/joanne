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
        mText.setText(String.format("<< %s >>", status.getText()));

        mScreenName.setTextColor(COLOR_NEUTRAL);
    }
}
