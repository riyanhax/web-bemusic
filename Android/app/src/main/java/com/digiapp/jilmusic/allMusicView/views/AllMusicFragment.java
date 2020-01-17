package com.digiapp.jilmusic.allMusicView.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.digiapp.jilmusic.components.EventType;
import com.digiapp.jilmusic.model.MvpRecyclerListAdapter;
import com.digiapp.jilmusic.offlineView.models.AbstractAdapter;
import com.digiapp.jilmusic.offlineView.models.AbstractFragment;
import com.digiapp.jilmusic.allMusicView.models.AllMusicAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AllMusicFragment extends AbstractFragment {

    @Override
    public MvpRecyclerListAdapter createAdapter() {
        return new AllMusicAdapter(getActivity());
    }

    @Override
    public void loadData() {
        getPresenter().loadData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        EventBus.getDefault().register(this);
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventType eventType) {
        if(eventType == EventType.UPDATE_ALL){
            loadData();
        }
    }
}
