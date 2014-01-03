package com.gmail.altakey.joanne.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.util.HashSet;
import java.util.Set;

public class IdListCoder {
    public String encode(Set<Long> list) {
        final JSONArray array = new JSONArray();
        for (Long id : list) {
            array.put(id);
        }
        return array.toString();
    }

    public Set<Long> decode(final String data) {
        final Set<Long> ret = new HashSet<Long>();
        try {
            final JSONArray friends = (JSONArray)new JSONTokener(data).nextValue();
            for (int i=0; i<friends.length(); ++i) {
                try {
                    ret.add(friends.getLong(i));
                } catch (JSONException e) {
                }
            }
        } catch (final JSONException e) {
        } catch (final ClassCastException e) {
        }
        return ret;
    }
}
