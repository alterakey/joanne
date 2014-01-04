package com.gmail.altakey.joanne.view;

import android.content.Context;
import android.widget.Toast;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;

import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.util.UserRelation;

import java.util.regex.Pattern;

import twitter4j.Status;
import twitter4j.TwitterStream;
import twitter4j.User;

public class RadioProfile {
    private final static int COLOR_BUDDY = 0xff00ff00;
    private final static int COLOR_FOE = 0xffff0000;
    private final static int COLOR_FRIEND = 0xff8888ff;
    private final static int COLOR_NEUTRAL = 0xffff8800;

    private final static int TEXT_COLOR = 0xffffffff;

    private final static String NULL_TEXT = "...";

    private final static String FAVORITE_SCREENNAME = "Tracer 2";
    private final static String FAVORITE_TEXT = "favったか";
    private final static int FAVORITE_COLOR = COLOR_BUDDY;

    private final static String RETWEET_SCREENNAME = "Tracer 2";
    private final static String RETWEET_TEXT = "注意 拡散されているぞ";
    private final static int RETWEET_COLOR = COLOR_BUDDY;

    private final static String FOLLOW_SCREENNAME = "Tracer 2";
    private final static String FOLLOW_TEXT = "注意 敵にロックされている";
    private final static int FOLLOW_COLOR = COLOR_BUDDY;

    private final static String LISTED_SCREENNAME = "Tracer 2";
    private final static String LISTED_TEXT = "注意 レーダー照射を受けている";
    private final static int LISTED_COLOR = COLOR_BUDDY;

    private final static String UNLISTED_SCREENNAME = "AWACS";
    private final static String UNLISTED_TEXT = "トレーサー1 レーダーを回避";
    private final static int UNLISTED_COLOR = COLOR_FRIEND;

    private final static String READY_SCREENNAME = "AWACS";
    private final static String READY_TEXT = "全機 聞こえるか";
    private final static int READY_COLOR = COLOR_FRIEND;

    private final static String ERROR_SCREENNAME = "Tracer 2";
    private final static String ERROR_TEXT = "無線不調 無線不調";
    private final static int ERROR_COLOR = COLOR_BUDDY;

    private final static String BLOCKING_SCREENNAME = "Tracer 2";
    private final static String BLOCKING_TEXT = "撃墜確認 いいぞ";
    private final static int BLOCKING_COLOR = COLOR_BUDDY;

    private final static String FOLLOWING_SCREENNAME = "AWACS";
    private final static String FOLLOWING_TEXT = "レーダーロック";
    private final static int FOLLOWING_COLOR = COLOR_FRIEND;

    private final static String DELETE_SCREENNAME = "AWACS";
    private final static String DELETE_TEXT = "消滅 消滅";
    private final static int DELETE_COLOR = COLOR_FRIEND;

    private final static String RETWEETING_SCREENNAME = "AWACS";
    private final static String RETWEETING_TEXT = "拡散 拡散";
    private final static int RETWEETING_COLOR = COLOR_FRIEND;

    private final static int MENTION_COLOR = COLOR_BUDDY;
    private final static int SCREENNAME_SIZE = 14;
    private final static int TEXT_SIZE = 16;

    private Context mContext;
    private UserRelation mRelation;

    public RadioProfile(final Context context, final TwitterStream stream) {
        mContext = context;
        mRelation = new UserRelation(context, stream);
    }

    private String getTeamName() {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return pref.getString("team_name", mContext.getString(R.string.pref_default_team_name));
    }

    private String getTeamScreenName() {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return pref.getString("team_name_english", mContext.getString(R.string.pref_default_team_name_english));
    }

    private String getScreenName() {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        final String sign = pref.getString("screen_name", "");
        if ("".equals(sign)) {
            return String.format("%s 1", getTeamScreenName());
        } else {
            return sign;
        }
    }

    private String getBuddyScreenName() {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        final String sign = pref.getString("screen_name_buddy", "");
        if ("".equals(sign)) {
            return String.format("%s 2", getTeamScreenName());
        } else {
            return sign;
        }
    }

    private String getCallsign() {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        final String sign = pref.getString("call_sign", "");
        if ("".equals(sign)) {
            return String.format("%s1", getTeamName());
        } else {
            return sign;
        }
    }

    private String getBuddyCallsign() {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        final String sign = pref.getString("call_sign_buddy", "");
        if ("".equals(sign)) {
            return String.format("%s2", getTeamName());
        } else {
            return sign;
        }
    }

    private String getAWACSScreenName() {
        return "AWACS";
    }

    private Radio getRadio() {
        final Radio r = new Radio();
        r.setDuration(Toast.LENGTH_LONG);
        r.setTextSize(TEXT_SIZE);
        r.setTextColor(TEXT_COLOR);
        r.setScreenNameSize(SCREENNAME_SIZE);
        return r;
    }

