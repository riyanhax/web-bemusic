package com.digiapp.jilmusic.fullScreenPlayer.presenters;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.MusicService;
import com.digiapp.jilmusic.R;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.beans.SelectionList;
import com.digiapp.jilmusic.components.EventType;
import com.digiapp.jilmusic.dao.MusicRoomDatabase;
import com.digiapp.jilmusic.dao.MusicTrackDAO;
import com.digiapp.jilmusic.dao.SelectionsDAO;
import com.digiapp.jilmusic.fullScreenPlayer.models.PLAY_TYPE;
import com.digiapp.jilmusic.utils.Core;
import com.digiapp.jilmusic.utils.LogHelper;
import com.digiapp.jilmusic.utils.MediaIDHelper;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


import static android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL;
import static android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_NONE;
import static android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL;
import static android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE;

public class FullScreenPlayerPresenter {

    private static final String TAG = LogHelper.makeLogTag(FullScreenPlayerPresenter.class);
    WeakReference<View> mView;

    private MusicTrack mCurrentTrack;
    private PLAY_TYPE playingType;
    private final Handler mHandler = new Handler();
    private MediaBrowserCompat mMediaBrowser;

    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
    private boolean mRepeat = false;
    private boolean mShuffle = false;

    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduleFuture;
    private PlaybackStateCompat mLastPlaybackState;

