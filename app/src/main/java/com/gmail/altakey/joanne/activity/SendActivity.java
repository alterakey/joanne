package com.gmail.altakey.joanne.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gmail.altakey.joanne.R;
import com.gmail.altakey.joanne.fragment.SendFragment;

public class SendActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);

        final Intent intent = getIntent();

        if (intent != null) {
            getSupportFragmentManager().beginTransaction().add(R.id.plate, SendFragment.call(intent)).commit();
        } else {
            finish();
        }
    }
}
