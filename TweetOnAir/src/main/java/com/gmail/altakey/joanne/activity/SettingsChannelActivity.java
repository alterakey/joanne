package com.gmail.altakey.joanne.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;

import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.fragment.ChannelListFragment;

public class SettingsChannelActivity extends ActionBarActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_channel);
    }
}
