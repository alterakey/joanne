package com.gmail.altakey.joanne.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.view.RadioProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SettingsChannelActivity extends PreferenceActivity {

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);

        final LinkedHashMap<String, String> tuples = new LinkedHashMap<String, String>();
        try {
            final JSONArray array = (JSONArray)new JSONTokener(getString(R.string.pref_channel_defaults)).nextValue();
            for (int i=0; i<array.length(); ++i) {
                final JSONArray tuple = array.getJSONArray(i);
                tuples.put(tuple.getString(0), tuple.getString(1));
            }
        } catch (final JSONException e) {
        }

        for (Preference p: getPreferences(tuples)) {
            screen.addPreference(p);
        }
        setPreferenceScreen(screen);
    }

    private List<Preference> getPreferences(final LinkedHashMap<String, String> tuples) {
        final List<Preference> prefs = new ArrayList<Preference>();

        final String[] channelLabels = getResources().getStringArray(R.array.pref_channel_labels);
        final String[] channelValues = getResources().getStringArray(R.array.pref_channel_values);
        final String[] colorLabels = getResources().getStringArray(R.array.pref_color_labels);
        final String[] colorValues = getResources().getStringArray(R.array.pref_color_values);

        final Map<String, String> channels = zip(
                channelValues, channelLabels
        );
        final Map<String, String> colors = zip(
                colorValues, colorLabels
        );
        final Map<String, Integer> actualColors = new HashMap<String, Integer>();
        for (String v: colorValues) {
            if ("friend".equals(v)) {
                actualColors.put(v, RadioProfile.COLOR_FRIEND);
            } else if ("neutral".equals(v)) {
                actualColors.put(v, RadioProfile.COLOR_NEUTRAL);
            } else if ("foe".equals(v)) {
                actualColors.put(v, RadioProfile.COLOR_FOE);
            }
        }

        for (Map.Entry<String, String> tuple: tuples.entrySet()) {
            final String channel = tuple.getKey();
            final String color = tuple.getValue();

            final ListPreference p = new ListPreference(this) {
                @Override

                @Override
                protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
                    super.onPrepareDialogBuilder(builder);
                    builder.setNegativeButton("remove", new )
                }
            }
            p.setKey(String.format("channel_%s", tuple.getKey()));
            p.setTitle(channels.containsKey(channel) ? channels.get(channel) : channel);
            p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ChannelEditDialogBuilder(SettingsChannelActivity.this).build(preference).show();
                    return false;
                }
            });

            bindPreferenceSummaryToValue(p);
            prefs.add(p);
        }

        final Preference add = new Preference(this);
        add.setKey("channel_add_more");
        add.setTitle("Add more...");
        add.setIcon(android.R.drawable.ic_menu_add);
        add.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new ChannelAddDialogBuilder(SettingsChannelActivity.this).build().show();
                return false;
            }
        });
        prefs.add(add);

        return prefs;
    }

    public static class ChannelAddDialogBuilder {
        private final Context mContext;

        public ChannelAddDialogBuilder(final Context c) {
            mContext = c;
        }

        public Dialog build() {
            final LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View v = inflater.inflate(R.layout.fragment_setting_channel_add, null, false);
            final Spinner channelType = (Spinner)v.findViewById(R.id.channel_type);
            final ArrayAdapter<CharSequence> channelTypeAdapter = ArrayAdapter.createFromResource(mContext, R.array.pref_channel_type_choices, android.R.layout.simple_spinner_item);
            channelTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            channelType.setAdapter(channelTypeAdapter);

            final Spinner color = (Spinner)v.findViewById(R.id.color);
            final ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(mContext, R.array.pref_color_choices, android.R.layout.simple_spinner_item);
            colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            color.setAdapter(colorAdapter);

            return new AlertDialog.Builder(mContext)
                    .setView(v)
                    .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .create();
        }
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference instanceof ListPreference) {
                final ListPreference p = (ListPreference)preference;

                try {
                    p.setSummary(Arrays.asList(p.getEntries())
                            .get(Arrays.asList(p.getEntryValues())
                                    .indexOf(value)));
                 } catch (IndexOutOfBoundsException e) {
                }
            } else {
                final String stringValue = value.toString();
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private static <K, V> Map<K, V> zip(final K[] a1, final V[] a2) {
        assert a1.length == a2.length; {
            final Map<K, V> ret = new HashMap<K, V>();
            for (int i=0; i<a1.length; ++i) {
                ret.put(a1[i], a2[i]);
            }
            return ret;
        }
    }
}
