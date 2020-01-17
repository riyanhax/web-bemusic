package com.digiapp.jilmusic.dashboardView.presenters;

import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.R;
import com.digiapp.jilmusic.api.beans.UserInfo;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.components.EventType;
import com.digiapp.jilmusic.dao.MusicRoomDatabase;
import com.digiapp.jilmusic.dao.MusicTrackDAO;
import com.digiapp.jilmusic.dashboardView.models.NotificationItem;
import com.digiapp.jilmusic.dashboardView.models.NotificationListener;
import com.digiapp.jilmusic.dashboardView.models.OnMusicTrackDownloadFinished;
import com.digiapp.jilmusic.dashboardView.models.YouTubeLink;
import com.digiapp.jilmusic.dashboardView.views.DashboardActivity;
import com.digiapp.jilmusic.media.MediaTagHelper;
import com.digiapp.jilmusic.utils.Core;
import com.digiapp.jilmusic.utils.MediaIDHelper;
import com.digiapp.jilmusic.utils.NetworkHelper;
import com.google.gson.Gson;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationHelper;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class DashboardPresenter {

    View mView;

    public int downloadId = 0;
    public NotificationListener listener;
    public final FileDownloadNotificationHelper<NotificationItem> notificationHelper =
            new FileDownloadNotificationHelper<>();

    public DashboardPresenter(View view){
        mView = view;

        listener = new NotificationListener(new WeakReference<DashboardPresenter>(this));
    }

    public interface View{
        void showWebFragment();
        void showOfflineFragment();
        void showNoInternetFragment();
        void hideAll();
        void updateUserUI(UserInfo userInfo);
        void showDownloadDialog(MusicTrack musicTrack);
        void showToast(String message);
        void showSelectorDialog(SparseArray<YtFile> ytFiles, MusicTrack musicTrack);

        void showProgDialog(String message);
        void dismissProgDialog();
    }

    public void getTrackObj(final String track_to_mobile) {

        Gson gson = new Gson();
        final MusicTrack musicTrack = gson.fromJson(track_to_mobile, MusicTrack.class);

        if (musicTrack.youtube_id != null
                && musicTrack.url != null
                && !musicTrack.url.isEmpty()) {

            mView.showDownloadDialog(musicTrack);

        } else {

            if (musicTrack.url == null
                    && musicTrack.youtube_id == null) {
                mView.showToast(AppObj.getGlobalContext().getString(R.string.link_broken));
                return;
            }

            if (musicTrack.youtube_id != null
                    && !musicTrack.youtube_id.isEmpty()) {
                downloadYoutube(musicTrack);
            } else {
                downloadTrack(musicTrack);
            }
        }
    }

    public void downloadTrack(MusicTrack musicTrack) {

        if (downloadId != musicTrack.id) {
            if (listener != null) {
                listener.setMusicTrack(musicTrack);
            }

            mView.showToast(AppObj.getGlobalContext().getString(R.string.download_started));

            String localPath = MediaIDHelper.getMediaDirectory(AppObj.getGlobalContext()).getAbsolutePath() + "/" + musicTrack.id;
            downloadId = FileDownloader.getImpl().create(musicTrack.url)
                    .setPath(localPath)
                    .setListener(listener)
                    .setTag(0, musicTrack)
                    .setTag(1, localPath)
                    .setTag(2, musicTrack.url.endsWith(".mp4")?true:false)
                    .setAutoRetryTimes(3)
                    .addFinishListener(new OnMusicTrackDownloadFinished(this))
                    .start();
        }
    }

    public void downloadTrack(MusicTrack musicTrack, YouTubeLink youTubeLink){
        if (downloadId != musicTrack.id) {
            if (listener != null) {
                listener.setMusicTrack(musicTrack);
            }

            mView.showToast(AppObj.getGlobalContext().getString(R.string.download_started));

            String extension = youTubeLink.ytFile.getFormat().getExt();

            String path = MediaIDHelper.getVideoDirectory(AppObj.getGlobalContext()).getAbsolutePath() + "/" + musicTrack.id + "." + extension;;
            downloadId = FileDownloader.getImpl().create(youTubeLink.ytFile.getUrl())
                    .setPath(path)
                    .setListener(listener)
                    .setAutoRetryTimes(3)
                    .setTag(0, musicTrack)
                    .setTag(1, path)
                    .setTag(2, extension.equalsIgnoreCase("m4a")?false:true)
                    .addFinishListener(new OnMusicTrackDownloadFinished(this))
                    .start();
        }
    }

    public void downloadPic(MusicTrack musicTrack) {
        if (musicTrack.album == null
                || musicTrack.album.image == null
                || musicTrack.album.image.isEmpty()) {
            return;
        }

        FileDownloader.getImpl().create(musicTrack.album.image)
                .setPath(MediaIDHelper.getPicsDirectory(AppObj.getGlobalContext()).getAbsolutePath() + "/" + musicTrack.id + ".png")
                .setTag(musicTrack)
                .addFinishListener(task -> {
                    if (task.getTag() instanceof MusicTrack) {
                        // doing nothing here
                        musicTrack.localPicturePath = task.getPath();
                        saveUpdateTrack(musicTrack);

                        new Handler(Looper.getMainLooper()).post(() -> EventBus.getDefault().post(EventType.UPDATE_ALL));
                    }
                })
                .start();
    }

    public void downloadYoutube(final MusicTrack musicTrack) {
        String youtubeLink = "http://youtube.com/watch?v=" + musicTrack.youtube_id;

        mView.showProgDialog(AppObj.getGlobalContext().getString(R.string.please_wait));
        new YouTubeExtractor(AppObj.getGlobalContext()) {
            @Override
            protected void onCancelled() {
                super.onCancelled();
                mView.dismissProgDialog();
                mView.showToast(AppObj.getGlobalContext().getString(R.string.goes_wrong));
                //Toast.makeText(getApplicationContext(), "Ooops, something wrong with Youtube url", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                mView.dismissProgDialog();

                if (ytFiles != null) {
                    mView.showSelectorDialog(ytFiles, musicTrack);
                } else {
                    //Toast.makeText(getApplicationContext(), "Ooops, something wrong with Youtube url", Toast.LENGTH_SHORT).show();
                    mView.showToast(AppObj.getGlobalContext().getString(R.string.goes_wrong));
                }
            }
        }.extract(youtubeLink, false, false);
    }

    public void saveUpdateTrack(MusicTrack mTrack) {
        new Thread(() -> {
            MusicTrackDAO musicTrackDAO = MusicRoomDatabase.getDatabase(AppObj.getGlobalContext()).musicTrackDAO();
            musicTrackDAO.insert(mTrack);
        }).start();
    }

    public void checkUserInfo() {
        UserInfo userInfo;
        try {
            userInfo = Core.getCurrentUser();
            mView.updateUserUI(userInfo);
        } catch (Exception ex) { // user not found
            ex.printStackTrace();
            return;
        }

        if (NetworkHelper.isOnline(AppObj.getGlobalContext())) {
            Core.checkSubscription();
        }
    }
}
