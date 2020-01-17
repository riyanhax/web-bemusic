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
package com.digiapp.jilmusic.fullScreenPlayer.views;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.digiapp.jilmusic.R;
import com.digiapp.jilmusic.beans.SelectionList;
import com.digiapp.jilmusic.blurredview.BlurredView;
import com.digiapp.jilmusic.dashboardView.views.DashboardActivity;
import com.digiapp.jilmusic.fullScreenPlayer.models.EventListenerWrapper;
import com.digiapp.jilmusic.fullScreenPlayer.models.PLAY_TYPE;
import com.digiapp.jilmusic.fullScreenPlayer.presenters.FullScreenPlayerPresenter;
import com.digiapp.jilmusic.ui.ActionBarCastActivity;
import com.digiapp.jilmusic.utils.Core;
import com.digiapp.jilmusic.utils.LogHelper;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.digiapp.jilmusic.dashboardView.views.DashboardActivity.EXTRA_START_FULLSCREEN;

/**
 * A full screen player that shows the current playing music with a background image
 * depicting the album art. The activity also has controls to seek/pause/play the audio.
 */
public class FullScreenPlayerActivity extends ActionBarCastActivity implements FullScreenPlayerPresenter.View {

    private static final String TAG = LogHelper.makeLogTag(FullScreenPlayerActivity.class);

    private Drawable mPauseDrawable;
    private Drawable mPlayDrawable;

    @BindView(R.id.prev)
    ImageView mSkipPrev;
    @BindView(R.id.next)
    ImageView mSkipNext;
    @BindView(R.id.play_pause)
    ImageView mPlayPause;
    @BindView(R.id.shuffle)
    ImageView shuffle;
    @BindView(R.id.repeat)
    ImageView repeat;
    @BindView(R.id.startText)
    TextView mStart;
    @BindView(R.id.endText)
    TextView mEnd;
    @BindView(R.id.seekBar1)
    SeekBar mSeekbar;
    @BindView(R.id.line1)
    TextView mLine1;
    @BindView(R.id.line2)
    TextView mLine2;
    @BindView(R.id.line3)
    TextView mLine3;
    @BindView(R.id.progressBar1)
    ProgressBar mLoading;
    @BindView(R.id.controllers)
    View mControllers;

    @BindView(R.id.background_image)
    SimpleDraweeView mBackgroundImage;
    @BindView(R.id.player_view)
    PlayerView videoView;
    private SimpleExoPlayer player;

    @BindView(R.id.img_album_pic)
    BlurredView img_album_pic;
    @BindView(R.id.btnAddSelection)
    ImageView btnAddSelection;
    @BindView(R.id.btnAddFavorite)
    ImageView btnAddFavorite;
    @BindView(R.id.switchControls)
    View switchControls;
    @BindView(R.id.switchMedia)
    ImageView switchMedia;

    FullScreenPlayerPresenter mPresenter;
    Unbinder mUnbinder;
    private long initProgress = 0;
    private int initState = PlaybackStateCompat.STATE_PLAYING;
    private boolean mCurrentlyPlaying = false;
    public static final int REQUEST_CODE = 338;
    Handler mHandler = new Handler(Looper.getMainLooper());

    private final String STATE_RESUME_WINDOW = "resumeWindow";
    private final String STATE_RESUME_POSITION = "resumePosition";
    private final String STATE_PLAYER_FULLSCREEN = "playerFullscreen";

    @Override
    public void updateDuration(int maxDuration) {
        new Handler(Looper.getMainLooper()).post(() -> {
            mSeekbar.setMax(maxDuration);
            mEnd.setText(DateUtils.formatElapsedTime(maxDuration / 1000));
        });
    }

    @Override
    public void setBackgroundImage(Bitmap bitmap) {


        mBackgroundImage.setImageBitmap(bitmap);
    }

