package com.digiapp.jilmusic.selectionsView.models;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.digiapp.jilmusic.R;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.offlineView.models.AbstractAdapter;
import com.digiapp.jilmusic.offlineView.presenters.MusicTrackViewPresenter;
import com.digiapp.jilmusic.offlineView.views.MusicTrackViewHolder;
import com.digiapp.jilmusic.selectionsView.presenters.SelectionsPresenter;
import com.digiapp.jilmusic.selectionsView.views.SelectionViewHolder;

public class SelectionsAdapter extends AbstractAdapter {

    protected Activity mActivity;

    public SelectionsAdapter(Activity activity) {
        mActivity = activity;
    }

    @Override
    public SelectionsPresenter getPresenter() {
        return new SelectionsPresenter();
    }

    @Override
    public Context getContext() {
        return mActivity;
    }

    @Override
    public MusicTrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MusicTrackViewHolder viewHolder = null;
        if (viewType == SelectionsPresenter.VIEW_TYPE_SIMPLE) {
            viewHolder = new MusicTrackViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_list_item, parent, false));
        } else {
            viewHolder = new SelectionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.selection_list_item, parent, false));
        }
        viewHolder.setContext(getContext());
        viewHolder.setItemClickListener(this);

        return viewHolder;
    }

    @NonNull
    @Override
    protected MusicTrackViewPresenter createPresenter(@NonNull MusicTrack track) {
        if(track.mediaItem.isBrowsable()) {
            SelectionsPresenter presenter = getPresenter();
            presenter.setModel(track);
            presenter.setMultipleSelection(isMultipleSelection());
            return presenter;
        }else{
            MusicTrackViewPresenter presenter = getPresenter();
            presenter.setModel(track);
            presenter.setMultipleSelection(isMultipleSelection());
            return presenter;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).mediaItem.isBrowsable()) {
            return SelectionsPresenter.VIEW_TYPE_SELECTION;
        } else {
            return SelectionsPresenter.VIEW_TYPE_SIMPLE;
        }
    }
}
