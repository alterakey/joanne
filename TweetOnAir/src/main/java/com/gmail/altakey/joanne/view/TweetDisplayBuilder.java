package com.gmail.altakey.joanne.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.gmail.altakey.joanne.hack.ToastAnimationCanceler;

public class TweetDisplayBuilder {
    private Context mContext;
    private Radio mRadio;

    public TweetDisplayBuilder(final Context context, final Radio radio) {
        mContext = context;
        mRadio = radio;
    }

    public Toast build() {
        @SuppressLint("ShowToast")
        final Toast message = Toast.makeText(mContext, "", Toast.LENGTH_LONG);
        message.setView(new TweetView(mContext).radio(mRadio));
        message.setGravity(Gravity.TOP, 0, 0);
        message.setMargin(0.0f, 0.0f);
        message.setDuration(mRadio.getDuration());
        new ToastAnimationCanceler(message).apply();
        return message;
    }
}