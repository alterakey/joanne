package com.gmail.altakey.joanne.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.service.TwitterAuthService;

public class MainActivity extends ActionBarActivity {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(final FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(final int position) {
            return WelcomeFragment.newInstance(1);
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public CharSequence getPageTitle(final int position) {
            return "Welcome";
        }
    }

    public static class WelcomeFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static WelcomeFragment newInstance(final int sectionNumber) {
            WelcomeFragment fragment = new WelcomeFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
            final View v = inflater.inflate(R.layout.fragment_welcome, container, false);
            final View proceed = v.findViewById(R.id.auth);
            proceed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    final Context c = getActivity();
                    if (c != null) {
                        final Intent intent = new Intent(c, TwitterAuthService.class);
                        intent.setAction(TwitterAuthService.ACTION_AUTH);
                        c.startService(intent);
                    }
                }
            });
            return v;
        }
    }
}