    @Override
    public void showToast(final String message) {
        mHandler.post(() -> Toast.makeText(FullScreenPlayerActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void showSnackBar(String message) {
        mHandler.post(() -> Core.showSnackBar(FullScreenPlayerActivity.this, message));
    }

    @Override
    public int getVideoState() {
        return player.getPlaybackState();// videoView.currentState;
    }

    @Override
    public boolean isVideoPlaying() {
        return isCurrentPlay();
    }

    private void initExoPlayer() {
        videoView.setVisibility(VISIBLE);

        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        DefaultAllocator allocator = new DefaultAllocator(true, C.DEFAULT_VIDEO_BUFFER_SIZE);
        DefaultLoadControl loadControl = new DefaultLoadControl(allocator, 5000, 60000, 5000, 5000, -1, true);

        RenderersFactory renderersFactory = new DefaultRenderersFactory(this);

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "ExoPlayer"), bandwidthMeter);
        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);

        Handler mHandler = new Handler();
        ExtractorMediaSource.Factory mediaSourceFactory =
                new ExtractorMediaSource.Factory(dataSourceFactory);
        MediaSource videoSource = mediaSourceFactory
                .createMediaSource(Uri.parse(mPresenter.getCurrentTrack().localPath), mHandler, null);

        LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);

        videoView.setUseController(true);
        videoView.requestFocus();
        videoView.setPlayer(player);

        player.prepare(loopingSource);
        player.setPlayWhenReady(true);
        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        player.addListener(new EventListenerWrapper(this));

        initFullscreenDialog();
        initFullscreenButton();
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("lifecycle", "Onresume");

        if (getIntent() != null
                && getIntent().getExtras() != null) {

            Bundle extras = getIntent().getExtras();
            // from background we always moving to audio
            boolean fullScreen = extras.getBoolean(EXTRA_START_FULLSCREEN);
            if (fullScreen) {
                mPresenter.setPlayingType(PLAY_TYPE.AUDIO);
                initProgress = 0;
            }
        }

        if (videoView.getVisibility() == VISIBLE
                && !isCurrentPlay()) {
            Log.d("init_debug", "Onresume");
            if (player != null) {
                player.setPlayWhenReady(true);
            }
        }

