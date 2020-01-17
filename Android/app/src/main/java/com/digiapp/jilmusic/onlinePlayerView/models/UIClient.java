package com.digiapp.jilmusic.onlinePlayerView.models;

import com.digiapp.jilmusic.onlinePlayerView.presenters.XWalkWebViewPresenter;

import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

public class UIClient extends XWalkUIClient {

    XWalkWebViewPresenter.View mView;

    public UIClient(XWalkView view) {
        super(view);
    }

    public UIClient(XWalkView view, XWalkWebViewPresenter.View presenterView){
        super(view);
        mView = presenterView;
    }

    @Override
    public void onPageLoadStarted(XWalkView view, String url) {
        super.onPageLoadStarted(view, url);
        mView.showProgressBar();
    }

    @Override
    public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
        super.onPageLoadStopped(view, url, status);
        mView.hideProgressBar();
        mView.onPageLoadFinished();
    }
}
