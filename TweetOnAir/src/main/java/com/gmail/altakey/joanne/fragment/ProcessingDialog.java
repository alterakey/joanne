package com.gmail.altakey.joanne.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ProcessingDialog extends DialogFragment {
    public static final String TAG = "processing_dialog";

    public static ProcessingDialog newInstance() {
        return new ProcessingDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Please wait");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }
}
