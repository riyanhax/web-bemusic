package com.digiapp.jilmusic.selectionsView.presenters;

import android.os.Looper;
import android.view.MenuItem;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.R;
import com.digiapp.jilmusic.beans.SelectionList;
import com.digiapp.jilmusic.components.EventType;
import com.digiapp.jilmusic.dao.MusicRoomDatabase;
import com.digiapp.jilmusic.dao.SelectionsDAO;
import com.digiapp.jilmusic.offlineView.presenters.MusicTrackViewPresenter;
import com.digiapp.jilmusic.offlineView.views.CustomPopupMenu;
import com.digiapp.jilmusic.selectionsView.views.SelectionViewHolder;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.logging.Handler;

public class SelectionsPresenter extends MusicTrackViewPresenter {

    public static final int VIEW_TYPE_SIMPLE = 0;
    public static final int VIEW_TYPE_SELECTION = 1;

    @Override
    protected void updateView() {
        if (view().getItemViewType() == VIEW_TYPE_SELECTION) {
            view().setFavouriteVisible(false);
            view().setMenuVisible(true);
            view().setTitle(model.name);
        } else {
            super.updateView();
            view().setMenuVisible(true);
        }
    }

    public void deleteSelection() {
        new Thread(() -> {
            SelectionsDAO selectionsDAO = MusicRoomDatabase.getDatabase(view().getContext()).selectionsDAO();

            // delete child roots
            List<SelectionList> list = selectionsDAO.getByParent(model.id);
            for (SelectionList sel : list) {
                selectionsDAO.deleteById(sel.id);
            }

            // delete main root
            selectionsDAO.deleteById(model.id);

            new android.os.Handler(Looper.getMainLooper()).post(() -> EventBus.getDefault().post(EventType.UPDATE_ALL));

        }).start();
    }

    @Override
    public void onMenuClicked() {
        if(model.mediaItem.isBrowsable()) {
            CustomPopupMenu customPopupMenu = CustomPopupMenu.getCustomPopupMenu(view().getImgMenu(), R.menu.menu_popup_selection, AppObj.getGlobalContext());
            customPopupMenu.setClickListener(new CustomPopupMenu.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    customPopupMenu.dismiss();
                }

                @Override
                public void onItemClick(MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.menu_delete) {
                        ((SelectionViewHolder) view()).confirmUserSelectionDelete();
                    }

                    if (menuItem.getItemId() == R.id.menu_change_name) {
                        SelectionsDAO selectionsDAO = MusicRoomDatabase.getDatabase(view().getContext()).selectionsDAO();
                        SelectionList selection = selectionsDAO.getById(model.id);
                        if (selection != null) {
                            ((SelectionViewHolder) view()).confirmSelectionNameChange(selection);
                        }
                    }
                }
            });
            customPopupMenu.show();
        }else{
            super.onMenuClicked();
        }
    }
}
