package com.gmail.altakey.joanne.hack;

import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Field;

public class Maybe {
    public static <T> T get(T o) throws NoSuchValueException {
        if (o != null) {
            return o;
        } else {
            throw new NoSuchValueException();
        }
    }

    public static class NoSuchValueException extends Exception {
    }
}
