package com.gmail.altakey.joanne;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.gmail.altakey.joanne.Attachable;

public class ConnectivityPolicy extends BroadcastReceiver implements Attachable {
    public static final String ACTION_DISCONNECT = "disconnect";
    public static final String ACTION_CONNECT = "connect";

    @Override
    public void attachTo(final Context c) {
        final IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_SCREEN_OFF);
        f.addAction(Intent.ACTION_SCREEN_ON);
        c.registerReceiver(this, new IntentFilter(f));
    }

    @Override
    public void detachFrom(final Context c) {
        c.unregisterReceiver(this);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_SCREEN_OFF:
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_DISCONNECT));
                break;
            case Intent.ACTION_SCREEN_ON:
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_CONNECT));
                break;
        }
    }
}
