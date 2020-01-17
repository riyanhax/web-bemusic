package com.digiapp.jilmusic.favouritesView.views;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.digiapp.jilmusic.components.EventType;
import com.digiapp.jilmusic.model.MvpRecyclerListAdapter;
import com.digiapp.jilmusic.offlineView.models.AbstractAdapter;
import com.digiapp.jilmusic.offlineView.models.AbstractFragment;
import com.digiapp.jilmusic.favouritesView.models.FavouritesAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavouritesFragment extends AbstractFragment {

    @Override
    public MvpRecyclerListAdapter createAdapter() {
        return new FavouritesAdapter(getActivity());
    }

    @Override
    public void loadData() {
        if( getPresenter()!=null) {
            getPresenter().loadFavouritesData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        EventBus.getDefault().register(this);
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventType eventType) {
        if(eventType == EventType.UPDATE_FAVOURITES
                || eventType == EventType.UPDATE_ALL){
            loadData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
