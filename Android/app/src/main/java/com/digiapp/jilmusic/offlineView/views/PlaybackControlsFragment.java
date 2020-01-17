/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.digiapp.jilmusic.offlineView.views;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.digiapp.jilmusic.MusicService;
import com.digiapp.jilmusic.R;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.dashboardView.views.DashboardActivity;
import com.digiapp.jilmusic.fullScreenPlayer.views.FullScreenPlayerActivity;
import com.digiapp.jilmusic.model.RemoteJSONSource;
import com.digiapp.jilmusic.offlineView.models.SeekBarChangeMediaListener;
import com.digiapp.jilmusic.offlineView.presenters.MediaControllerPresenter;
import com.digiapp.jilmusic.offlineView.presenters.PlaybackControlsPresenter;
import com.digiapp.jilmusic.utils.LogHelper;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A class that shows the Media Queue to the user.
 */
public class PlaybackControlsFragment extends Fragment implements MediaControllerPresenter.View,PlaybackControlsPresenter.View {

    private static final String TAG = LogHelper.makeLogTag(PlaybackControlsFragment.class);

    @BindView(R.id.play_pause)
    ImageButton mPlayPause;
    @BindView(R.id.prev)
    ImageButton mPrev;
    @BindView(R.id.next)
    ImageButton mNext;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.artist)
    TextView mSubtitle;
    @BindView(R.id.extra_info)
    TextView mExtraInfo;
    @BindView(R.id.seekBar)
    SeekBar mSeekbar;
    @BindView(R.id.img_album_pic)
    ImageView img_album_pic;
    @BindView(R.id.arrow_up)
    ImageView arrow_up;

    private final Handler mHandler = new Handler();

    public boolean videoPlayed = false;
    public long videoProgress = 0;
    public MusicTrack videoTrack = null;
    public String videoTrackId = "";
    public int videoState = -1;

    MediaControllerPresenter mControllerPresenter;
    PlaybackControlsPresenter mPlaybackControlsPresenter;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FullScreenPlayerActivity.REQUEST_CODE) {
            if (resultCode == -1) { // OK

                if (data.getExtras() != null
                        && data.getExtras().get("media") != null) {
                    videoPlayed = true;
                    videoTrack = (MusicTrack) data.getExtras().get("media");
                    videoProgress = data.getLongExtra("progress", 0);
                    videoState = data.getIntExtra("state", -1);

                    try {
                        MediaMetadataCompat mediaMetadataCompat = RemoteJSONSource.buildFromFile(videoTrack.localPath, videoTrack);
                        videoTrackId = mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    this.videoPlayed = false;
                    this.videoTrack = null;
                }

            } else {
                this.videoPlayed = false;
                this.videoTrack = null;
            }
        }
    }

    public void sendPlayCommand() {
        if (mPlayPause != null) {
            mPlayPause.callOnClick();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playback_controls, container, false);
        ButterKnife.bind(this, rootView);
        mControllerPresenter = new MediaControllerPresenter(getActivity(), this);
        mPlaybackControlsPresenter = new PlaybackControlsPresenter(this);

        mPlayPause.setEnabled(true);
        mPlayPause.setOnClickListener(mButtonListener);
        mPrev.setOnClickListener(mButtonListener);
        mNext.setOnClickListener(mButtonListener);
        mSeekbar.setOnSeekBarChangeListener(new SeekBarChangeMediaListener(mControllerPresenter));

        return rootView;
    }

    @OnClick(R.id.arrow_up)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.arrow_up:
                Intent intent = new Intent(getActivity(), FullScreenPlayerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                // extra params for playing
                if (mControllerPresenter.getLastPlaybackState() != null) {
                    intent.putExtra("progress", mControllerPresenter.getLastPlaybackState().getPosition());
                    intent.putExtra("state", mControllerPresenter.getLastPlaybackState().getState());
                }

                MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
                if(controller==null){
                    return;
                }
                MediaMetadataCompat metadata = controller.getMetadata();
                if (metadata != null) {
                    intent.putExtra(DashboardActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION,
                            metadata.getDescription());
                }
                startActivityForResult(intent, FullScreenPlayerActivity.REQUEST_CODE);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mControllerPresenter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mControllerPresenter.onStop();
    }

    public void setExtraInfo(String extraInfo) {
        if (extraInfo == null) {
            mExtraInfo.setVisibility(View.GONE);
        } else {
            mExtraInfo.setText(extraInfo);
            mExtraInfo.setVisibility(View.VISIBLE);
        }
    }

    private final View.OnClickListener mButtonListener = v -> {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        PlaybackStateCompat stateObj = controller.getPlaybackState();
        final int state = stateObj == null ?
                PlaybackStateCompat.STATE_NONE : stateObj.getState();
        LogHelper.d(TAG, "Button pressed, in state " + state);
        switch (v.getId()) {
            case R.id.play_pause:
                LogHelper.d(TAG, "Play button pressed, in state " + state);
                if (state == PlaybackStateCompat.STATE_PAUSED ||
                        state == PlaybackStateCompat.STATE_STOPPED ||
                        state == PlaybackStateCompat.STATE_NONE) {
                    playMedia();
                } else if (state == PlaybackStateCompat.STATE_PLAYING ||
                        state == PlaybackStateCompat.STATE_BUFFERING ||
                        state == PlaybackStateCompat.STATE_CONNECTING) {
                    pauseMedia();
                }
                break;
            case R.id.next:
                nextMedia();
                break;
            case R.id.prev:
                prevMedia();
                break;
        }
    };

    private void nextMedia() {
        mControllerPresenter.nextMedia();
    }

    private void prevMedia() {
        mControllerPresenter.prevMedia();
    }

    private void playMedia() {
        mControllerPresenter.playMedia();
    }

    private void pauseMedia() {
        mControllerPresenter.pauseMedia();
    }

    @Override
    public void onProgressUpdated(int progress) {
        mHandler.post(() -> mSeekbar.setProgress(progress));
    }

    @Override
    public void onError(String message) {
        mHandler.post(() -> Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadataCompat,MediaControllerCompat controller) {
        mPlaybackControlsPresenter.updateByMediaMetadata(metadataCompat);
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {

        if(getActivity()==null){
            return;
        }

        mHandler.post(() -> {
            boolean enablePlay = false;
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PAUSED:
                case PlaybackStateCompat.STATE_STOPPED:
                    enablePlay = true;
                    break;
                case PlaybackStateCompat.STATE_ERROR:
                    LogHelper.e(TAG, "error playbackstate: ", state.getErrorMessage());
                    Toast.makeText(getActivity(), state.getErrorMessage(), Toast.LENGTH_LONG).show();
                    break;
            }

            if (enablePlay) {
                mPlayPause.setImageDrawable(
                        ContextCompat.getDrawable(getActivity(), R.drawable.uamp_ic_play_arrow_white_48dp));
            } else {
                mPlayPause.setImageDrawable(
                        ContextCompat.getDrawable(getActivity(), R.drawable.uamp_ic_pause_white_48dp));
            }

            // TODO
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
            String extraInfo = null;
            if (controller != null && controller.getExtras() != null) {
                String castName = controller.getExtras().getString(MusicService.EXTRA_CONNECTED_CAST);
                if (castName != null) {
                    extraInfo = getResources().getString(R.string.casting_to_device, castName);
                }
            }
            setExtraInfo(extraInfo);
        });
    }

    @Override
    public void onControllerStatusChanged(boolean available) {

    }

    @Override
    public void setTitle(String title) {
        mHandler.post(() -> mTitle.setText(title));
    }

    @Override
    public void setSubtitle(String value) {
        mHandler.post(() -> mSubtitle.setText(value));
    }

    @Override
    public void setMaxDuration(int duration) {
        mHandler.post(() -> mSeekbar.setMax(duration));
    }

    @Override
    public void setAlbumPicture(String path) {
        mHandler.post(() -> img_album_pic.setImageURI(Uri.fromFile(new File(path))));
    }
}
