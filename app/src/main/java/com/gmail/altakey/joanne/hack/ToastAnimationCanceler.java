package com.gmail.altakey.joanne.hack;

import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Field;

public class ToastAnimationCanceler {
    private final Toast mToast;

    public ToastAnimationCanceler(final Toast t) {
        mToast = t;
    }

    public void apply() {
        try {
            final Field tnField = mToast.getClass().getDeclaredField("mTN");
            tnField.setAccessible(true);
            final Object TN = tnField.get(mToast);
            final Field paramsField = TN.getClass().getDeclaredField("mParams");
            paramsField.setAccessible(true);
            final WindowManager.LayoutParams params = (WindowManager.LayoutParams)paramsField.get(TN);
            params.windowAnimations = android.R.style.Animation;
            params.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        } catch (NoSuchFieldException e) {
            Log.w("TAC", "cannot find TN", e);
        } catch (IllegalAccessException e) {
            Log.w("TAC", "cannot access TN", e);
        }
    }
}
