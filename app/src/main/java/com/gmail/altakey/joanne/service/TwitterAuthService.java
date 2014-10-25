/*
 * Copyright 2013 Takahiro Yoshimura
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gmail.altakey.joanne.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.util.IdListCoder;
import com.gmail.altakey.joanne.util.UserRelation;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
 
/* Please set your apps' callback URL to somewhere in your domain, like "http://(codename).apps.example.com/" */
public class TwitterAuthService extends IntentService {
    public static final String ACTION_AUTH = "AUTH";
    public static final String ACTION_AUTH_VERIFY = "AUTH_VERIFY";
    public static final String ACTION_AUTH_SUCCESS = "AUTH_SUCCESS";
    public static final String ACTION_AUTH_FAIL = "AUTH_FAIL";
 
    public static final String EXTRA_VERIFIER = "verifier";
    public static final String EXTRA_TOKEN = "token";

    public static final String PREFERENCE = "com.gmail.altakey.joanne";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_TOKEN_SECRET = "token_secret";
    public static final String KEY_SCREEN_NAME = "screen_name";
    public static final String KEY_FRIENDS = "friends";
    public static final String KEY_FOLLOWERS = "followers";

    public TwitterAuthService() {
        super("TwitterAuthService");
    }
 
    @Override
    public void onHandleIntent(Intent data) {
        final Twitter tw = TwitterFactory.getSingleton();
        try {
            tw.setOAuthConsumer(getString(R.string.consumer_key), getString(R.string.consumer_secret));
        } catch (IllegalStateException e) {
        }
 
        final String action = data.getAction();
 
        if (ACTION_AUTH.equals(action)) {
            authenticate(tw);
        } else if (ACTION_AUTH_VERIFY.equals(action)) {
            final String verifier = data.getStringExtra(EXTRA_VERIFIER);
            authenticateDone(tw, verifier);
        }
        stopSelf();
    }
 
    private void authenticate(final Twitter tw) {
        final AccessToken accessToken = getAccessToken();
        if (accessToken == null) {
            try {
                final RequestToken req = tw.getOAuthRequestToken();
                final Intent intent = new Intent(this, AuthorizeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse(req.getAuthorizationURL()));
                startActivity(intent);
            } catch (TwitterException e) {
                Log.e("TAS", "authentication failure", e);
            }
        } else {
            Log.d("TAS", String.format("got access token: %s", accessToken.toString()));
            updateRelations(this, accessToken);

            final Intent intent = new Intent(ACTION_AUTH_SUCCESS);
            intent.putExtra(EXTRA_TOKEN, accessToken);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
 
    private void authenticateDone(final Twitter tw, final String verifier) {
        try {
            final AccessToken accessToken = tw.getOAuthAccessToken(verifier);
            Log.d("TAS", String.format("got access token: %s", accessToken.toString()));
            setAccessToken(accessToken);
            updateRelations(this, accessToken);

            final Intent intent = new Intent(ACTION_AUTH_SUCCESS);
            intent.putExtra(EXTRA_TOKEN, accessToken);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } catch (TwitterException e) {
            Log.e("TAS", "authentication failure", e);

            final Intent intent = new Intent(ACTION_AUTH_FAIL);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
 
    public AccessToken getAccessToken() {
        final SharedPreferences prefs = getSharedPreferences(PREFERENCE, MODE_PRIVATE);
        final String token = prefs.getString(KEY_TOKEN, null);
        final String tokenSecret = prefs.getString(KEY_TOKEN_SECRET, null);
        if (token != null && tokenSecret != null) {
            return new AccessToken(token, tokenSecret);
        } else {
            return null;
        }
    }
 
    public void setAccessToken(final AccessToken token) {
        final SharedPreferences prefs = getSharedPreferences(PREFERENCE, MODE_PRIVATE);
        prefs
            .edit()
            .putString(KEY_TOKEN, token.getToken())
            .putString(KEY_TOKEN_SECRET, token.getTokenSecret())
            .putString(KEY_SCREEN_NAME, token.getScreenName())
            .commit();
    }

    public static void updateRelations(final Context context, final AccessToken token) {
        final SharedPreferences prefs = context.getSharedPreferences(PREFERENCE, MODE_PRIVATE);
        try {
            final Twitter twitter = TwitterFactory.getSingleton();
            twitter.setOAuthAccessToken(token);

            final Set<Long> friends = new HashSet<Long>();
            for (IDs ids = twitter.getFriendsIDs(-1); ; ids = twitter.getFriendsIDs(ids.getNextCursor())) {
                for (Long id : ids.getIDs()) {
                    friends.add(id);
                }
                if (!ids.hasNext()) {
                    break;
                }
            }

            final Set<Long> followers = new HashSet<Long>();
            for (IDs ids = twitter.getFollowersIDs(-1); ; ids = twitter.getFollowersIDs(ids.getNextCursor())) {
                for (Long id : ids.getIDs()) {
                    followers.add(id);
                }
                if (!ids.hasNext()) {
                    break;
                }
            }

            prefs
                .edit()
                .putString(KEY_FRIENDS, new IdListCoder().encode(friends))
                .putString(KEY_FOLLOWERS, new IdListCoder().encode(followers))
                .commit();
            UserRelation.notifyRelationsChanged();
            Log.d("TAS", String.format("got %d friends", friends.size()));
            Log.d("TAS", String.format("got %d followers", followers.size()));
        } catch (TwitterException e) {
            Log.w("TAS", "cannot get follower list", e);
        }
    }

    public static class AuthorizeActivity extends Activity {
        @Override
        @SuppressLint("SetJavaScriptEnabled")
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTitle(getString(R.string.twitter_auth_title));
 
            final Intent intent = getIntent();
            final WebView view = new WebView(this);
            view.setVerticalScrollBarEnabled(true);
            view.setHorizontalScrollBarEnabled(false);
            view.getSettings().setJavaScriptEnabled(true);
            view.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
            view.getSettings().setSupportMultipleWindows(false);
            view.getSettings().setSaveFormData(false);
            view.getSettings().setSavePassword(false);
            view.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    final Pattern pat = Pattern.compile("\\?.*oauth_verifier=([a-zA-Z0-9]+)");
                    final Matcher m = pat.matcher(url);
                    Log.d("TAS", String.format("url: %s", url));
                    if (m.find()) {
                        final String verifier = m.group(1);
                        final Intent verifyIntent = new Intent(AuthorizeActivity.this, TwitterAuthService.class);
                        verifyIntent.setAction(TwitterAuthService.ACTION_AUTH_VERIFY);
                        verifyIntent.putExtra(TwitterAuthService.EXTRA_VERIFIER, verifier);
                        startService(verifyIntent);
                        finish();
                        return true;
                    }
                    view.loadUrl(url);
                    return true;
                }
            });
            view.loadUrl(intent.getData().toString());
            setContentView(view);
        }
    }
}
