package com.digiapp.jilmusic.offlineView.models;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.digiapp.jilmusic.R;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.beans.SelectionItems;
import com.digiapp.jilmusic.beans.SelectionList;
import com.digiapp.jilmusic.model.MvpRecyclerListAdapter;
import com.digiapp.jilmusic.offlineView.presenters.MusicTrackViewPresenter;
import com.digiapp.jilmusic.offlineView.views.MusicTrackViewHolder;

import java.util.ArrayList;

public abstract class AbstractAdapter extends MvpRecyclerListAdapter<MusicTrack, MusicTrackViewPresenter, MusicTrackViewHolder> implements MusicTrackViewHolder.ItemClickListener {

    boolean isMultipleSelection = false;

    public void setMultipleSelection(boolean multipleSelection) {
        isMultipleSelection = multipleSelection;
        for(int i=0;i<getItemCount();i++){

            MusicTrack track = getItem(i);
            MusicTrackViewPresenter existingPresenter = presenters.get(getModelId(track));
            existingPresenter.setMultipleSelection(isMultipleSelection);
            if(existingPresenter!=null){
                existingPresenter.setModel(track);
            }
            notifyItemChanged(i);
        }
    }

    public boolean isMultipleSelection() {
        return isMultipleSelection;
    }

    public ArrayList<MusicTrack> getAllSelected(){
        ArrayList<MusicTrack> selected = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            if(getItem(i).isSelected){
                selected.add(getItem(i));
            }
        }
        return selected;
    }

    public void selectAll(){
        for (int i = 0; i < getItemCount(); i++) {
            getPresenter(getItem(i)).setSelected(true);
        }
        notifyDataSetChanged();
    }

    public void deleteSelected(){
        for (int i = 0; i < getItemCount(); i++) {
            if(getItem(i).isSelected){
                getPresenter(getItem(i)).deleteModel();
            }
        }
        notifyDataSetChanged();
    }

    public void moveSelectedToSelection(SelectionList selection){
        for (int i = 0; i < getItemCount(); i++) {
            if(getItem(i).isSelected){
                getPresenter(getItem(i)).addInSelection(selection.id);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public MusicTrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MusicTrackViewHolder musicTrackViewHolder = new MusicTrackViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_list_item, parent, false));
        musicTrackViewHolder.setContext(getContext());
        musicTrackViewHolder.setItemClickListener(this);

        return musicTrackViewHolder;
    }

    @NonNull
    @Override
    protected MusicTrackViewPresenter createPresenter(@NonNull MusicTrack track) {
        MusicTrackViewPresenter presenter = getPresenter();
        presenter.setModel(track);
        presenter.setMultipleSelection(isMultipleSelection);
        return presenter;
    }

    public abstract MusicTrackViewPresenter getPresenter();

    public abstract Context getContext();

    @NonNull
    @Override
    protected Object getModelId(@NonNull MusicTrack model) {
        return model.id;
    }

    @Override
    public void onItemClicked(int pos) {
        if (itemClickListener != null) {
            itemClickListener.onItemClicked(pos);
        }
    }

    @Override
    public void onLongItemClicked(int pos) {
        if (itemClickListener != null) {
            itemClickListener.onLongItemClicked(pos);
        }
    }
}