    private final MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            LogHelper.d(TAG, "onPlaybackstate changed", state);
            updatePlaybackState(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata != null) {
                updateMediaDescription(metadata.getDescription());
                updateDuration(metadata);
            }
        }
    };

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    LogHelper.d(TAG, "onConnected");
                    try {
                        connectToSession(mMediaBrowser.getSessionToken());
                    } catch (RemoteException e) {
                        LogHelper.e(TAG, e, "could not connect media controller");
                    }
                }
            };

    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            if(mView.get()==null){
                return;
            }
            mView.get().updateProgress(mLastPlaybackState,isVideo());
        }
    };

    public FullScreenPlayerPresenter(View view){
        this.mView = new WeakReference<>(view);

        playingType = PLAY_TYPE.VIDEO; // by default for all video files
        mMediaBrowser = new MediaBrowserCompat(AppObj.getGlobalContext(),
                new ComponentName(AppObj.getGlobalContext(), MusicService.class), mConnectionCallback, null);
    }

    public interface View{
        void updateDuration(int maxDuration);
        void setBackgroundImage(Bitmap bitmap);
        void finish();
        void showToast(String message);
        void showSnackBar(String message);
        int getVideoState();
        boolean isVideoPlaying();
        void playVideo();
        void pauseVideo();
        void updateFavoriteStatus(Drawable drawable);
        void resetProgress(String videoPath);
        void processVideoIfNeeded(MediaDescriptionCompat description);
        void updateNameAlbum(String name,String album,String pathToPoster);
        void updateCastText(String status);
        void updateProgress(PlaybackStateCompat stateCompat,boolean isVideo);

        // media controller
        MediaControllerCompat.TransportControls getTransportControls();
        MediaControllerCompat getController();
        void setMediaController(MediaControllerCompat mediaControllerCompat);

        // player view
        void setStatePlaying(PlaybackStateCompat state,boolean isVideo);
        void setStatePausing(PlaybackStateCompat state,boolean isVideo);
        void setStateNone(PlaybackStateCompat state,boolean isVideo);
        void setStateBuffering(PlaybackStateCompat state,boolean isVideo);
        void updateSkipNextVisibility(PlaybackStateCompat state);
        void switchToAudio();
        void switchToVideo(PlaybackStateCompat stateCompat);
        void videoSeekTo(int progress);
        void audioSeekTo(int progress);
    }

    public void onDestroy(){
        stopSeekbarUpdate();
        mExecutorService.shutdown();
    }

    public void onStart(){
        if (mMediaBrowser != null) {
            mMediaBrowser.connect();
        }
    }

    public void onStop(){
        if (mMediaBrowser != null) {
            mMediaBrowser.disconnect();
        }
        MediaControllerCompat controllerCompat = mView.get().getController();
        if (controllerCompat != null) {
            controllerCompat.unregisterCallback(mCallback);
        }
    }

    public void onPause(boolean isCurrentPlay){
        if (isVideo()) {
            if (isCurrentPlay) {
                switchMediaAction(); // switching to audio
            }
        }
    }

    public void onShuffle(){
        if(mView.get()==null){
            return;
        }

        mShuffle = !mShuffle;

        MediaControllerCompat.TransportControls controls = mView.get().getTransportControls();
        if (mShuffle) {
            controls.setShuffleMode(SHUFFLE_MODE_ALL);
        } else {
            controls.setShuffleMode(SHUFFLE_MODE_NONE);
        }
    }

    public MusicTrack getCurrentTrack() {
        return mCurrentTrack;
    }

    public void onBackPressed(){
        stopSeekbarUpdate();
    }

    public void onRepeat(){
        if(mView.get()==null){
            return;
        }

        MediaControllerCompat.TransportControls controls = mView.get().getTransportControls();
        mRepeat = !mRepeat; // changing current status
        if (mRepeat) {
            controls.setRepeatMode(REPEAT_MODE_ALL);
        } else {
            controls.setRepeatMode(REPEAT_MODE_NONE);
        }
    }

    public void onSkipNext(){

        View view = mView.get();
        if(view == null){
            return;
        }

        MediaControllerCompat.TransportControls controls = mView.get().getTransportControls();
        controls.skipToNext();

        view.resetProgress(mCurrentTrack.localPath);
    }

    public void onStopTrackingTouch(int progress){
        scheduleSeekbarUpdate();
        if (isVideo()) {
            mView.get().videoSeekTo(progress);
        } else {
            mView.get().audioSeekTo(progress);
        }
    }

    public void onSkipPrev(){
        MediaControllerCompat.TransportControls controls = mView.get().getTransportControls();
        controls.skipToPrevious();

        mView.get().resetProgress(mCurrentTrack.localPath);
    }

    public void onPlayPause(){

        View view = mView.get();
        if(view == null){
            return;
        }

        if (isVideo()) {
            if (view.isVideoPlaying()) {
                view.pauseVideo();
            } else {
                view.playVideo();
                scheduleSeekbarUpdate();
            }

        } else {
            PlaybackStateCompat state = view.getController().getPlaybackState();
            if (state != null) {
                MediaControllerCompat.TransportControls controls = view.getTransportControls();
                switch (state.getState()) {
                    case PlaybackStateCompat.STATE_PLAYING: // fall through
                    case PlaybackStateCompat.STATE_BUFFERING:
                        controls.pause();
                        stopSeekbarUpdate();
                        break;
                    case PlaybackStateCompat.STATE_PAUSED:
                    case PlaybackStateCompat.STATE_STOPPED:
                        controls.play();
                        scheduleSeekbarUpdate();
                        break;
                    default:
                        LogHelper.d(TAG, "onClick with state ", state.getState());
                }
            }
        }
    }

    public void switchMediaAction() {

        if (mCurrentTrack == null) {
            return;
        }

        if (!mCurrentTrack.isVideo) {
            return;
        }

        if (playingType == PLAY_TYPE.VIDEO) {
            playingType = PLAY_TYPE.AUDIO;
            mView.get().switchToAudio();

        } else {
            playingType = PLAY_TYPE.VIDEO;
            mView.get().switchToVideo(mLastPlaybackState);
        }

        stopSeekbarUpdate();

        // Only update from the intent if we are not recreating from a config change:
       // TODO:: updateFromParams(getIntent());
    }

    public boolean isVideo() {
        boolean isVideoFile = mCurrentTrack != null
                && mCurrentTrack.isVideo;

        if (!isVideoFile) {
            return false;
        }

        return playingType == PLAY_TYPE.VIDEO;
    }

    public void setPlayingType(PLAY_TYPE playingType) {
        this.playingType = playingType;
    }

    public void stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }

    private void updateDuration(MediaMetadataCompat metadata) {
        if (metadata == null) {
            LogHelper.d(TAG, "updateDuration called metadata == null");
            return;
        }
        LogHelper.d(TAG, "updateDuration called ");
        int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);

        if(mView.get()!=null) {
            mView.get().updateDuration(duration);
        }
    }

    private void updateImage(MediaDescriptionCompat description) {

        String id = Core.getMusicId(description.getMediaUri().toString());

        File file = new File(MediaIDHelper.getPicsDirectory(AppObj.getGlobalContext()) + "/" + id + ".png");
        if (!file.exists()) {
            return;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        if(mView.get()!=null) {
            mView.get().setBackgroundImage(bitmap);
        }
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {

        View view = mView.get();
        if(view == null){
            return;
        }

        MediaControllerCompat mediaController = new MediaControllerCompat(
                AppObj.getGlobalContext(), token);
        if (mediaController.getMetadata() == null) {
            view.showToast(AppObj.getGlobalContext().getString(R.string.media_is_null));
            view.finish();

            return;
        }

        view.setMediaController(mediaController);

        mediaController.registerCallback(mCallback);
        PlaybackStateCompat state = mediaController.getPlaybackState();
        updatePlaybackState(state);

        MediaMetadataCompat metadata = mediaController.getMetadata();
        if (metadata != null) {
            updateMediaDescription(metadata.getDescription());
            updateDuration(metadata);
        }

        view.updateProgress(mLastPlaybackState,isVideo());
        if (state != null && (state.getState() == PlaybackStateCompat.STATE_PLAYING ||
                state.getState() == PlaybackStateCompat.STATE_BUFFERING)) {
            scheduleSeekbarUpdate();
        }

        // need to switch off current music if we are on video
        prepareAndStartVideo();
    }

    public void updateMediaDescription(final MediaDescriptionCompat description) {
        if (description == null) {
            return;
        }
        LogHelper.d(TAG, "updateMediaDescription called ");

        View view = mView.get();
        if(view == null){
            return;
        }

        new Thread(() -> {
            MusicTrackDAO musicTrackDAO = MusicRoomDatabase.getDatabase(AppObj.getGlobalContext()).musicTrackDAO();
            mCurrentTrack = musicTrackDAO.getById(Integer.valueOf(Core.getMusicId(description.getMediaUri().toString())));

            if (mCurrentTrack != null) {
                final String name = mCurrentTrack.name.isEmpty() ? description.getTitle().toString() : mCurrentTrack.name;
                final String album = mCurrentTrack.album_name.isEmpty() ? description.getSubtitle().toString() : mCurrentTrack.album_name;

                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {
                    view.updateNameAlbum(name,album,mCurrentTrack.localPicturePath==null?"":mCurrentTrack.localPicturePath);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        updateFavoriteUi();
                    }

                    view.processVideoIfNeeded(description);
                    updateImage(description);

                }, 0);
            }

        }).start();
    }

    public void addTrackToFavorite() {
        mCurrentTrack.favorite = !mCurrentTrack.favorite;

        MusicTrackDAO musicTrackDao = MusicRoomDatabase.getDatabase(AppObj.getGlobalContext()).musicTrackDAO();
        musicTrackDao.insert(mCurrentTrack);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            updateFavoriteUi();
        }

        EventBus.getDefault().post(EventType.UPDATE_FAVOURITES);
    }

    public void addTrackToSelection(SelectionList selectionList) {

        SelectionsDAO selectionsDAO = MusicRoomDatabase.getDatabase(AppObj.getGlobalContext()).selectionsDAO();
        try {
            SelectionList selectionListParent = selectionsDAO.getByTrackIdInParent(selectionList.id, mCurrentTrack.id);
            if (selectionListParent != null) {
                if(mView.get()!=null) {
                    mView.get().showSnackBar(AppObj.getGlobalContext().getString(R.string.song_in_selection));
                }
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }


        SelectionList objSel = new SelectionList(selectionList);
        objSel.track_id = mCurrentTrack.id;
        selectionsDAO.insertSelectionList(objSel);

        if(mView.get()!=null) {
            mView.get().showSnackBar(AppObj.getGlobalContext().getString(R.string.added_to_sel));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateFavoriteUi() {

        View view = mView.get();
        if(view == null){
            return;
        }

        if (mCurrentTrack.favorite) {
            view.updateFavoriteStatus(AppObj.getGlobalContext().getDrawable(R.drawable.baseline_favorite_white_36));
        } else {
            view.updateFavoriteStatus(AppObj.getGlobalContext().getDrawable(R.drawable.baseline_favorite_border_white_36));
        }
    }

    public void prepareAndStartVideo() {

        View view = mView.get();
        if(view == null){
            return;
        }

        //if (isVideo()
        //        && view.getVideoState() == CURRENT_STATE_NORMAL) {
        if (isVideo()) {
            if (view.getController() != null) {
                view.getTransportControls().stop();
            }
            scheduleSeekbarUpdate();
            view.playVideo();
        }
    }

    public void scheduleSeekbarUpdate() {
        stopSeekbarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    () -> mHandler.post(mUpdateProgressTask), PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
        }
    }

    private void updatePlaybackState(PlaybackStateCompat state) {

        if(state == null) {
            return;
        }

        View view = mView.get();
        if(view == null){
            return;
        }

        mLastPlaybackState = state;
        MediaControllerCompat controllerCompat = view.getController();
        if (controllerCompat != null && controllerCompat.getExtras() != null) {
            String castName = controllerCompat.getExtras().getString(MusicService.EXTRA_CONNECTED_CAST);
            String line3Text = castName == null ? "" : AppObj.getGlobalContext().getResources()
                    .getString(R.string.casting_to_device, castName);
            view.updateCastText(line3Text);
        }

        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                view.setStatePlaying(mLastPlaybackState,isVideo());
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                view.setStatePausing(mLastPlaybackState,isVideo());
                break;
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                view.setStateNone(mLastPlaybackState,isVideo());
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                view.setStateBuffering(mLastPlaybackState,isVideo());
                break;
            default:
                LogHelper.d(TAG, "Unhandled state ", state.getState());
        }

        // if(!isVideo()){
           //  stopSeekbarUpdate(); ???
        // }

        view.updateSkipNextVisibility(state);
    }

}
