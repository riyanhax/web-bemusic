package com.digiapp.jilmusic.dashboardView.views;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsMessage;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.digiapp.jilmusic.R;
import com.digiapp.jilmusic.api.beans.UserInfo;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.components.EventType;
import com.digiapp.jilmusic.dashboardView.models.YouTubeLink;
import com.digiapp.jilmusic.dashboardView.presenters.DashboardCallbackPresenter;
import com.digiapp.jilmusic.dashboardView.presenters.DashboardPresenter;
import com.digiapp.jilmusic.offlineView.views.MusicPlayerCompatFragment;
import com.digiapp.jilmusic.fragments.NoInternetFragment;
import com.digiapp.jilmusic.fragments.WebViewFragment;
import com.digiapp.jilmusic.onlinePlayerView.views.XWalkWebViewFragment;
import com.digiapp.jilmusic.notifications.WebViewNotificationManager;
import com.digiapp.jilmusic.fullScreenPlayer.views.FullScreenPlayerActivity;
import com.digiapp.jilmusic.utils.Core;
import com.digiapp.jilmusic.utils.LogHelper;
import com.digiapp.jilmusic.utils.NetworkHelper;
import com.digiapp.jilmusic.utils.PreferencesHelper;
import com.facebook.drawee.view.SimpleDraweeView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xwalk.core.XWalkActivityDelegate;

import java.util.ArrayList;

import at.huber.youtubeExtractor.YtFile;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


@SuppressLint("NewApi")
public class DashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        DashboardPresenter.View, View.OnClickListener {

    private static final String TAG = LogHelper.makeLogTag(DashboardActivity.class);
    public static final String SAVED_MEDIA_ID = "com.example.android.uamp.MEDIA_ID";
    public static final String FRAGMENT_TAG = "uamp_list_container";
    public static final String EXTRA_START_FULLSCREEN =
            "com.example.android.uamp.EXTRA_START_FULLSCREEN";
    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION =
            "com.example.android.uamp.CURRENT_MEDIA_DESCRIPTION";

    private XWalkWebViewFragment webViewFragment;
    private MusicPlayerCompatFragment musicPlayerFragment;
    private NoInternetFragment noInternetFragment;

    private Bundle mVoiceSearchParams;

    @BindView(R.id.profile_image)
    SimpleDraweeView profile_image;
    @BindView(R.id.profile_name)
    TextView profile_name;
    @BindView(R.id.profile_sub)
    TextView profile_sub;
    @BindView(R.id.action_exit)
    ImageButton action_exit;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    private NavigationView mNavigationView;
    private ProgressDialog progressDialog;

    Handler mHandler = new Handler(Looper.getMainLooper());
    DashboardPresenter mPresenter;
    DashboardCallbackPresenter mCallbackPresenter;
    XWalkActivityDelegate delegate;

    ActionBarDrawerToggle mDrawerToggle;

