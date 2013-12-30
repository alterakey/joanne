package com.gmail.altakey.joanne.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

public class TweetBroadcastService extends Service {
    private final IBinder mBinder = new Binder () {
        TweetBroadcastService getService() {
            return TweetBroadcastService.this;
        }
    };

    private final TweetWatcher mTask = new TweetWatcher();

    @Override
    public void onCreate() {
        mTask.execute();
    }

    @Override
    public void onDestroy() {
        mTask.cancel(true);
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private class TweetWatcher extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... args) {
            return null;
        }
    }
}
