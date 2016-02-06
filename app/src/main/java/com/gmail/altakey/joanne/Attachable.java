package com.gmail.altakey.joanne;

import android.content.Context;

public interface Attachable {
    void attachTo(Context c);
    void detachFrom(Context c);
}
