package com.digiapp.jilmusic.offlineView.presenters;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.MenuItem;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.R;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.beans.SelectionList;
import com.digiapp.jilmusic.components.EventType;
import com.digiapp.jilmusic.dao.MusicRoomDatabase;
import com.digiapp.jilmusic.dao.MusicTrackDAO;
import com.digiapp.jilmusic.dao.SelectionsDAO;
import com.digiapp.jilmusic.model.BasePresenter;
import com.digiapp.jilmusic.offlineView.views.CustomPopupMenu;
import com.digiapp.jilmusic.offlineView.views.MusicTrackViewHolder;
import com.digiapp.jilmusic.utils.Core;
import com.digiapp.jilmusic.utils.MediaIDHelper;

import org.greenrobot.eventbus.EventBus;

import static com.digiapp.jilmusic.offlineView.views.CustomPopupMenu.ADD_NEW_SELECTION;
import static com.digiapp.jilmusic.utils.Core.CALLBACK_SIMPLE_STRING;

public class MusicTrackViewPresenter extends BasePresenter<MusicTrack, MusicTrackViewHolder> {

    public static final int STATE_INVALID = -1;
    public static final int STATE_NONE = 0;
    public static final int STATE_PLAYABLE = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_PLAYING = 3;
    private MusicTrackDAO musicTrackDAO = MusicRoomDatabase.getDatabase(AppObj.getGlobalContext()).musicTrackDAO();

    public boolean isMultipleSelection = false;
    public void setMultipleSelection(boolean multipleSelection) {
        isMultipleSelection = multipleSelection;
    }

    @Override
    protected void updateView() {
        view().setCheckedVisible(false);
        view().setDescription(model.album_name);
        view().setTitle(model.name);
        view().setFavourite(model.favorite);

        if (model.localPicturePath != null) {
            view().setImageURI(model.localPicturePath);
        }

        view().setMenuVisible(false);
        view().setCheckedVisible(isMultipleSelection);
        view().setChecked(model.isSelected);

        if(getMediaItemState((Activity)view().getContext(),model.mediaItem)==MusicTrackViewHolder.STATE_PLAYING
                || getMediaItemState((Activity)view().getContext(),model.mediaItem)==MusicTrackViewHolder.STATE_PAUSED){
            view().setItemPlaying(true);
        }else{
            view().setItemPlaying(false);
        }
    }

    public void setSelected(boolean selected){
        model.isSelected = selected;
    }

    public void onFavouritesButtonClicked() {

        if(!model.mediaItem.isBrowsable()) {
            model.favorite = !model.favorite;
            musicTrackDAO.insert(model);
        }
        updateView();

        EventBus.getDefault().post(EventType.UPDATE_FAVOURITES);
    }

    public void deleteModel() {
        new Thread(() -> {
            MusicTrackDAO musicTrackDAO = MusicRoomDatabase.getDatabase(AppObj.getGlobalContext()).musicTrackDAO();
            musicTrackDAO.deleteById(model.id);

            // delete selections also
            SelectionsDAO selectionsDAO = MusicRoomDatabase.getDatabase(AppObj.getGlobalContext()).selectionsDAO();
            selectionsDAO.deleteSelectionByTrack(model.id);

            new Handler(Looper.getMainLooper()).post(() -> EventBus.getDefault().post(EventType.UPDATE_ALL));
        }).start();

        updateView();
    }

    public static int getMediaItemState(Activity context, MediaBrowserCompat.MediaItem mediaItem) {
        int state = STATE_NONE;
        // Set state to playable first, then override to playing or paused state if needed
        if (mediaItem.isPlayable()) {
            state = STATE_PLAYABLE;
            if (MediaIDHelper.isMediaItemPlaying(context, mediaItem)) {
                state = getStateFromController(context);
            }
        }

        return state;
    }

    public static int getStateFromController(Activity context) {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(context);
        PlaybackStateCompat pbState = controller.getPlaybackState();
        if (pbState == null ||
                pbState.getState() == PlaybackStateCompat.STATE_ERROR) {
            return MusicTrackViewHolder.STATE_NONE;
        } else if (pbState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            return MusicTrackViewHolder.STATE_PLAYING;
        } else {
            return MusicTrackViewHolder.STATE_PAUSED;
        }
    }

    public void addInSelection(long selectionId){
        SelectionsDAO selectionsDAO = MusicRoomDatabase.getDatabase(AppObj.getGlobalContext()).selectionsDAO();
        // check first if it is in selection already
        SelectionList selectionListParent = selectionsDAO.getByTrackIdInParent(selectionId, model.id);
        if (selectionListParent != null) {
            view().showMessage(AppObj.getGlobalContext().getString(R.string.song_in_selection));
            return;
        }

        SelectionList rootSelection = selectionsDAO.getById(selectionId);
        if (rootSelection != null) {
            SelectionList objSel = new SelectionList(rootSelection);
            objSel.track_id = model.id;
            selectionsDAO.insertSelectionList(objSel);

            view().showMessage(AppObj.getGlobalContext().getString(R.string.text_added));
        } else {
            view().showMessage("Can't find selection");
        }
    }

    public void onMenuClicked() {
        CustomPopupMenu customPopupMenu = CustomPopupMenu.getCustomPopupMenu(view().getImgMenu(), model, AppObj.getGlobalContext());
        if(model.mediaItem.isPlayable()) {
            customPopupMenu.setClickListener(new CustomPopupMenu.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {

                    if (!customPopupMenu.isSelectionVisible()) {
                        if (position == 2) {
                            customPopupMenu.showSelectionMenu();
                            return;
                        }
                    }

                    if (position == ADD_NEW_SELECTION) {
                        Core.getSelectionDialog(view().getContext(), msg -> {
                            if (msg.what == CALLBACK_SIMPLE_STRING
                                    && msg.obj != null) {
                                view().showMessage(msg.obj.toString());
                                customPopupMenu.showSelectionMenu(); // update current selection menu
                            }
                            return false;
                        }).show();
                        return;
                    }
                    customPopupMenu.dismiss();
                }

                @Override
                public void onItemClick(MenuItem menuItem) {

                    if (customPopupMenu.isSelectionVisible()) {

                        long selectionId = 0;
                        try {
                            selectionId = Long.parseLong(menuItem.getTitleCondensed().toString());
                        } catch (NumberFormatException ex) {
                            ex.printStackTrace();
                            return;
                        }
                        if (selectionId == 0) {
                            return;
                        }
                        addInSelection(selectionId);

                        return;
                    }

                    if (menuItem.getItemId() == R.id.menu_favorite) {
                        onFavouritesButtonClicked();
                    }

                    if (menuItem.getItemId() == R.id.menu_delete) {
                        view().confirmUserDelete();
                    }

                }
            });
            customPopupMenu.show();
        }
    }

}