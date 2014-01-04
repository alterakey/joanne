package com.gmail.altakey.joanne.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.service.TweetBroadcastService;
import com.gmail.altakey.joanne.service.TwitterAuthService;
import com.gmail.altakey.joanne.view.RadioProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelListFragment extends ListFragment {
    private static final String DEFAULT_LIST = "[[\"__friends__\",\"friendly\"],[\"__other__\",\"neutral\"]]";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = super.onCreateView(inflater, container, savedInstanceState);
        final Context c = getActivity();
        if (c != null) {
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
            final List<Map<String, String>> data = new ArrayList<Map<String, String>>();
            try {
                final JSONArray list = (JSONArray)new JSONTokener(pref.getString("channels", DEFAULT_LIST)).nextValue();
                for (int i=0; i<list.length(); ++i) {
                    final JSONArray tuple = list.getJSONArray(i);
                    Map<String, String> e = new HashMap<String, String>();
                    final String key = tuple.getString(0);
                    final String color = tuple.getString(1);
                    final String description;
                    final String colorDescription;
                    if ("__friends__".equals(key)) {
                        description = "フォロー";
                    } else if ("__followers__".equals(key)) {
                        description = "フォロワー";
                    } else if ("__mutual_followers__".equals(key)) {
                        description = "相互フォロー";
                    } else if ("__other__".equals(key)) {
                        description = "その他";
                    } else {
                        description = "";
                    }

                    if ("friendly".equals(color)) {
                        colorDescription = "友軍";
                    } else if ("foe".equals(color)) {
                        colorDescription = "敵軍";
                    } else {
                        colorDescription = "中立";
                    }
                    e.put("color", colorDescription);
                    e.put("key", key);
                    e.put("description", description);
                    data.add(e);
                }
            } catch (JSONException e) {

            } catch (ClassCastException e) {

            }

            setListAdapter(new SimpleAdapter(c, data, android.R.layout.simple_list_item_2, new String[] {
                    "description", "color"
            }, new int[] {
                    android.R.id.text1, android.R.id.text2
            }));
        }
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_channel_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Toast.makeText(getActivity(), "TBD", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }
}
