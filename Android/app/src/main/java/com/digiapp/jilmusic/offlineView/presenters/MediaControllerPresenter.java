package com.digiapp.jilmusic.offlineView.presenters;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.dao.MusicRoomDatabase;
import com.digiapp.jilmusic.dao.MusicTrackDAO;
import com.digiapp.jilmusic.offlineView.models.MediaControllerCallback;
import com.digiapp.jilmusic.utils.LogHelper;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MediaControllerPresenter {

    private static final String TAG = LogHelper.makeLogTag(MediaControllerPresenter.class);
    private PlaybackStateCompat mLastPlaybackState;
    MediaControllerCallback mCallback;

    // progress updater
    private ScheduledFuture<?> mScheduleFuture;
    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();
    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;

    WeakReference<Activity> mActivity;
    View mView;

    public MediaControllerPresenter(Activity activity, View view) {
        mActivity = new WeakReference<>(activity);
        this.mView = view;
    }

    public interface View {
        void onProgressUpdated(int progress);

        void onError(String message);

        void onMetadataChanged(MediaMetadataCompat metadataCompat, MediaControllerCompat controller);

        void onPlaybackStateChanged(PlaybackStateCompat state);

        void onControllerStatusChanged(boolean available);
    }

    private final Runnable mUpdateProgressTask = () -> updateProgress();

    public Activity getActivity() {
        return mActivity.get();
    }

    public void nextMedia() {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            controller.getTransportControls().skipToNext();
        }
    }

    public void prevMedia() {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            controller.getTransportControls().skipToPrevious();
        }
    }

    public void playMedia() {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            controller.getTransportControls().play();
        }
    }

    public void pauseMedia() {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            controller.getTransportControls().pause();
        }
    }

    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        LogHelper.d(TAG, "onPlaybackStateChanged ", state);

        if (state == null) {
            return;
        }

        mLastPlaybackState = state;

        if (getActivity() == null) {
            LogHelper.w(TAG, "onPlaybackStateChanged called when getActivity null," +
                    "this should not happen if the callback was properly unregistered. Ignoring.");
            return;
        }
        if (state == null) {
            return;
        }

        if (getActivity() instanceof Activity) { // special rules for this
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
            MediaMetadataCompat mediaMetadataCompat = controller.getMetadata();
            if (mediaMetadataCompat != null) {
                if (mView != null) {
                    mView.onMetadataChanged(mediaMetadataCompat, controller);
                }
            }
        }

        if (mView != null) {
            mView.onPlaybackStateChanged(state);
        }

        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                scheduleSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                stopSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                stopSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                stopSeekbarUpdate();
                break;
            default:
                LogHelper.d(TAG, "Unhandled state ", state.getState());
        }
    }

    public void onMetadataChanged(final MediaMetadataCompat metadata) {
        LogHelper.d(TAG, "onMetadataChanged ", metadata);
        if (getActivity() == null) {
            LogHelper.w(TAG, "onMetadataChanged called when getActivity null," +
                    "this should not happen if the callback was properly unregistered. Ignoring.");
            return;
        }
        if (metadata == null) {
            return;
        }

        if (mView != null) {
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
            mView.onMetadataChanged(metadata, controller);
        }
    }

    public void createMediaController(MediaSessionCompat.Token token) {

        try {
            MediaControllerCompat mediaController = new MediaControllerCompat(
                    AppObj.getGlobalContext(), token);
            if (mediaController.getMetadata() == null) {
                return;
            }
        } catch (Exception ex) {
            if (mView != null) {
                mView.onError(ex.toString());
            }
        }
    }

    public void onConnected() {
        try {
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
            LogHelper.d(TAG, "onConnected, mediaController==null? ", controller == null);
            if (controller != null) {
                mCallback = new MediaControllerCallback(this); // new
                onMetadataChanged(controller.getMetadata());
                onPlaybackStateChanged(controller.getPlaybackState());
                controller.registerCallback(mCallback);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            if (mView != null) {
                mView.onError(ex.toString());
            }
        }
    }

    public void onStop() {
        LogHelper.d(TAG, "fragment.onStop");
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            if (mCallback != null) {
                controller.unregisterCallback(mCallback);
            }
        }
    }

    public void onStart() {
        LogHelper.d(TAG, "fragment.onStart");
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            onConnected();
        }
    }

    public PlaybackStateCompat getLastPlaybackState() {
        return mLastPlaybackState;
    }

    public void scheduleSeekbarUpdate() {
        stopSeekbarUpdate();
        if (!mExecutorService.isShutdown()) {
            if (mView != null) {
                mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                        mUpdateProgressTask, PROGRESS_UPDATE_INITIAL_INTERVAL,
                        PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void seekTo(int progress) {
        MediaControllerCompat mediaControllerCompat = MediaControllerCompat.getMediaController(getActivity());
        if (mediaControllerCompat != null) {
            mediaControllerCompat.getTransportControls().seekTo(progress);
        }
    }

    public void stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }

    private void updateProgress() {
        if (mLastPlaybackState == null) {
            return;
        }
        long currentPosition = mLastPlaybackState.getPosition();
        if (mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            long timeDelta = SystemClock.elapsedRealtime() -
                    mLastPlaybackState.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
        }

        if (mView != null) {
            mView.onProgressUpdated((int) currentPosition);
        }
    }

    public void checkIfControllerAvailable(){
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(getActivity());
        if (mediaController == null ||
                mediaController.getMetadata() == null ||
                mediaController.getPlaybackState() == null) {
            if(mView!=null){
                mView.onControllerStatusChanged(false);
            }
            return;
        }

        switch (mediaController.getPlaybackState().getState()) {
            case PlaybackStateCompat.STATE_ERROR:
            case PlaybackStateCompat.STATE_NONE:
                if(mView!=null){
                    mView.onControllerStatusChanged(false);
                }
            default:
                if(mView!=null){
                    mView.onControllerStatusChanged(true);
                }
        }
    }
}
