package com.digiapp.jilmusic.offlineView.views;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.R;
import com.digiapp.jilmusic.allMusicView.models.AllMusicAdapter;
import com.digiapp.jilmusic.allMusicView.views.AllMusicFragment;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.beans.SelectionList;
import com.digiapp.jilmusic.dao.MusicRoomDatabase;
import com.digiapp.jilmusic.dao.SelectionsDAO;
import com.digiapp.jilmusic.dashboardView.views.DashboardActivity;
import com.digiapp.jilmusic.favouritesView.models.FavouritesAdapter;
import com.digiapp.jilmusic.favouritesView.views.FavouritesFragment;
import com.digiapp.jilmusic.offlineView.models.AbstractFragment;
import com.digiapp.jilmusic.offlineView.presenters.MediaBrowserPresenter;
import com.digiapp.jilmusic.offlineView.presenters.MediaControllerPresenter;
import com.digiapp.jilmusic.selectionsView.views.SelectionsFragment;
import com.digiapp.jilmusic.utils.Core;
import com.digiapp.jilmusic.utils.MediaIDHelper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MusicPlayerCompatFragment extends Fragment implements MediaBrowserPresenter.View,
        AbstractFragment.ItemClickListener,
        MediaControllerPresenter.View {

    public static final String MUSIC_PLAYER_FRAGMENT_TAG = "music_player_fragment_tag";

    @BindView(R.id.vpPager)
    ViewPager vpPager;
    @BindView(R.id.controls_container)
    CardView controls_container;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.actionsView)
    View actionsView;
    @BindView(R.id.btnSelectAll)
    AppCompatImageButton btnSelectAll;
    @BindView(R.id.btnDeleteSelected)
    AppCompatImageButton btnDeleteSelected;
    @BindView(R.id.btnMoveSelection)
    AppCompatImageButton btnMoveSelection;
    // @BindView(R.id.viewAnimator)
    // ViewAnimator viewAnimator;

    PlaybackControlsFragment fragment_playback_controls;
    MediaBrowserPresenter mMediaBrowserPresenter;
    MediaControllerPresenter mMediaControllerPresenter;
    Handler mHandler = new Handler(Looper.getMainLooper());
    MyPagerAdapter myPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_player, container, false);
        ButterKnife.bind(this, view);

        myPagerAdapter = new MyPagerAdapter(getFragmentManager());
        vpPager.setAdapter(myPagerAdapter);
        vpPager.addOnPageChangeListener(new onPageChanger(tabLayout));
        tabLayout.addOnTabSelectedListener(new onTabChanged(vpPager));

        tabLayout.getTabAt(1).select();

        fragment_playback_controls = (PlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.fragment_playback_controls);

        mMediaBrowserPresenter = new MediaBrowserPresenter(this);
        mMediaControllerPresenter = new MediaControllerPresenter(getActivity(), this);

        return view;
    }

    @OnClick({R.id.btnSelectAll, R.id.btnDeleteSelected, R.id.btnMoveSelection})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSelectAll:
                myPagerAdapter.selectAll(vpPager.getCurrentItem());
                break;
            case R.id.btnDeleteSelected:
                myPagerAdapter.deleteSelected(vpPager.getCurrentItem());
                break;
            case R.id.btnMoveSelection:
                Core.getFullSelectionDialog(getActivity(), msg -> {
                    if (msg.obj != null) {
                        SelectionList selectionList = (SelectionList) msg.obj;
                        if (myPagerAdapter.getSelected(vpPager.getCurrentItem()).size() > 0) {
                            myPagerAdapter.moveToSelection(vpPager.getCurrentItem(), selectionList);
                        }
                    }

                    return false;
                }).show();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMediaBrowserPresenter.connectMediaBrowser();
        mMediaControllerPresenter.checkIfControllerAvailable();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMediaBrowserPresenter.onStop();
        mMediaControllerPresenter.onStop();
    }

    @Override
    public void onMediaBrowserConnected(MediaSessionCompat.Token token) {
        mMediaControllerPresenter.onConnected();
    }

    @Override
    public void setMediaController(MediaControllerCompat mediaControllerCompat) {
        MediaControllerCompat.setMediaController(getActivity(), mediaControllerCompat);
    }

    @Override
    public void onProgressUpdated(int progress) {

    }

    @Override
    public void onError(String message) {
        mHandler.post(() -> Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadataCompat, MediaControllerCompat controller) {

    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        mMediaControllerPresenter.checkIfControllerAvailable();
        myPagerAdapter.notifyDataChanged();
    }

    @Override
    public void onControllerStatusChanged(boolean available) {
        mHandler.post(() -> {
            if (available) {
                getChildFragmentManager().beginTransaction()
                        .show(getChildFragmentManager().findFragmentById(R.id.fragment_playback_controls))
                        .commit();
            } else {
                getChildFragmentManager().beginTransaction()
                        .hide(getChildFragmentManager().findFragmentById(R.id.fragment_playback_controls))
                        .commit();
            }
        });
    }

    @Override
    public void onItemClicked(MusicTrack musicTrack) {
        MediaBrowserCompat.MediaItem item = musicTrack.mediaItem;
        if (item.isPlayable()) {
            MediaControllerCompat.getMediaController(getActivity()).getTransportControls()
                    .playFromMediaId(item.getMediaId(), null);
            fragment_playback_controls.onStart();

            if(getActivity() instanceof DashboardActivity){
                ((DashboardActivity) getActivity()).showBackButton(false);
                getActivity().setTitle(getString(R.string.app_name));
            }

        }else{
            // selections clicked so we need to browse items
            myPagerAdapter.browseSelected(musicTrack);
            if(getActivity() instanceof DashboardActivity){
                ((DashboardActivity) getActivity()).showBackButton(true);
                getActivity().setTitle(musicTrack.name);
            }
        }
    }

    @Override
    public void onLongItemClicked(MusicTrack musicTrack) {
        // viewAnimator.setDisplayedChild(viewAnimator.indexOfChild(viewAnimator));
        if (actionsView.getVisibility() == View.GONE) {
            actionsView.setVisibility(View.VISIBLE);
            myPagerAdapter.setMultipleSelection(true);
        } else {
            actionsView.setVisibility(View.GONE);
            myPagerAdapter.setMultipleSelection(false);
        }
        mMediaControllerPresenter.pauseMedia();
        myPagerAdapter.notifyDataChanged();
    }

    public class onTabChanged implements TabLayout.OnTabSelectedListener {

        ViewPager viewPager;

        onTabChanged(ViewPager viewPager) {
            this.viewPager = viewPager;
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            viewPager.setCurrentItem(tab.getPosition(), false);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    }

    public class onPageChanger implements ViewPager.OnPageChangeListener {

        TabLayout mTabLayout;

        public onPageChanger(TabLayout tabLayout) {
            mTabLayout = tabLayout;
        }

        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            mTabLayout.getTabAt(i).select();
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {
        private final int NUM_ITEMS = 3;

        FavouritesFragment favouritesFragment;
        AllMusicFragment allMusicFragment;
        SelectionsFragment selectionsFragment;

        boolean isMultipleSelection = false;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public void setMultipleSelection(boolean multipleSelection) {
            isMultipleSelection = multipleSelection;
        }

        public void notifyDataChanged() {
            if (allMusicFragment != null) {
                ((AllMusicAdapter) allMusicFragment.getAdapter()).setMultipleSelection(isMultipleSelection);
                allMusicFragment.getAdapter().notifyDataSetChanged();
            }

            if (favouritesFragment != null) {
                ((FavouritesAdapter) favouritesFragment.getAdapter()).setMultipleSelection(isMultipleSelection);
                favouritesFragment.getAdapter().notifyDataSetChanged();
            }
        }

        public void selectAll(int currentItem) {
            if (currentItem == 1) {
                if (allMusicFragment != null) {
                    ((AllMusicAdapter) allMusicFragment.getAdapter()).selectAll();
                }
            }
            if (currentItem == 0) {
                if (favouritesFragment != null) {
                    ((FavouritesAdapter) favouritesFragment.getAdapter()).selectAll();
                }
            }
        }

        public void deleteSelected(int currentItem) {
            if (currentItem == 1) {
                if (allMusicFragment != null) {
                    ((AllMusicAdapter) allMusicFragment.getAdapter()).deleteSelected();
                }
            }
            if (currentItem == 0) {
                if (favouritesFragment != null) {
                    ((FavouritesAdapter) favouritesFragment.getAdapter()).deleteSelected();
                }
            }
            setMultipleSelection(false);
            actionsView.setVisibility(View.GONE);
            notifyDataChanged();
        }

        public void moveToSelection(int currentItem, SelectionList selectionList) {
            if (currentItem == 1) {
                if (allMusicFragment != null) {
                    ((AllMusicAdapter) allMusicFragment.getAdapter()).moveSelectedToSelection(selectionList);
                }
            }
            if (currentItem == 0) {
                if (favouritesFragment != null) {
                    ((FavouritesAdapter) favouritesFragment.getAdapter()).moveSelectedToSelection(selectionList);
                }
            }
            setMultipleSelection(false);
            actionsView.setVisibility(View.GONE);
            notifyDataChanged();
            Toast.makeText(getContext(),"Added",Toast.LENGTH_SHORT).show();
        }

        public ArrayList<MusicTrack> getSelected(int currentItem) {
            ArrayList<MusicTrack> arrayList = new ArrayList<>();
            if (currentItem == 1) {
                if (allMusicFragment != null) {
                    arrayList.addAll(((AllMusicAdapter) allMusicFragment.getAdapter()).getAllSelected());
                }
            }
            if (currentItem == 0) {
                if (favouritesFragment != null) {
                    arrayList.addAll(((FavouritesAdapter) favouritesFragment.getAdapter()).getAllSelected());
                }
            }

            return arrayList;
        }

        public void browseSelected(MusicTrack musicTrack){
            if(selectionsFragment!=null){
                selectionsFragment.loadData(musicTrack);
            }
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            if (fragment instanceof AbstractFragment) {
                ((AbstractFragment) fragment).setItemClickListener(MusicPlayerCompatFragment.this);
            }
            return fragment;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (favouritesFragment == null) {
                        favouritesFragment = new FavouritesFragment();
                    }
                    return favouritesFragment;
                case 1:
                    if (allMusicFragment == null) {
                        allMusicFragment = new AllMusicFragment();
                    }
                    return allMusicFragment;
                case 2:
                    if(selectionsFragment==null){
                        selectionsFragment = new SelectionsFragment();
                    }
                    return selectionsFragment;
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }
    }

}
