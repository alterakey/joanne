package com.gmail.altakey.joanne.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gmail.altakey.joanne.R;

public class TweetView extends LinearLayout {
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public TweetView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(final Context context) {
        inflate(context, R.layout.tweet, this);
        mScreenName = (TextView)findViewById(R.id.screen_name);
        mText = (TextView)findViewById(R.id.text);
    }

    public TweetView radio(final Radio radio) {
        mScreenName.setText(radio.getScreenName());
        mScreenName.setTextColor(radio.getScreenNameColor());
        mScreenName.setTextSize(TypedValue.COMPLEX_UNIT_SP, radio.getScreenNameSize());
        mText.setText(radio.getText());
        mText.setTextColor(radio.getTextColor());
        mText.setTextSize(TypedValue.COMPLEX_UNIT_SP, radio.getTextSize());
        return this;
    }

}
