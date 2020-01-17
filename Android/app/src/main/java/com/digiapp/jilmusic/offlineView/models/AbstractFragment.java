package com.digiapp.jilmusic.offlineView.models;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewAnimator;

import com.digiapp.jilmusic.R;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.model.MvpRecyclerListAdapter;
import com.digiapp.jilmusic.model.PresenterManager;
import com.digiapp.jilmusic.offlineView.presenters.MainPresenter;
import com.digiapp.jilmusic.offlineView.views.MainView;
import com.digiapp.jilmusic.offlineView.views.MusicTrackViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class AbstractFragment extends Fragment implements MainView {
    private static final int POSITION_LIST = 0;
    private static final int POSITION_EMPTY = 1;

    @BindView(R.id.listView)
    RecyclerView listView;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    private ViewAnimator animator;
    private MvpRecyclerListAdapter adapter;
    private MainPresenter presenter;
    private Handler mHandler = new Handler(Looper.myLooper());
    private ItemClickListener itemClickListener;

    public abstract MvpRecyclerListAdapter createAdapter();
    public abstract void loadData();

    public AbstractFragment() {
        // Required empty public constructor
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public MvpRecyclerListAdapter getAdapter(){
        return adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_abstract, container, false);
        ButterKnife.bind(this, view);

        if (savedInstanceState == null) {
            presenter = new MainPresenter();
        } else {
            presenter = PresenterManager.getInstance().restorePresenter(savedInstanceState);
        }

        animator = (ViewAnimator) view.findViewById(R.id.animator);

        listView.setHasFixedSize(true);
        listView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        adapter = createAdapter();
        adapter.setItemClickListener(new MusicTrackViewHolder.ItemClickListener() {
            @Override
            public void onItemClicked(int pos) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClicked((MusicTrack) adapter.getItem(pos));
                }
            }

            @Override
            public void onLongItemClicked(int pos) {
                if (itemClickListener != null) {
                    itemClickListener.onLongItemClicked((MusicTrack) adapter.getItem(pos));
                }
            }
        });

        listView.setAdapter(adapter);
        loadData();

        swipeRefresh.setOnRefreshListener(() -> loadData());

        return view;
    }

    public MainPresenter getPresenter() {
        return presenter;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.bindView(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.bindView(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(presenter!=null) {
            PresenterManager.getInstance().savePresenter(presenter, outState);
        }
    }

    @Override
    public void showData(List<MusicTrack> data) {
        mHandler.post(() -> {
            swipeRefresh.setRefreshing(false);
            adapter.removeAllAddAll(data);
            animator.setDisplayedChild(POSITION_LIST);
        });
    }

    @Override
    public void showLoading() {
        mHandler.post(() -> swipeRefresh.setRefreshing(true));
    }

    @Override
    public void showEmpty() {
        animator.setDisplayedChild(POSITION_EMPTY);
    }

    public interface ItemClickListener {
        void onItemClicked(MusicTrack musicTrack);
        void onLongItemClicked(MusicTrack musicTrack);
    }
}
