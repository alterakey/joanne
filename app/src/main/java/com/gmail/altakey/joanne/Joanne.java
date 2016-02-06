package com.gmail.altakey.joanne;

import android.app.Application;

import java.lang.ref.WeakReference;

public class Joanne extends Application {
    private static Joanne sInstance;

    private Attachable mConnectivityPolicyReceiver = new ConnectivityPolicy();

    public static Joanne getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        mConnectivityPolicyReceiver.attachTo(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        sInstance = null;
        mConnectivityPolicyReceiver.detachFrom(this);
    }

}