    // @Override
    protected void onXWalkReady() {
        mainFragment();
        mPresenter.checkUserInfo();
        webViewFragment.onReady();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.app_name));

        mDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        toolbar.setNavigationOnClickListener(this);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getBackground().setAlpha(150);

        initializeFromParams(savedInstanceState, getIntent());

        webViewFragment = (XWalkWebViewFragment) Fragment.instantiate(this, XWalkWebViewFragment.class.getName());
        musicPlayerFragment = getMusicPlayerFragment();
        noInternetFragment = (NoInternetFragment) Fragment.instantiate(this, NoInternetFragment.class.getName());

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .add(R.id.frameLayout, webViewFragment);

        if (musicPlayerFragment == null) {
            musicPlayerFragment = (MusicPlayerCompatFragment) Fragment.instantiate(this, MusicPlayerCompatFragment.class.getName());
            transaction.add(R.id.frameLayout, musicPlayerFragment, MusicPlayerCompatFragment.MUSIC_PLAYER_FRAGMENT_TAG);
        }

        transaction.add(R.id.frameLayout, noInternetFragment)
                .hide(webViewFragment)
                .hide(musicPlayerFragment)
                .hide(noInternetFragment)
                .commit();
        showWebFragment();

        boolean offline = getIntent().getBooleanExtra("offline", false);
        mNavigationView.getMenu().getItem(0).setChecked(true);

        Intent msgIntent = new Intent(this, WebViewNotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(msgIntent);
        } else {
            startService(msgIntent);
        }

        if (offline) {
            MaterialDialog.Builder builder = Core.getDialogBuilder(this);
            builder.title(R.string.no_internet)
                    .content(R.string.redirected_offline)
                    .cancelable(false).positiveText("OK")
                    .negativeText(null)
                    .onPositive((dialog, which) -> offlineFragment()).build().show();
        }

        registerLockScreenReceiver();
        EventBus.getDefault().register(this);

        startFullScreenActivityIfNeeded(getIntent());

        mPresenter = new DashboardPresenter(this);
        mCallbackPresenter = new DashboardCallbackPresenter(mPresenter);
        webViewFragment.setCallbackListener(mCallbackPresenter);

        delegate = new XWalkActivityDelegate(this, new OnXWalkFail(), new OnXWalkReady());
    }

    /**
     * To be semantically or contextually correct, maybe change the name
     * and signature of this function to something like:
     *
     * private void showBackButton(boolean show)
     * Just a suggestion.
     */
    public void showBackButton(boolean enable) {

        // To keep states of ActionBar and ActionBarDrawerToggle synchronized,
        // when you enable on one, you disable on the other.
        // And as you may notice, the order for this operation is disable first, then enable - VERY VERY IMPORTANT.
        if(enable) {
            //You may not want to open the drawer on swipe from the left in this case
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            // Remove hamburger
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            // Show back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // when DrawerToggle is disabled i.e. setDrawerIndicatorEnabled(false), navigation icon
            // clicks are disabled i.e. the UP button will not work.
            // We need to add a listener, as in below, so DrawerToggle will forward
            // click events to this listener.
            /*if (!mToolBarNavigationListenerIsRegistered) {
                mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Doesn't have to be onBackPressed
                        onBackPressed();
                    }
                });

                mToolBarNavigationListenerIsRegistered = true;
            }*/

        } else {
            //You must regain the power of swipe for the drawer.
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

            // Remove back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            // Show hamburger
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            // Remove the/any drawer toggle listener
            mDrawerToggle.setToolbarNavigationClickListener(null);
            //mToolBarNavigationListenerIsRegistered = false;

            new Handler(Looper.getMainLooper()).post(() -> EventBus.getDefault().post(EventType.BACK_PRESSED));

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        delegate.onResume();
    }

    @Override
    public void onClick(View v) {
        if (drawer.isDrawerOpen(Gravity.RIGHT)) {
            drawer.closeDrawer(Gravity.RIGHT);
        } else {
            if(!mDrawerToggle.isDrawerIndicatorEnabled()){
                // we need to back stack fragment
                onBackPressed();
               // showBackButton(false);
            }else {
                drawer.openDrawer(Gravity.RIGHT);
            }
        }
    }

    class OnXWalkFail implements Runnable {
        @Override
        public void run() {
            showToast("OnXWalkFail");
        }
    }

    class OnXWalkReady implements Runnable {
        @Override
        public void run() {
            mHandler.post(() -> onXWalkReady());
        }
    }

    private MusicPlayerCompatFragment getMusicPlayerFragment() {
        return (MusicPlayerCompatFragment) getSupportFragmentManager()
                .findFragmentByTag(MusicPlayerCompatFragment.MUSIC_PLAYER_FRAGMENT_TAG);
    }

    public NavigationView getNavigationView() {
        return mNavigationView;
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().postSticky(Intent.ACTION_SCREEN_OFF);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object[] pdus) {
        SmsMessage[] msgs = new SmsMessage[pdus.length];
        for (int i = 0; i < msgs.length; i++) {
            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            String msg_from = msgs[i].getOriginatingAddress();
            String msgBody = msgs[i].getMessageBody();

        }
    }

    private void registerLockScreenReceiver() {
        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction(Intent.ACTION_SCREEN_ON);
        theFilter.addAction(Intent.ACTION_SCREEN_OFF);
        theFilter.addAction(Intent.ACTION_USER_PRESENT);

        BroadcastReceiver screenOnOffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String strAction = intent.getAction();

                KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                if (strAction.equals(Intent.ACTION_USER_PRESENT) || strAction.equals(Intent.ACTION_SCREEN_OFF) || strAction.equals(Intent.ACTION_SCREEN_ON))
                    if (myKM.inKeyguardRestrictedInputMode()) {
                        EventBus.getDefault().postSticky(Intent.ACTION_SCREEN_OFF);
                    } else {
                        EventBus.getDefault().postSticky(Intent.ACTION_SCREEN_ON);
                    }
            }
        };

        getApplicationContext().registerReceiver(screenOnOffReceiver, theFilter);
    }

    @OnClick({R.id.profile_image, R.id.profile_name, R.id.profile_sub, R.id.action_exit})
    public void onProfileClick(View view) {
        openAccountFragment();
    }

    @Override
    public void showWebFragment() {
        mHandler.post(() -> getSupportFragmentManager().beginTransaction()
                .show(webViewFragment)
                .hide(musicPlayerFragment)
                .hide(noInternetFragment)
                .commit());
    }

    @Override
    public void showOfflineFragment() {
        mHandler.post(() -> getSupportFragmentManager().beginTransaction()
                .show(musicPlayerFragment)
                .hide(webViewFragment)
                .hide(noInternetFragment)
                .commit());
    }

    @Override
    public void showNoInternetFragment() {
        mHandler.post(() -> getSupportFragmentManager().beginTransaction()
                .hide(webViewFragment)
                .hide(musicPlayerFragment)
                .show(noInternetFragment)
                .commit());
    }

    @Override
    public void hideAll() {
        mHandler.post(() -> getSupportFragmentManager().beginTransaction()
                .hide(webViewFragment)
                .hide(musicPlayerFragment)
                .hide(noInternetFragment)
                .commit());
    }

    @Override
    public void updateUserUI(UserInfo userInfo) {
        mHandler.post(() -> {
            if (userInfo != null) {
                profile_image.setImageURI(userInfo.avatar);
                profile_name.setText(userInfo.display_name);
                profile_sub.setText(userInfo.subscription);
            } else {
                profile_image.setImageDrawable(getResources().getDrawable(R.drawable.profile_placeholder));
                profile_name.setText(getString(R.string.text_profile_name));
                profile_sub.setText("-");
            }
        });
    }

    @Override
    public void showDownloadDialog(MusicTrack musicTrack) {
        mHandler.post(() -> {
            final MaterialDialog.Builder builder = Core.getDialogBuilder(DashboardActivity.this);
            builder.title(R.string.text_download);
            builder.content(R.string.text_video_audio).cancelable(false)
                    .positiveText(R.string.text_audio)
                    .negativeText(R.string.text_video)
                    .onPositive((dialog, which) -> mPresenter.downloadTrack(musicTrack))
                    .onNegative((dialog, which) -> mPresenter.downloadYoutube(musicTrack));
            builder.build().show();
        });
    }

    @Override
    public void showToast(String message) {
        mHandler.post(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void setTitle(CharSequence title) {
        TextView toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(title);
        super.setTitle("");
    }

    private void startFullScreenActivityIfNeeded(Intent intent) {
        if (intent != null && intent.getBooleanExtra(EXTRA_START_FULLSCREEN, false)) {
            MediaDescriptionCompat description =
                    intent.getParcelableExtra(EXTRA_CURRENT_MEDIA_DESCRIPTION);

            Intent fullScreenIntent = new Intent(this, FullScreenPlayerActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(EXTRA_CURRENT_MEDIA_DESCRIPTION, description)
                    .putExtra(EXTRA_START_FULLSCREEN, true);
            startActivity(fullScreenIntent);
        }
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawers();

        if (drawer.isDrawerOpen(Gravity.RIGHT)) {
            drawer.closeDrawer(Gravity.RIGHT);
        } else {

            if(!mDrawerToggle.isDrawerIndicatorEnabled()){
                showBackButton(false);
                setTitle(getString(R.string.app_name));
                return;
            }

            // Otherwise, it may return to the previous fragment stack
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_switch_theme) {
            int currentModeNight = AppCompatDelegate.getDefaultNightMode();
            if (currentModeNight == AppCompatDelegate.MODE_NIGHT_YES) {
                currentModeNight = AppCompatDelegate.MODE_NIGHT_NO;
            } else {
                currentModeNight = AppCompatDelegate.MODE_NIGHT_YES;
            }

            PreferencesHelper.setModeNight(this, currentModeNight);
            AppCompatDelegate.setDefaultNightMode(currentModeNight);
            getDelegate().setLocalNightMode(currentModeNight);
            // webViewFragment.switchTheme(currentModeNight);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.nav_main:
                mainFragment(WebViewFragment.GENRES_URL);
                break;
            case R.id.nav_top:
                mainFragment(WebViewFragment.TOP_50_URL);
                break;
            case R.id.nav_search:
                mainFragment(WebViewFragment.SEARCH_URL);
                break;
            case R.id.nav_premium:
                openSubscriptionFragment();
                break;
            case R.id.nav_offline:
                if (Core.isUserLogged(this)
                        && Core.isUserActive(this)) {
                    offlineFragment();
                    setTitle(getString(R.string.app_name_offline_mode));
                } else {
                    openAccountFragment();
                }
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.END);

        return true;
    }

    private void openAccountFragment() {

        drawer.closeDrawers();

        mainFragment(WebViewFragment.ACCOUNT_URL);
        setTitle("ACCOUNT");
    }

    private void openSubscriptionFragment() {

        drawer.closeDrawers();

        if (Core.isUserLogged(this)) {
            mainFragment(WebViewFragment.SUBSCRIPTION_URL);
            setTitle("ACCOUNT");
        } else {
            openAccountFragment();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogHelper.d(TAG, "onNewIntent, intent=" + intent);
        initializeFromParams(null, intent);
        startFullScreenActivityIfNeeded(intent);
    }

    protected void initializeFromParams(Bundle savedInstanceState, Intent intent) {
        String mediaId = null;
        // check if we were started from a "Play XYZ" voice search. If so, we save the extras
        // (which contain the query details) in a parameter, so we can reuse it later, when the
        // MediaSession is connected.
        if (intent.getAction() != null
                && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            mVoiceSearchParams = intent.getExtras();
            LogHelper.d(TAG, "Starting from voice search query=",
                    mVoiceSearchParams.getString(SearchManager.QUERY));
        } else {
            if (savedInstanceState != null) {
                // If there is a saved media ID, use it
                mediaId = savedInstanceState.getString(SAVED_MEDIA_ID);
            }
        }
        navigateToBrowser(mediaId);
    }

    private void navigateToBrowser(String mediaId) {
        LogHelper.d(TAG, "navigateToBrowser, mediaId=" + mediaId);
    }

    private void mainFragment() {
        if (webViewFragment == null) {
            webViewFragment = new XWalkWebViewFragment();
            if (mCallbackPresenter != null) {
                webViewFragment.setCallbackListener(mCallbackPresenter);
            }
        }

        if (NetworkHelper.isOnline(this)) {
            showWebFragment();
        } else {
            showNoInternetFragment();
        }
    }

    private void offlineFragment() {
        if (musicPlayerFragment == null) {
            musicPlayerFragment = new MusicPlayerCompatFragment();
        }

        /*
        TODO
        try {
            musicPlayerFragment.refresh();
        } catch (Exception ex) {
            Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }*/

        showOfflineFragment();
    }

    private void mainFragment(String page) {
        if (webViewFragment == null) {
            webViewFragment = new XWalkWebViewFragment();
            if (mCallbackPresenter != null) {
                webViewFragment.setCallbackListener(mCallbackPresenter);
            }
        }

        webViewFragment.setCurrentPage(page);
        try {
            webViewFragment.refresh();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (NetworkHelper.isOnline(this)) {
            showWebFragment();
        } else {
            showNoInternetFragment();
        }
    }

    public void showSelectorDialog(SparseArray<YtFile> ytFiles, MusicTrack musicTrack) {

        ArrayList<YouTubeLink> arrayList = new ArrayList<>();
        for (int i = 0; i < ytFiles.size(); i++) {
            YtFile ytFile = ytFiles.valueAt(i);

            if (ytFile.getFormat().getExt().equalsIgnoreCase("3gp")
                    || ytFile.getFormat().getExt().equalsIgnoreCase("m4a")) {
                arrayList.add(new YouTubeLink(ytFile));
            }
        }

        MaterialDialog.Builder builder = Core.getDialogBuilder(this)
                .items(arrayList)
                .itemsCallback((dialog, itemView, position, text) -> {
                    YouTubeLink youTubeLink = arrayList.get(position);
                    mPresenter.downloadTrack(musicTrack, youTubeLink);
                })
                .title(R.string.select_format)
                .positiveText(null)
                .negativeText(R.string.text_cancel);

        builder.build().show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void showProgDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this, R.style.DialogStyle);
        }

        if (!progressDialog.isShowing()) {
            progressDialog = new ProgressDialog(this, R.style.DialogStyle);
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    @Override
    public void dismissProgDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
        } catch (Exception e) {
        }
    }



}
