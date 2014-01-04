package com.gmail.altakey.joanne.activity;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;

import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.fragment.ChannelListFragment;

public class SettingsChannelActivity extends PreferenceActivity {
    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
        final Preference p = new Preference(this);
        p.setKey("test_header");
        p.setTitle("TEST");
        p.setSummary("test content");
        p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Preference q = new Preference(SettingsChannelActivity.this);
                q.setKey("test2_header");
                q.setTitle("TEST2");
                q.setSummary("test2 content");
                q.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        screen.removePreference(q);
                        return true;
                    }
                });
                screen.addPreference(q);

                return true;
            }
        });

        screen.addPreference(p);
        setPreferenceScreen(screen);
    }
}
