package com.digiapp.jilmusic.dashboardView.models;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.dashboardView.presenters.DashboardPresenter;
import com.digiapp.jilmusic.media.MediaTagHelper;
import com.liulishuo.filedownloader.BaseDownloadTask;

import java.io.File;


public class OnMusicTrackDownloadFinished implements BaseDownloadTask.FinishListener {

    DashboardPresenter mPresenter;

    public OnMusicTrackDownloadFinished(DashboardPresenter presenter){
        mPresenter = presenter;
    }

    @Override
    public void over(BaseDownloadTask task) {
        if (task.getTag(0) instanceof MusicTrack) {
            MusicTrack mTrack = (MusicTrack) task.getTag(0);
            mTrack.localPath = (String) task.getTag(1);
            mTrack.isVideo = (Boolean) task.getTag(2);
            mPresenter.saveUpdateTrack(mTrack);

            // now trying to download pic
            mPresenter.downloadPic(mTrack);
        }
    }
}
