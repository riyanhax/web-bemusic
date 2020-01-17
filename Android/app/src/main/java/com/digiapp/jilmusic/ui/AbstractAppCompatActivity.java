package com.digiapp.jilmusic.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.digiapp.jilmusic.R;

public class AbstractAppCompatActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    protected void showProgDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this, R.style.DialogStyle);
        }

        if (!progressDialog.isShowing()) {
            progressDialog = new ProgressDialog(this, R.style.DialogStyle);
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    protected void dismissProgDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
        } catch (Exception e) {
        }
    }
}
