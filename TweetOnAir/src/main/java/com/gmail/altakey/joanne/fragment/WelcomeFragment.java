package com.gmail.altakey.joanne.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.service.TweetBroadcastService;
import com.gmail.altakey.joanne.service.TwitterAuthService;

public class WelcomeFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context c, final Intent intent) {
            final String action = intent.getAction();
            if (TwitterAuthService.ACTION_AUTH_SUCCESS.equals(action)) {
                final Intent serviceLaunchIntent = new Intent(c, TweetBroadcastService.class);
                serviceLaunchIntent.putExtra(TweetBroadcastService.EXTRA_TOKEN, intent.getSerializableExtra(TwitterAuthService.EXTRA_TOKEN));
                c.startService(serviceLaunchIntent);
            } else if (TwitterAuthService.ACTION_AUTH_FAIL.equals(action)) {
                hideProcessingDialog();
                Toast.makeText(c, "Authentication failure", Toast.LENGTH_LONG).show();
            } else if (TweetBroadcastService.ACTION_STATE_CHANGED.equals(action)) {
                hideProcessingDialog();
                updateTitle(getView());
            }
        }
    };

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
        final TextView proceed = (TextView)v.findViewById(R.id.auth);
        updateTitle(v);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final Context c = getActivity();
                if (c != null) {
                    showProcessingDialog();
                    if (TweetBroadcastService.sActive) {
                        c.stopService(new Intent(c, TweetBroadcastService.class));
                    } else {
                        final Intent intent = new Intent(c, TwitterAuthService.class);
                        intent.setAction(TwitterAuthService.ACTION_AUTH);
                        c.startService(intent);
                    }
                }
            }
        });
        return v;
    }

    private void updateTitle(final View root) {
        final TextView proceed = (TextView)root.findViewById(R.id.auth);
        if (TweetBroadcastService.sActive) {
            proceed.setText("stop");
        } else {
            proceed.setText("start");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(TwitterAuthService.ACTION_AUTH_SUCCESS);
        filter.addAction(TwitterAuthService.ACTION_AUTH_FAIL);
        filter.addAction(TweetBroadcastService.ACTION_STATE_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);

        updateTitle(getView());

        if (!TweetBroadcastService.sActive) {
            hideProcessingDialog();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    private void showProcessingDialog() {
        final FragmentActivity activity = getActivity();
        if (activity != null) {
            final DialogFragment dialog = ProcessingDialog.newInstance();
            dialog.show(activity.getSupportFragmentManager(), ProcessingDialog.TAG);
        }
    }

    private void hideProcessingDialog() {
        final FragmentActivity activity = getActivity();
        if (activity != null) {
            final DialogFragment dialog = (DialogFragment)activity.getSupportFragmentManager().findFragmentByTag(ProcessingDialog.TAG);
            if (dialog != null) {
                dialog.dismissAllowingStateLoss();
            }
        }
    }
}
