package com.digiapp.jilmusic.onlinePlayerView.presenters;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.webkit.ValueCallback;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.BuildConfig;
import com.digiapp.jilmusic.R;
import com.digiapp.jilmusic.utils.Core;

import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkView;

import java.util.HashMap;

public class XWalkWebViewPresenter {

    static final String MAIN_URL = BuildConfig.WEB_URL;
    public static final String GENRES_URL = MAIN_URL;
    public static final String TOP_50_URL = MAIN_URL + "/top-50";
    public static final String SEARCH_URL = MAIN_URL + "/search";
    public static final String LIBRARY_URL = MAIN_URL + "/library";
    public static final String ACCOUNT_URL = MAIN_URL + "/account-settings";
    public static final String SUBSCRIPTION_URL = MAIN_URL + "/account/subscription";

    static HashMap<String, String> mPageTitle = new HashMap();
    static HashMap<String, Integer> mPageBottomNav = new HashMap();

    View mView;
    static XWalkView mWebView;
    public static String mCurrentPage;

    public XWalkWebViewPresenter(View view, XWalkView webView) {
        this.mView = view;
        this.mWebView = webView;
    }

    public void setCurrentPage(String url) {
        this.mCurrentPage = url;
    }

    public String getCurrentPage() {
        return mCurrentPage;
    }

    public void refresh() {

        String curPage = getCurrentPage();
        if (curPage == null) {
            curPage = GENRES_URL;
        }

        String loginUrl = MAIN_URL + "/login";

        if (mWebView.getUrl() == null
                || mWebView.getUrl().equalsIgnoreCase(loginUrl)) {
            mWebView.loadUrl(curPage);
        } else {
            switch (curPage) {
                case GENRES_URL:
                    genresPage();
                    break;
                case TOP_50_URL:
                    top50Page();
                    break;
                case SEARCH_URL:
                    searchPage();
                    break;
                case LIBRARY_URL:
                    libraryPage();
                    break;
                case ACCOUNT_URL:
                    accountPage();
                    break;
            }
        }

        if (mPageTitle.containsKey(curPage)) {
            mView.onTitleChanged(mPageTitle.get(curPage));
        }

        if (mPageBottomNav.get(curPage) != null) {
            mView.onBottomNavItemChecked(mPageBottomNav.get(curPage));
        }


        if (mPageBottomNav.get(curPage) != null && mPageBottomNav.get(curPage) < 3) {
            mView.onNavigationItemCheched(mPageBottomNav.get(curPage));
        }
    }

    public static void stopPlayback() {
        if (mWebView != null) {
            mWebView.loadUrl("javascript:window.myAndroid.appToAngular.pauseFromApp();");
        }
    }

    public static void playPlayback() {
        if (mWebView != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:window.myAndroid.appToAngular.playFromApp();");
                }
            });
        }
    }

    public static void nextFromApp() {
        if (mWebView != null) {
            mWebView.loadUrl("javascript:window.myAndroid.appToAngular.nextFromApp();");
        }
    }

    public static void setLightTheme() {
        if (mWebView != null) {
            mWebView.evaluateJavascript("setCurrentTheme('light');", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {

                }
            });
        }
    }

    public static void setDarkTheme() {
        if (mWebView != null) {
            mWebView.evaluateJavascript("setCurrentTheme('dark');", null);
        }
    }

    public static void getCurrentTheme(ValueCallback<String> callback) {
        if (mWebView != null) {
            mWebView.evaluateJavascript("javascript:getCurrentTheme();", callback);
        }
    }

    public static void prevFromApp() {
        if (mWebView != null) {
            mWebView.loadUrl("javascript:window.myAndroid.appToAngular.prevFromApp();");
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void top50Page() {
        if (mWebView != null) {
            String js = "javascript:document.getElementsByClassName('top_50')[0].click()";
            mWebView.evaluateJavascript(js, value -> {
            });
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void genresPage() {
        // home pr genres
        String home = "genres";
        if (Core.isUserLogged(AppObj.getGlobalContext())) {
            home = "home";
        }
        String js = "javascript:document.getElementsByClassName('" + home + "')[0].click()";
        mWebView.evaluateJavascript(js, value -> {
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void searchPage() {
        String js = "javascript:document.getElementsByClassName('search')[0].click()";
        mWebView.evaluateJavascript(js, value -> {
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void libraryPage() {
        String js = "javascript:document.getElementsByClassName('your__music')[0].click()";
        mWebView.evaluateJavascript(js, value -> {
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void accountPage() {
        String js = "javascript:document.getElementsByClassName('account')[0].click()";
        mWebView.evaluateJavascript(js, value -> {
        });
    }

    public interface View {
        void showProgressBar();

        void hideProgressBar();

        void onPageLoadFinished();

        void onTitleChanged(String title);

        void onBottomNavItemChecked(@NonNull int navId);

        void onNavigationItemCheched(int vavId);
    }

    static {
        mPageTitle.put(GENRES_URL, AppObj.getGlobalContext().getString(R.string.app_name));
        mPageTitle.put(TOP_50_URL, AppObj.getGlobalContext().getString(R.string.nav_top));
        mPageTitle.put(SEARCH_URL, AppObj.getGlobalContext().getString(R.string.nav_search));
        mPageTitle.put(LIBRARY_URL, AppObj.getGlobalContext().getString(R.string.nav_your));
        mPageTitle.put(ACCOUNT_URL, AppObj.getGlobalContext().getString(R.string.nav_account));
        mPageTitle.put(SUBSCRIPTION_URL, AppObj.getGlobalContext().getString(R.string.nav_sub));

        mPageBottomNav.put(GENRES_URL, 0);
        mPageBottomNav.put(TOP_50_URL, 1);
        mPageBottomNav.put(SEARCH_URL, 2);
        mPageBottomNav.put(LIBRARY_URL, 3);
        mPageBottomNav.put(ACCOUNT_URL, 4);
    }

}