    private Radio filter(final Radio r) {
        if (r != null) {
            final String NULL_TOKEN = String.format(" %s ", NULL_TEXT);
            boolean needTidy = false;
            if (readBooleanPreference("suppress_informal_rt")) {
                r.filterText(Pattern.compile("(^|[\\s　]+)[RQ]T[\\s　]+.*$"), NULL_TOKEN);
                needTidy = true;
            }
            if (readBooleanPreference("suppress_url")) {
                r.filterText(Pattern.compile("(^|[\\s　]+)https?://[^ ]+?([\\s　]+|$)"), NULL_TOKEN);
                needTidy = true;
            }
            if (readBooleanPreference("suppress_lf")) {
                r.filterText(Pattern.compile("\\n"), "");
            }
            if (readBooleanPreference("suppress_mention")) {
                r.filterText(Pattern.compile("@[^ ]+?([\\s　]+|$)"), NULL_TOKEN);
                needTidy = true;
            }
            if (readBooleanPreference("suppress_tag")) {
                r.filterText(Pattern.compile("(^|[\\s　]+)#[^ ]+?([\\s　]+|$)"), NULL_TOKEN);
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

    public Radio status(final Status status) {
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
        r.setScreenName(target.getUser().getScreenName());
        r.setText(target.getText());
        r.setScreenNameColor(getScreenNameColorOf(target.getUser()));

        final String myScreenName = mRelation.getMyScreenName();
        if (myScreenName != null) {
            if (status.getText().contains(String.format("@%s", myScreenName))) {
                r.setTextColor(MENTION_COLOR);
            }
        }
        return filter(r);
    }

    public Radio favorite(final User source, final User target) {
        if (mRelation.isMe(target)) {
            final Radio r = getRadio();
            r.setScreenName(getBuddyScreenName());
            r.setText("favったか");
            r.setScreenNameColor(COLOR_BUDDY);
            return filter(r);
        } else {
            return null;
        }
    }

    public Radio retweet() {
        final Radio r = getRadio();
        r.setScreenName(getBuddyScreenName());
        r.setText("注意 拡散されているぞ");
        r.setScreenNameColor(COLOR_BUDDY);
        return filter(r);
    }

    public Radio retweeting() {
        final Radio r = getRadio();
        r.setScreenName(getAWACSScreenName());
        r.setText("拡散 拡散");
        r.setScreenNameColor(COLOR_FRIEND);
        return filter(r);
    }

    public Radio deletion() {
        final Radio r = getRadio();
        r.setScreenName(getAWACSScreenName());
        r.setText("消滅 消滅");
        r.setScreenNameColor(COLOR_FRIEND);
        return filter(r);
    }

    public Radio follow(final User source, final User target) {
        if (mRelation.isMe(source)) {
            final Radio r = getRadio();
            r.setScreenName(getAWACSScreenName());
            r.setText("レーダーロック");
            r.setScreenNameColor(COLOR_FRIEND);
            return filter(r);
        } else if (mRelation.isMe(target)) {
            final Radio r = getRadio();
            r.setScreenName(getBuddyScreenName());
            r.setText("注意 敵にロックされている");
            r.setScreenNameColor(COLOR_BUDDY);
            return filter(r);
        } else {
            return null;
        }
    }

    public Radio block(final User source, final User target) {
        if (mRelation.isMe(source)) {
            final Radio r = getRadio();
            r.setScreenName(getBuddyScreenName());
            r.setText("撃墜確認 いいぞ");
            r.setScreenNameColor(COLOR_BUDDY);
            return filter(r);
        } else {
            return null;
        }
    }

    public Radio listed(final User source, final User target) {
        if (mRelation.isMe(target)) {
            final Radio r = getRadio();
            r.setScreenName(getBuddyScreenName());
            r.setText("注意 レーダー照射を受けている");
            r.setScreenNameColor(COLOR_BUDDY);
            return filter(r);
        } else {
            return null;
        }
    }

    public Radio unlisted(final User source, final User target) {
        if (mRelation.isMe(target)) {
            final Radio r = getRadio();
            r.setScreenName(getAWACSScreenName());
            r.setText(String.format("%s レーダーを回避", getCallsign()));
            r.setScreenNameColor(COLOR_FRIEND);
            return filter(r);
        } else {
            return null;
        }
    }
    
    public Radio ready() {
        final Radio r = getRadio();
        r.setScreenName(getAWACSScreenName());
        r.setText("全機 聞こえるか");
        r.setScreenNameColor(COLOR_FRIEND);
        return filter(r);
    }

    public Radio error() {
        final Radio r = getRadio();
        r.setIsError(true);
        r.setScreenName(getBuddyScreenName());
        r.setText("無線不調 無線不調");
        r.setScreenNameColor(COLOR_BUDDY);
        return filter(r);
    }

    private int getScreenNameColorOf(final User user) {
        if (mRelation.isMe(user)) {
            return COLOR_BUDDY;
        } else if (mRelation.isFriend(user)) {
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
