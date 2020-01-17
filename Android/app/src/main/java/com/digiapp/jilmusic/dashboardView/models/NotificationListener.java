package com.digiapp.jilmusic.dashboardView.models;

import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.dashboardView.presenters.DashboardPresenter;
import com.digiapp.jilmusic.dashboardView.views.DashboardActivity;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.notification.BaseNotificationItem;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationListener;

import java.lang.ref.WeakReference;

public class NotificationListener extends FileDownloadNotificationListener {

    private WeakReference<DashboardPresenter> wActivity;
    private MusicTrack musicTrack;

    public NotificationListener(WeakReference<DashboardPresenter> wActivity) {
        super(wActivity.get().notificationHelper);
        this.wActivity = wActivity;
    }

    public void setMusicTrack(MusicTrack musicTrack) {
        this.musicTrack = musicTrack;
    }

    @Override
    protected BaseNotificationItem create(BaseDownloadTask task) {
        if (musicTrack != null) {
            return new NotificationItem(task.getId(), musicTrack.name, musicTrack.album_name);
        } else {
            return new NotificationItem(task.getId(), "", "video");
        }
    }

    @Override
    public void addNotificationItem(BaseDownloadTask task) {
        super.addNotificationItem(task);
    }

    @Override
    public void destroyNotification(BaseDownloadTask task) {
        super.destroyNotification(task);
        if (wActivity.get() != null) {
            wActivity.get().downloadId = 0;
        }
    }

    @Override
    protected boolean interceptCancel(BaseDownloadTask task,
                                      BaseNotificationItem n) {
        // in this demo, I don't want to cancel the notification, just show for the test
        // so return true
        return true;
    }

    @Override
    protected boolean disableNotification(BaseDownloadTask task) {
        return super.disableNotification(task);
    }

    @Override
    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        super.pending(task, soFarBytes, totalBytes);
        //Toast.makeText(WebViewActivity.this.getApplicationContext(),"soFarBytespending: " + soFarBytes,Toast.LENGTH_SHORT).show();
        if (wActivity.get() != null) {
        }
    }

    @Override
    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        super.progress(task, soFarBytes, totalBytes);
        //Toast.makeText(WebViewActivity.this.getApplicationContext(),"soFarBytes: " + soFarBytes,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void completed(BaseDownloadTask task) {
        super.completed(task);
        // for video we should rename TODO

    }
}
