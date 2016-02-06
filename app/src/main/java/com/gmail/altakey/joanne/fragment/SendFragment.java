package com.gmail.altakey.joanne.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.altakey.joanne.Attachable;
import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.Maybe;
import com.gmail.altakey.joanne.service.TweetService;
import com.gmail.altakey.joanne.service.TwitterAuthService;

import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import twitter4j.auth.AccessToken;

/**
 * Created by mean on 2015/12/03.
 */
public class SendFragment extends Fragment {
    private static final String TAG = SendFragment.class.getSimpleName();
    private static final String KEY_SUBJECT = "text";
    private static final String KEY_URI = "uri";

    @Bind(R.id.transmit)
    Button mTransmit;

    @Bind(R.id.text)
    EditText mText;

    @Bind(R.id.chars)
    TextView mCharsRemaining;

    private final Attachable mLauncher = new Launcher();


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

        mText.addTextChangedListener(new TransmittionPolicy());
        mTransmit.setOnClickListener(view -> {
            final Context c = getActivity();
            if (c != null) {
                showProcessingDialog();
                c.startService(TwitterAuthService.call());
            }
        });

        mText.setText(formatted(getArguments()));
        return v;
    }

    private String formatted(final Bundle args) {
        try {
            return String.format(" / %s %s", Maybe.of(args.getString(KEY_SUBJECT)).get(), args.getString(KEY_URI));
        } catch (Maybe.Nothing e) {
            return String.format(" / %s", args.getString(KEY_URI));
        }
    }

    // XXX crude detection
    private int estimatedPostLength(final String body) {
        return Pattern.compile("https?://[^ ]+").matcher(body).replaceAll("https://t.co/XXXXXXXXXX").length();
    }

    private class TransmittionPolicy implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            final int remaining = Math.max(0, 140-estimatedPostLength(mText.getText().toString()));
            mCharsRemaining.setText(String.valueOf(remaining));
            mTransmit.setEnabled(remaining > 0);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private class Launcher extends BroadcastReceiver implements Attachable {
        @Override
        public void attachTo(Context c) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(TwitterAuthService.ACTION_AUTH_SUCCESS);
            filter.addAction(TwitterAuthService.ACTION_AUTH_FAIL);
            filter.addAction(TweetService.ACTION_DONE);
            LocalBroadcastManager.getInstance(c).registerReceiver(this, filter);
        }

        @Override
        public void detachFrom(Context c) {
            LocalBroadcastManager.getInstance(c).unregisterReceiver(this);
        }

        @Override
        public void onReceive(final Context c, final Intent intent) {
            final String action = intent.getAction();
            if (TwitterAuthService.ACTION_AUTH_SUCCESS.equals(action)) {
                final String status = mText.getText().toString();
                final AccessToken token = (AccessToken)intent.getSerializableExtra(TwitterAuthService.EXTRA_TOKEN);
                c.startService(TweetService.call(status, token));
            } else if (TwitterAuthService.ACTION_AUTH_FAIL.equals(action)) {
                hideProcessingDialog();
                Toast.makeText(c, c.getString(R.string.auth_failure), Toast.LENGTH_LONG).show();
            } else if (TweetService.ACTION_DONE.equals(action)) {
                hideProcessingDialog();
                final Activity activity = getActivity();
                if (activity != null) {
                    if (intent.getBooleanExtra(TweetService.EXTRA_SUCCESS, true)) {
                        activity.finish();
                    } else {
                        Toast.makeText(activity, intent.getStringExtra(TweetService.EXTRA_MESSAGE), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mLauncher.attachTo(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        mLauncher.detachFrom(getActivity());
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

}
