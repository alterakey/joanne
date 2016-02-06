package com.gmail.altakey.joanne;

import android.app.Application;

public class Joanne extends Application {
    private Attachable mConnectivityPolicyReceiver = new ConnectivityPolicy();

    @Override
    public void onCreate() {
        super.onCreate();
        mConnectivityPolicyReceiver.attachTo(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mConnectivityPolicyReceiver.detachFrom(this);
    }

}
