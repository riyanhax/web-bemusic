package com.digiapp.jilmusic.selectionsView.views;

import android.os.Looper;
import android.os.Message;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.digiapp.jilmusic.R;
import com.digiapp.jilmusic.beans.SelectionList;
import com.digiapp.jilmusic.components.EventType;
import com.digiapp.jilmusic.offlineView.views.MusicTrackViewHolder;
import com.digiapp.jilmusic.selectionsView.presenters.SelectionsPresenter;
import com.digiapp.jilmusic.utils.Core;

import org.greenrobot.eventbus.EventBus;

import static com.digiapp.jilmusic.utils.Core.CALLBACK_SIMPLE_STRING;

public class SelectionViewHolder extends MusicTrackViewHolder {
    public SelectionViewHolder(View itemView) {
        super(itemView);
    }

    public void confirmUserSelectionDelete() {
        MaterialDialog.Builder builder = Core.getDialogBuilder(getContext())
                .title(R.string.delete_selection_question)
                .positiveText(R.string.text_yes)
                .negativeText(R.string.text_cancel)
                .onPositive((dialog, which) -> {
                    ((SelectionsPresenter) presenter).deleteSelection();
                });
        builder.show();
    }

    public void confirmSelectionNameChange(SelectionList selection){
        if(selection==null){
            return;
        }
        Core.getSelectionDialog(getContext(), selection, msg -> {
            if (msg.what == CALLBACK_SIMPLE_STRING
                    && msg.obj != null) {
                showMessage(msg.obj.toString());
            }
            new android.os.Handler(Looper.getMainLooper()).post(() -> EventBus.getDefault().post(EventType.UPDATE_ALL));

            return false;
        }).show();
    }
}
