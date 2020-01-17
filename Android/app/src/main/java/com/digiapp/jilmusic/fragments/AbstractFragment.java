package com.digiapp.jilmusic.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;

public class AbstractFragment extends Fragment {
    @SuppressLint("MissingSuperCall")
    @Override
    public void onPause() {
        //super.onPause();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onStop() {
       // super.onStop();
    }
}
