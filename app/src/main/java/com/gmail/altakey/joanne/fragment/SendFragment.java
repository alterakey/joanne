package com.gmail.altakey.joanne.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.hack.Maybe;
import com.gmail.altakey.joanne.service.TweetBroadcastService;
import com.gmail.altakey.joanne.service.TweetService;
import com.gmail.altakey.joanne.service.TwitterAuthService;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by mean on 2015/12/03.
 */
public class SendFragment extends Fragment {
    @Bind(R.id.transmit)
    Button mTransmit;

    @Bind(R.id.text)
    EditText mText;

    @Bind(R.id.chars)
    TextView mCharsRemaining;

    private static final String TAG = SendFragment.class.getSimpleName();
    private static final String KEY_SUBJECT = "text";
    private static final String KEY_URI = "uri";

    public static SendFragment call(final Intent intent) {
        final SendFragment f = new SendFragment();
        final Bundle args = new Bundle();
        f.setArguments(args);
        args.putString(KEY_SUBJECT, intent.getStringExtra(Intent.EXTRA_SUBJECT));
        args.putString(KEY_URI, intent.getStringExtra(Intent.EXTRA_TEXT));
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_send, container, false);
        ButterKnife.bind(this, v);

        mText.addTextChangedListener(new TextChangedListener());
        mTransmit.setOnClickListener(view -> {
            final Context c = getActivity();
            if (c != null) {
                final Intent intent = new Intent(c, TwitterAuthService.class);
                intent.setAction(TwitterAuthService.ACTION_AUTH);
                showProcessingDialog();
                c.startService(intent);
            }
        });

        mText.setText(formatted(getArguments()));
        return v;
    }

    private String formatted(final Bundle args) {
        try {
            return String.format(" / %s %s", Maybe.get(args.getString(KEY_SUBJECT)), args.getString(KEY_URI));
        } catch (Maybe.NoSuchValueException e) {
            return String.format(" / %s", args.getString(KEY_URI));
        }
    }

    private class TextChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            final int remaining = Math.max(0, 140-mText.getText().length());
            mCharsRemaining.setText(String.valueOf(remaining));
            mTransmit.setEnabled(remaining > 0);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context c, final Intent intent) {
            final String action = intent.getAction();
            if (TwitterAuthService.ACTION_AUTH_SUCCESS.equals(action)) {
                final Intent serviceLaunchIntent = new Intent(c, TweetService.class);
                serviceLaunchIntent.setAction(TweetService.ACTION_TWEET);
                serviceLaunchIntent.putExtra(TweetService.EXTRA_STATUS, mText.getText().toString());
                serviceLaunchIntent.putExtra(TweetService.EXTRA_TOKEN, intent.getSerializableExtra(TwitterAuthService.EXTRA_TOKEN));
                c.startService(serviceLaunchIntent);
            } else if (TwitterAuthService.ACTION_AUTH_FAIL.equals(action)) {
                hideProcessingDialog();
                Toast.makeText(c, c.getString(R.string.auth_failure), Toast.LENGTH_LONG).show();
            } else if (TweetService.ACTION_DONE.equals(action)) {
                hideProcessingDialog();
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.finish();
                }
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(TwitterAuthService.ACTION_AUTH_SUCCESS);
        filter.addAction(TwitterAuthService.ACTION_AUTH_FAIL);
        filter.addAction(TweetService.ACTION_DONE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);
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
