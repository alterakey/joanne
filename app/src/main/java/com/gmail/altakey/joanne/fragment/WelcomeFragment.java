package com.gmail.altakey.joanne.fragment;

import android.app.Activity;
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

import com.gmail.altakey.joanne.Attachable;
import com.gmail.altakey.joanne.Maybe;
import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.service.TweetBroadcastService;
import com.gmail.altakey.joanne.service.TwitterAuthService;

import butterknife.Bind;
import butterknife.ButterKnife;
import twitter4j.auth.AccessToken;

public class WelcomeFragment extends Fragment {
    @Bind(R.id.auth)
    TextView mProceed;

    private Attachable mServiceCallback = new ServiceCallback();

    public static WelcomeFragment call() {
        final WelcomeFragment f = new WelcomeFragment();
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_welcome, container, false);
        ButterKnife.bind(this, v);

        updateTitle();
        mProceed.setOnClickListener(view -> {
            try {
                final Context c = Maybe.of(getActivity()).get();
                showProcessingDialog();
                if (TweetBroadcastService.sActive) {
                    c.startService(TweetBroadcastService.quit());
                } else {
                    c.startService(TwitterAuthService.call());
                }
            } catch (Maybe.Nothing ignore) {
            }
        });
        return v;
    }

    private void updateTitle() {
        if (TweetBroadcastService.sActive) {
            mProceed.setText(getString(R.string.stop));
        } else {
            mProceed.setText(getString(R.string.start));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mServiceCallback.attachTo(getActivity());

        updateTitle();

        if (!TweetBroadcastService.sActive) {
            hideProcessingDialog();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mServiceCallback.detachFrom(getActivity());
    }

    private void showProcessingDialog() {
        try {
            ProcessingDialog.call(Maybe.of(getFragmentManager()).get());
        } catch (Maybe.Nothing ignore) {
        }
    }

    private void hideProcessingDialog() {
        try {
            Maybe.of(ProcessingDialog.on(Maybe.of(getFragmentManager()).get())).get().dismissAllowingStateLoss();
        } catch (Maybe.Nothing ignore) {
        }
    }

    private class ServiceCallback extends BroadcastReceiver implements Attachable {
        @Override
        public void attachTo(Context c) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(TwitterAuthService.ACTION_AUTH_SUCCESS);
            filter.addAction(TwitterAuthService.ACTION_AUTH_FAIL);
            filter.addAction(TweetBroadcastService.ACTION_STATE_CHANGED);
            LocalBroadcastManager.getInstance(c).registerReceiver(this, filter);
        }

        @Override
        public void detachFrom(Context c) {
            LocalBroadcastManager.getInstance(c).unregisterReceiver(this);
        }

        @Override
        public void onReceive(final Context c, final Intent intent) {
            switch (intent.getAction()) {
                case TwitterAuthService.ACTION_AUTH_SUCCESS:
                    final AccessToken token = (AccessToken)intent.getSerializableExtra(TwitterAuthService.EXTRA_TOKEN);
                    c.startService(TweetBroadcastService.call(token));
                    break;
                case TwitterAuthService.ACTION_AUTH_FAIL:
                    hideProcessingDialog();
                    Toast.makeText(c, c.getString(R.string.auth_failure), Toast.LENGTH_LONG).show();
                    break;
                case TweetBroadcastService.ACTION_STATE_CHANGED:
                    hideProcessingDialog();
                    updateTitle();
                    try {
                        Maybe.of(getActivity()).get().finish();
                    } catch (Maybe.Nothing ignore) {
                    }
                    break;
            }
        }
    }

}
