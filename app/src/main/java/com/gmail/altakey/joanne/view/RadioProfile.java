package com.gmail.altakey.joanne.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.gmail.altakey.joanne.Maybe;
import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.util.UserRelation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

import twitter4j.Status;
import twitter4j.TwitterStream;
import twitter4j.User;

public class RadioProfile {
    private final static String COLOR_BUDDY = "buddy";
    private final static String COLOR_FOE = "foe";
    private final static String COLOR_FRIEND = "friend";
    private final static String COLOR_NEUTRAL = "neutral";
    private final static String TEXT_COLOR = "text";

    private final static String NULL_TEXT = "...";

    private Context mContext;
    private UserRelation mRelation;
    private JSONObject mProfile;

    public RadioProfile(final Context context, final TwitterStream stream) {
        mContext = context;
        mRelation = new UserRelation(context, stream);
        init();
    }

    public void hydrate(final JSONObject profile) {
        mProfile = profile;
    }

    private void init() {
        try {
            hydrate(new JSONObject(new Scanner(mContext.getResources().openRawResource(R.raw.profile_tracer)).useDelimiter("\\A").next()));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private int getColorOf(final String colorKey) {
        try {
            if (!TEXT_COLOR.equals(colorKey)) {
                return Long.valueOf(mProfile.getJSONObject("color").getString(colorKey), 16).intValue();
            } else {
                return Color.WHITE;
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String getActorDataOf(final String actorKey, final String key) {
        try {
            return mProfile.getJSONObject("actor").getJSONObject(actorKey).getString(key);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String getTeamDataOf(final String key) {
        try {
            return mProfile.getJSONObject("team").getString(key);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private Iterable<JSONObject> getTranscriptOf(final String key) {
        try {
            return iterateOver(mProfile.getJSONObject("transcript").getJSONArray(key));
        } catch (JSONException e) {
            try {
                final JSONArray random = mProfile.getJSONObject("transcript").getJSONObject(key).getJSONArray("random");
                return iterateOver(random.getJSONArray(new Random().nextInt(random.length())));
            } catch (JSONException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    private List<Radio> runTranscript(Iterable<JSONObject> transcript) {
        try {
            final List<Radio> o = new LinkedList<>();
            for (JSONObject t : Maybe.of(transcript).get()) {
                final Radio r = getRadio();
                final String k = t.keys().next();
                final String v = t.getString(k);

                r.setScreenName(t.optString("screen_name", getActorDataOf(k, "screen_name")));
                r.setText(v.replace("%(me_sign)s", getActorDataOf("me", "sign")).replace("%(me_screen_name)s", getActorDataOf("me", "screen_name")).replace("%(team_name)s", getTeamDataOf("name")));
                r.setScreenNameColor(getColorOf(getActorDataOf(k, "color")));
                o.add(filter(r));
            }
            return o;
        } catch (Maybe.Nothing e) {
            return new ArrayList<>();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static Iterable<JSONObject> iterateOver(final JSONArray target) {
        return () -> new Iterator<JSONObject>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < target.length();
            }

            @Override
            public JSONObject next() {
                try {
                    return target.getJSONObject(i++);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    private Radio getRadio() {
        final Radio r = new Radio();
        r.setDuration(Toast.LENGTH_LONG);
        r.setTextSize(16);
        r.setTextColor(getColorOf(TEXT_COLOR));
        r.setScreenNameSize(14);
        return r;
    }

    private Radio filter(final Radio r) {
        if (r != null) {
            final String NULL_TOKEN = String.format(" %s ", NULL_TEXT);
            boolean needTidy = false;
            if (readBooleanPreference("suppress_informal_rt")) {
                r.filterText(Pattern.compile("(^|(\\s|　)+)[RQ]T(\\s|　)+.*$"), NULL_TOKEN);
                needTidy = true;
            }
            if (readBooleanPreference("suppress_url")) {
                r.filterText(Pattern.compile("(^|(\\s|　)+)https?://[^ ]+?((\\s|　)+|$)"), NULL_TOKEN);
                needTidy = true;
            }
            if (readBooleanPreference("suppress_lf")) {
                r.filterText(Pattern.compile("\\n"), "");
            }
            if (readBooleanPreference("suppress_mention")) {
                r.filterText(Pattern.compile("@[^ ]+?((\\s|　)+|$)"), NULL_TOKEN);
                needTidy = true;
            }
            if (readBooleanPreference("suppress_tag")) {
                r.filterText(Pattern.compile("(^|(\\s|　)+)#[^ ]+?((\\s|　)+|$)"), NULL_TOKEN);
                needTidy = true;
            }
            if (needTidy) {
                r.tidyText(NULL_TEXT);
            }
            return r.isEmpty() ? nullify(r) : r;
        } else {
            return null;
        }
    }

    private Radio nullify(final Radio r) {
        r.setText(NULL_TEXT);
        return r;
    }

    public Iterable<Radio> status(final Status status) {
        final List<Radio> o = new LinkedList<>();
        Status target = status;
        
        if (status.isRetweet()) {
            target = status.getRetweetedStatus();
            if (mRelation.isMe(target.getUser())) {
                return retweet();
            } else if (mRelation.isMe(status.getUser())) {
                return retweeting();
            }
        }

        final Radio r = getRadio();
        String screenNameColor = getScreenNameColorOf(target.getUser());
        r.setScreenName(target.getUser().getScreenName());
        r.setText(target.getText());

        if (COLOR_NEUTRAL.equals(screenNameColor)) {
            if (!mRelation.isFriend(target.getUser()) && !mRelation.isFollower(target.getUser())
                && !mRelation.isFriend(status.getUser()) && !mRelation.isFollower(status.getUser())) {
                screenNameColor = COLOR_FOE;
            }
        }
        r.setScreenNameColor(getColorOf(screenNameColor));

        final String myScreenName = mRelation.getMyScreenName();
        if (myScreenName != null) {
            if (!status.isRetweet()) {
                if (status.getText().contains(String.format("@%s", myScreenName))) {
                    r.setTextColor(getColorOf(screenNameColor));
                }
            }
        }
        o.add(filter(r));
        return o;
    }

    public Iterable<Radio> favorite(final User source, final User target) {
        if (mRelation.isMe(target)) {
            return runTranscript(getTranscriptOf("favorite"));
        } else if (mRelation.isMe(source)) {
            return runTranscript(getTranscriptOf("favoriting"));
        } else {
            return runTranscript(null);
        }
    }

    public Iterable<Radio> retweet() {
        return runTranscript(getTranscriptOf("retweet"));
    }

    public Iterable<Radio> retweeting() {
        return runTranscript(getTranscriptOf("retweeting"));
    }

    public Iterable<Radio> deletion() {
        return runTranscript(getTranscriptOf("deletion"));
    }

    public Iterable<Radio> follow(final User source, final User target) {
        if (mRelation.isMe(source)) {
            return runTranscript(getTranscriptOf("following"));
        } else if (mRelation.isMe(target)) {
            return runTranscript(getTranscriptOf("follow"));
        } else {
            return runTranscript(null);
        }
    }

    public Iterable<Radio> block(final User source, final User target) {
        if (mRelation.isMe(source)) {
            return runTranscript(getTranscriptOf("blocking"));
        } else {
            return null;
        }
    }

    public Iterable<Radio> listed(final User source, final User target) {
        if (mRelation.isMe(target)) {
            return runTranscript(getTranscriptOf("listed"));
        } else {
            return null;
        }
    }

    public Iterable<Radio> unlisted(final User source, final User target) {
        if (mRelation.isMe(target)) {
            return runTranscript(getTranscriptOf("unlisted"));
        } else {
            return null;
        }
    }
    
    public Iterable<Radio> ready() {
        return runTranscript(getTranscriptOf("ready"));
    }

    public Iterable<Radio> error() {
        return runTranscript(getTranscriptOf("error"));
    }

    private String getScreenNameColorOf(final User user) {
        if (mRelation.isMe(user)) {
            return COLOR_BUDDY;
        } else if (mRelation.isMutualFollower(user)) {
            return COLOR_FRIEND;
        } else {
            return COLOR_NEUTRAL;
        }
    }

    private boolean readBooleanPreference(final String key) {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return pref.getBoolean(key, false);
    }
}
