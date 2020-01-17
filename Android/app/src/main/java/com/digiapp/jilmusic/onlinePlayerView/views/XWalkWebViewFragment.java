package com.digiapp.jilmusic.onlinePlayerView.views;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.digiapp.jilmusic.CallbackListener;
import com.digiapp.jilmusic.MainActivity;
import com.digiapp.jilmusic.dashboardView.views.DashboardActivity;
import com.digiapp.jilmusic.R;
import com.digiapp.jilmusic.onlinePlayerView.models.ResourceClient;
import com.digiapp.jilmusic.onlinePlayerView.models.UIClient;
import com.digiapp.jilmusic.onlinePlayerView.models.WebAppInterface;
import com.digiapp.jilmusic.onlinePlayerView.presenters.XWalkWebViewPresenter;
import com.digiapp.jilmusic.utils.ResourceHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.xwalk.core.XWalkSettings.LOAD_CACHE_ELSE_NETWORK;

/**
 * A simple {@link Fragment} subclass.
 */
public class XWalkWebViewFragment extends Fragment implements XWalkWebViewPresenter.View {

    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private CallbackListener callbackListener;

    public static XWalkView webview;
    XWalkWebViewPresenter mPresenter;

    @BindView(R.id.bottom_navigation)
    BottomNavigationViewEx bottomNavigationView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    Handler mHandler = new Handler(Looper.getMainLooper());

    public XWalkWebViewFragment() {
        // Required empty public constructor
    }

    public void onReady() {
        webview.setBackgroundColor(getResources().getColor(R.color.color_back));

        XWalkSettings webSettings = webview.getSettings();
        webSettings.setLoadsImagesAutomatically(false);
        webSettings.setLayoutAlgorithm(XWalkSettings.LayoutAlgorithm.NORMAL);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAllowContentAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        // webSettings.setCacheMode(LOAD_CACHE_ELSE_NETWORK);

        webSettings.setBuiltInZoomControls(false);
        webSettings.setLayoutAlgorithm(XWalkSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSaveFormData(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setAllowFileAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        String android_id = Settings.Secure.getString(getActivity().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev> (KHTML, like Gecko) Chrome/<Chrome Rev> Mobile Safari/<WebKit Rev>;devId=" + android_id);
        webview.addJavascriptInterface(new WebAppInterface(callbackListener), "Android");

        ResourceClient resourceClient = new ResourceClient(webview);
        webview.setResourceClient(resourceClient);
        webview.setUIClient(new UIClient(webview, this));

        webview.resumeTimers();
        webview.onShow();

        mPresenter.refresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_xwalk_web_view, container, false);
        ButterKnife.bind(this, view);

        XWalkPreferences.setValue("enable-javascript", true);
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
        XWalkPreferences.setValue(XWalkPreferences.ALLOW_UNIVERSAL_ACCESS_FROM_FILE, true);
        XWalkPreferences.setValue(XWalkPreferences.JAVASCRIPT_CAN_OPEN_WINDOW, true);
        XWalkPreferences.setValue(XWalkPreferences.SUPPORT_MULTIPLE_WINDOWS, true);
        XWalkPreferences.setValue(XWalkPreferences.ENABLE_EXTENSIONS,true);
        XWalkPreferences.setValue(XWalkPreferences.SPATIAL_NAVIGATION,true);
        XWalkPreferences.setValue(XWalkPreferences.ANIMATABLE_XWALK_VIEW,true);

        webview = view.findViewById(R.id.webview);
        return view;
    }

    /*@Override
    public void onStart() {
        super.onStart();

    }*/

    @Override
    public void onResume() {
        super.onResume();
        mPresenter = new XWalkWebViewPresenter(this, webview);
    }

    public void switchTheme(int mode) {
        if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            mHandler.post(() -> XWalkWebViewPresenter.setDarkTheme());
        } else if (mode == AppCompatDelegate.MODE_NIGHT_NO) {
            mHandler.post(() -> XWalkWebViewPresenter.setLightTheme());
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        if (bottomNavigationView != null) {
            bottomNavigationView.setTextVisibility(false);
            bottomNavigationView.setIconsMarginTop(10);
            ResourceHelper.removeShiftMode(bottomNavigationView);

            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                switch (item.getItemId()) {
                    case R.id.action_genres:
                        mPresenter.setCurrentPage(mPresenter.GENRES_URL);
                        break;
                    case R.id.action_top:
                        mPresenter.setCurrentPage(mPresenter.TOP_50_URL);
                        break;
                    case R.id.action_search:
                        mPresenter.setCurrentPage(mPresenter.SEARCH_URL);
                        break;
                    case R.id.action_your:
                        mPresenter.setCurrentPage(mPresenter.LIBRARY_URL);
                        break;
                    case R.id.action_account:
                        mPresenter.setCurrentPage(mPresenter.ACCOUNT_URL);
                        break;
                }
                mPresenter.refresh();

                return true;
            });
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.upload_image_fail), Toast.LENGTH_LONG).show();
        }
    }

    public void setCallbackListener(CallbackListener callbackListener) {
        this.callbackListener = callbackListener;
    }

    public void setCurrentPage(String page) {
        mPresenter.setCurrentPage(page);
    }

    public void refresh() {
        mPresenter.refresh();
    }

    @Override
    public void showProgressBar() {
        mHandler.post(() -> progressBar.setVisibility(View.VISIBLE));
    }

    @Override
    public void hideProgressBar() {
        mHandler.post(() -> progressBar.setVisibility(View.GONE));
    }

    @Override
    public void onPageLoadFinished() {
        mHandler.post(() -> {
            if (getActivity() == null) {
                return;
            }
            int nightModeFlags =
                    getActivity().getResources().getConfiguration().uiMode &
                            Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    XWalkWebViewPresenter.setDarkTheme();
                    break;

                case Configuration.UI_MODE_NIGHT_NO:
                    XWalkWebViewPresenter.setLightTheme();
                    break;
            }
        });
    }

    @Override
    public void onTitleChanged(String title) {
        if (getActivity() != null) {
            getActivity().setTitle(title);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBottomNavItemChecked(int navId) {
        BottomNavigationItemView itemView = bottomNavigationView.getBottomNavigationItemView(navId);
        itemView.getItemData().setChecked(true);
    }

    @Override
    public void onNavigationItemCheched(int vavId) {
        NavigationView navigationView = ((DashboardActivity) getActivity()).getNavigationView();
        navigationView.getMenu().getItem(vavId).setChecked(true);
    }

}
