package com.digiapp.jilmusic.offlineView.views;

public interface MediaItemView {
    void setChecked(boolean checked);
    void setCheckedVisible(boolean visible);
    void setTitle(String value);
    void setDescription(String value);
    void setFavourite(boolean value);
    void setFavouriteVisible(boolean visible);
    void setImageURI(String path);
    void setItemPlaying(boolean playing);
    void confirmUserDelete();
    void showMessage(String message);
}