        if (player != null) {
            // init fullscreen button
            initFullscreenDialog();
            initFullscreenButton();

            if (mExoPlayerFullscreen) {
                ((ViewGroup) videoView.getParent()).removeView(videoView);
                mFullScreenDialog.addContentView(videoView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_skrink));
                mFullScreenDialog.show();
            }
        }
    }

    Dialog mFullScreenDialog;
    boolean mExoPlayerFullscreen;
    private FrameLayout mFullScreenButton;
    private ImageView mFullScreenIcon;
    private int mResumeWindow;
    private long mResumePosition;

    private void initFullscreenDialog() {
        if (mFullScreenDialog == null) {
            mFullScreenDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
                public void onBackPressed() {
                    if (mExoPlayerFullscreen) {
                        closeFullscreenDialog();
                    }
                    super.onBackPressed();
                }
            };
        }

    }

    private void openFullscreenDialog() {
        ((ViewGroup) videoView.getParent()).removeView(videoView);
        mFullScreenDialog.addContentView(videoView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_skrink));
        mExoPlayerFullscreen = true;
        mFullScreenDialog.show();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void closeFullscreenDialog() {
        ((ViewGroup) videoView.getParent()).removeView(videoView);
        ((FrameLayout) findViewById(R.id.main_media_frame)).addView(videoView);
        mExoPlayerFullscreen = false;
        mFullScreenDialog.dismiss();
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_expand));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    ImageButton exo_next, exo_prev;

    private void initFullscreenButton() {

        PlaybackControlView controlView = videoView.findViewById(R.id.exo_controller);
        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
        mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mExoPlayerFullscreen) {
                    openFullscreenDialog();
                } else {
                    closeFullscreenDialog();
                }
            }
        });

        // next prev buttons
        exo_next = controlView.findViewById(R.id.exo_next);
        exo_next.setOnClickListener(v -> {

            mPresenter.onSkipNext();
        });
        exo_prev = controlView.findViewById(R.id.exo_prev);
        exo_prev.setOnClickListener(v -> mPresenter.onSkipPrev());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_RESUME_WINDOW, mResumeWindow);
        outState.putLong(STATE_RESUME_POSITION, mResumePosition);
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, mExoPlayerFullscreen);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null) {
            setIntent(intent);
        }
    }

    @Override
    public void onBackPressed() {

        mPresenter.onBackPressed();
        if (mPresenter.isVideo()) {

            long progress = player.getContentPosition();//videoView.getCurrentPositionWhenPlaying();

            Intent intent = getIntent();
            intent.putExtra("progress", progress);
            intent.putExtra("media", mPresenter.getCurrentTrack());
            intent.putExtra("state", 3);//videoView.currentState);

            pauseVideo();
            onStop();

            player.stop(true);
            setResult(-1, intent);
            finish();
        } else {
            onStop();
            super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.onPause(isCurrentPlay());

        if (videoView != null && videoView.getPlayer() != null) {
            mResumeWindow = videoView.getPlayer().getCurrentWindowIndex();
            mResumePosition = Math.max(0, videoView.getPlayer().getContentPosition());

            videoView.getPlayer().release();
        }

        if (mFullScreenDialog != null) {
            mFullScreenDialog.dismiss();
        }
    }

    public boolean isCurrentPlay() {
        if (videoView == null
                || player == null) {
            return false;
        }
        return player.getPlaybackState() == Player.STATE_READY && player.getPlayWhenReady();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_player);
        initializeToolbar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.down_arrow_icon);
            getSupportActionBar().setBackgroundDrawable(null);
        }

        mUnbinder = ButterKnife.bind(this);
        mPresenter = new FullScreenPlayerPresenter(this);

        mPauseDrawable = ContextCompat.getDrawable(this, R.drawable.uamp_ic_pause_white_48dp);
        mPlayDrawable = ContextCompat.getDrawable(this, R.drawable.uamp_ic_play_arrow_white_48dp);

        btnAddFavorite.setOnClickListener(v -> mPresenter.addTrackToFavorite());
        btnAddSelection.setOnClickListener(v -> {
            Core.getFullSelectionDialog(FullScreenPlayerActivity.this, msg -> {
                if (msg.obj != null) {
                    mPresenter.addTrackToSelection((SelectionList) msg.obj);
                }
                return true;
            }
            ).show();

        });

        repeat.setOnClickListener(v -> {
            mPresenter.onRepeat();
        });

        shuffle.setOnClickListener(v -> {
            mPresenter.onShuffle();
        });

        mSkipNext.setOnClickListener(v -> {
            mPresenter.onSkipNext();
        });

        mSkipPrev.setOnClickListener(v -> {
            mPresenter.onSkipPrev();
        });

        mPlayPause.setOnClickListener(v -> {
            mPresenter.onPlayPause();
        });

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mStart.setText(DateUtils.formatElapsedTime(progress / 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mPresenter.stopSeekbarUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPresenter.onStopTrackingTouch(seekBar.getProgress());
            }
        });

        switchMedia.setOnClickListener(v -> mPresenter.switchMediaAction());
        mControllers.setVisibility(VISIBLE);

        // Only update from the intent if we are not recreating from a config change:
        if (savedInstanceState == null) {
            updateFromParams(getIntent());
        }

        if (getIntent() != null
                && getIntent().getExtras() != null) {

            Bundle extras = getIntent().getExtras();
            initProgress = extras.getLong("progress", 0);
            initState = extras.getInt("state", PlaybackStateCompat.STATE_PLAYING);

            boolean fullScreen = extras.getBoolean(EXTRA_START_FULLSCREEN);
            if (fullScreen) {
                mPresenter.setPlayingType(PLAY_TYPE.AUDIO);
                initProgress = 0;
            }

            Log.d("lifecycle", "onCreate");
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (savedInstanceState != null) {
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW);
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION);
            mExoPlayerFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN);
        }
    }

    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        System.out.println("on config change method called.");

        if (Configuration.ORIENTATION_LANDSCAPE == newConfig.orientation) {
            //Portatrate to landscape...
            System.out.println("on config change method called portraite mode.");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (Configuration.ORIENTATION_PORTRAIT == newConfig.orientation) {
            //Landscape to portraite....
            System.out.println("on config change method called landscape mode.");
        }
    }*/

    @Override
    public MediaControllerCompat getController() {
        return MediaControllerCompat.getMediaController(FullScreenPlayerActivity.this);
    }

    @Override
    public void playVideo() {
        if(player.getPlaybackState()==Player.STATE_READY
                && !player.getPlayWhenReady()) {
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }

        mPlayPause.setImageDrawable(mPauseDrawable);
    }

    @Override
    public void updateFavoriteStatus(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btnAddFavorite.setImageDrawable(drawable);
        }
    }

    @Override
    public void resetProgress(String localPath) {
        initProgress = 0; // reset this
        if (mPresenter.isVideo()) {
            player.stop(true);
        }
    }

    @Override
    public MediaControllerCompat.TransportControls getTransportControls() {
        MediaControllerCompat.TransportControls controls = MediaControllerCompat.getMediaController(this).getTransportControls();
        return controls;
    }

    @Override
    public void setMediaController(MediaControllerCompat mediaControllerCompat) {
        MediaControllerCompat.setMediaController(FullScreenPlayerActivity.this, mediaControllerCompat);
    }

    @Override
    public void setStatePlaying(PlaybackStateCompat state, boolean isVideo) {
        mLoading.setVisibility(INVISIBLE);
        mPlayPause.setVisibility(VISIBLE);
        mControllers.setVisibility(VISIBLE);

        if (!isVideo) {
            mPlayPause.setImageDrawable(mPauseDrawable);
        }
    }

    @Override
    public void setStatePausing(PlaybackStateCompat state, boolean isVideo) {
        mControllers.setVisibility(VISIBLE);
        mLoading.setVisibility(INVISIBLE);
        mPlayPause.setVisibility(VISIBLE);

        if (!isVideo) {
            mPlayPause.setImageDrawable(mPlayDrawable);
        }
    }

    @Override
    public void setStateNone(PlaybackStateCompat state, boolean isVideo) {
        mLoading.setVisibility(INVISIBLE);
        mPlayPause.setVisibility(VISIBLE);

        if (!isVideo) {
            mPlayPause.setImageDrawable(mPlayDrawable);
        }
    }

    @Override
    public void setStateBuffering(PlaybackStateCompat state, boolean isVideo) {
        mPlayPause.setVisibility(INVISIBLE);
        mLoading.setVisibility(VISIBLE);
        mLine3.setText(R.string.loading);
    }

    @Override
    public void updateSkipNextVisibility(PlaybackStateCompat state) {
        mSkipNext.setVisibility((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) == 0
                ? INVISIBLE : VISIBLE);
        mSkipPrev.setVisibility((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) == 0
                ? INVISIBLE : VISIBLE);
    }

    @Override
    public void switchToAudio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            switchMedia.setImageDrawable(this.getDrawable(R.drawable.video_record));
        }

        mCurrentlyPlaying = isCurrentPlay();
        initProgress = videoView.getPlayer().getCurrentPosition();

        updateFromParams(getIntent());
    }

    @Override
    public void switchToVideo(PlaybackStateCompat stateCompat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            switchMedia.setImageDrawable(this.getDrawable(R.drawable.music_record));
        }

        mCurrentlyPlaying = false;
        if (stateCompat != null) {
            initProgress = stateCompat.getPosition();
        }

        updateFromParams(getIntent());
    }

    @Override
    public void videoSeekTo(int progress) {
        if (player != null) {
            player.seekTo(progress);
        }
    }

    @Override
    public void audioSeekTo(int progress) {
        MediaControllerCompat.getMediaController(FullScreenPlayerActivity.this).getTransportControls().seekTo(progress);
    }

    @Override
    public void pauseVideo() {

        initProgress = videoView.getPlayer().getCurrentPosition(); // saving this locally

        if(player.getPlaybackState()==Player.STATE_READY
                && player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }

        mPlayPause.setImageDrawable(mPlayDrawable);
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mPresenter.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    @Override
    public void processVideoIfNeeded(MediaDescriptionCompat description) {

        if (player != null) {
            player.stop(true);
            player.release();
        }

        if (mPresenter.isVideo()) { // that means it is video
            mBackgroundImage.setVisibility(View.GONE);
            initExoPlayer();

            // changing view for progress
            Log.d("init_debug", "initProgress: " + initProgress);
            mPlayPause.setImageDrawable(mPlayDrawable); // by default paused
            mSeekbar.setProgress((int) initProgress);
            mStart.setText(DateUtils.formatElapsedTime(initProgress / 1000));

            if (initState == PlaybackStateCompat.STATE_PLAYING) {
                mPresenter.prepareAndStartVideo();
            }

        } else {

            if (mExoPlayerFullscreen) {
                closeFullscreenDialog();
            }

            mBackgroundImage.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);

            if (player != null) {
                player.release();
            }

            MediaControllerCompat mediaControllerCompat = MediaControllerCompat.getMediaController(FullScreenPlayerActivity.this);
            if (mediaControllerCompat != null) {

                int mediaId = Integer.parseInt(mediaControllerCompat.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
                MediaControllerCompat.TransportControls controls = mediaControllerCompat.getTransportControls();

                mCurrentlyPlaying = initState == PlaybackStateCompat.STATE_PLAYING;
                if (mCurrentlyPlaying) {
                    controls.play();
                } else {
                    controls.stop();
                }

                if (initProgress > 0) {
                    controls.seekTo(initProgress);
                }

                mPresenter.scheduleSeekbarUpdate();
            }
        }

        // special rules for switch controls
        if (mPresenter.isVideo() || mPresenter.getCurrentTrack().isVideo) {
            switchControls.setVisibility(VISIBLE);
        } else {
            switchControls.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateNameAlbum(String name, String album, String pathToPoster) {
        try {
            mLine1.setText(name);
            mLine2.setText(album);
            img_album_pic.setBlurredImg(Core.getBitmapFromPath(pathToPoster));
            img_album_pic.setBlurredLevel(50);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void updateCastText(String status) {
        mLine3.setText(status);
    }

    @Override
    public void updateProgress(PlaybackStateCompat stateCompat, boolean isVideo) {
        long currentPosition = 0;
        if (isVideo) {
            currentPosition = player.getCurrentPosition();
            try {
                mEnd.setText(DateUtils.formatElapsedTime(player.getDuration() / 1000));
                mSeekbar.setProgress((int) currentPosition);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else {
            if (stateCompat == null) {
                return;
            }
            currentPosition = stateCompat.getPosition();
            if (stateCompat.getState() == PlaybackStateCompat.STATE_PLAYING) {
                long timeDelta = SystemClock.elapsedRealtime() -
                        stateCompat.getLastPositionUpdateTime();
                currentPosition += (int) timeDelta * stateCompat.getPlaybackSpeed();
            }

        }

        if(mSeekbar!=null) {
            mSeekbar.setProgress((int) currentPosition);
        }
    }

    private void updateFromParams(Intent intent) {
        if (intent != null) {

            MediaDescriptionCompat description;

            MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(FullScreenPlayerActivity.this);
            if (controllerCompat == null) {
                description = intent.getParcelableExtra(DashboardActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION);
            } else {
                description = controllerCompat.getMetadata().getDescription();
                if (description == null) {
                    description = intent.getParcelableExtra(DashboardActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION);
                }
            }

            if (description != null) {
                mPresenter.updateMediaDescription(description);
            }
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
