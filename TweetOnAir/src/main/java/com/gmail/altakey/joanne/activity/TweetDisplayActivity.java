package com.gmail.altakey.joanne.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.gmail.altakey.joanne.R;

public class TweetDisplayActivity extends Activity {
    public static final String ACTION_INCOMING = "ACTION_INCOMING";
    public static final String EXTRA_SCREEN_NAME = "screen_name";
    public static final String EXTRA_TEXT = "text";

    public static final int COLOR_FRIEND = 0xff00ff00;
    public static final int COLOR_FOE = 0xffff0000;
    public static final int COLOR_NEUTRAL = 0xff8888ff;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        final Intent intent = getIntent();
        if (intent != null) {
            final Window w = getWindow();
            w.setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);
            w.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            setContentView(R.layout.activity_tweet);

            final String screenName = intent.getStringExtra(EXTRA_SCREEN_NAME);
            final String text = intent.getStringExtra(EXTRA_TEXT);
            updateDisplay(screenName, text, COLOR_NEUTRAL);
        } else {
            finish();
        }
    }

    @Override
    public void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(0, 0);

        if (intent != null) {
            final String screenName = intent.getStringExtra(EXTRA_SCREEN_NAME);
            final String text = intent.getStringExtra(EXTRA_TEXT);
            updateDisplay(screenName, text, COLOR_NEUTRAL);
        }
    }

    private void updateDisplay(final String screenName, final String text, final int color) {
        final TextView screenNameView = (TextView)findViewById(R.id.screen_name);
        final TextView textView = (TextView)findViewById(R.id.text);
        Log.i("TDA", String.format("status: @%s: %s", screenName, text));
        screenNameView.setTextColor(color);
        screenNameView.setText(screenName);
        textView.setText(String.format("<< %s >>", text));
    }
}
