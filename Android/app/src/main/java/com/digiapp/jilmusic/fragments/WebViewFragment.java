package com.digiapp.jilmusic.fragments;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.digiapp.jilmusic.components.AdvancedWebView;
import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.dashboardView.views.DashboardActivity;
import com.digiapp.jilmusic.utils.Core;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.digiapp.jilmusic.MainActivity;
import com.digiapp.jilmusic.R;

import com.digiapp.jilmusic.CallbackListener;
import com.digiapp.jilmusic.utils.ResourceHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class WebViewFragment extends Fragment implements AdvancedWebView.Listener {

    CallbackListener callbackListener;
    BottomNavigationViewEx bottomNavigationView;

    public static final String GENRES_URL = "http://jil-music.digi-app.com/";
    public static final String TOP_50_URL = "http://jil-music.digi-app.com/top-50";
    public static final String SEARCH_URL = "http://jil-music.digi-app.com/search";
    public static final String LIBRARY_URL = "http://jil-music.digi-app.com/library";
    public static final String ACCOUNT_URL = "http://jil-music.digi-app.com/account-settings";
    public static final String SUBSCRIPTION_URL = "http://jil-music.digi-app.com/account/subscription";

    static HashMap<String, String> mPageTitle = new HashMap();
    static HashMap<String, Integer> mPageBottomNav = new HashMap();

    public static String mCurrentPage;

    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;

    public static WebView webview;
    static final String MAIN_URL = "http://jil-music.digi-app.com/";

    private View loadingView;

    public WebViewFragment() {
        // Required empty public constructor
    }

    public void setCurrentPage(String url) {
        this.mCurrentPage = url;
    }

    public String getCurrentPage() {
        return mCurrentPage;
    }

    public static WebView getWebView() {
        return webview;
    }

    public static void stopPlayback() {
        if (getWebView() != null) {
            getWebView().loadUrl("javascript:window.myAndroid.appToAngular.pauseFromApp();");
        }
    }

    public static void playPlayback() {
        if (getWebView() != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    getWebView().loadUrl("javascript:window.myAndroid.appToAngular.playFromApp();");
                }
            });
        }
    }

    public static void nextFromApp() {
        if (getWebView() != null) {
            getWebView().loadUrl("javascript:window.myAndroid.appToAngular.nextFromApp();");
        }
    }

    public static void prevFromApp() {
        if (getWebView() != null) {
            getWebView().loadUrl("javascript:window.myAndroid.appToAngular.prevFromApp();");
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void top50Page() {
        String js = "javascript:document.getElementsByClassName('top_50')[0].click()";
        webview.evaluateJavascript(js, value -> {
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void genresPage() {
        // home pr genres
        String home = "genres";
        if (Core.isUserLogged(AppObj.getGlobalContext())) {
            home = "home";
        }
        String js = "javascript:document.getElementsByClassName('" + home + "')[0].click()";
        webview.evaluateJavascript(js, value -> {
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void searchPage() {
        String js = "javascript:document.getElementsByClassName('search')[0].click()";
        webview.evaluateJavascript(js, value -> {
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void libraryPage() {
        String js = "javascript:document.getElementsByClassName('your__music')[0].click()";
        webview.evaluateJavascript(js, value -> {
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void accountPage() {
        String js = "javascript:document.getElementsByClassName('account')[0].click()";
        webview.evaluateJavascript(js, value -> {
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);

        loadingView = view.findViewById(R.id.loadingView);

        webview = view.findViewById(R.id.webview);
        webview.setBackgroundColor(getResources().getColor(R.color.color_back));

        WebSettings webSettings = webview.getSettings();

        webSettings.setAppCacheMaxSize(1024 * 1024 * 8);
        webSettings.setAppCachePath(ContextCompat.getCodeCacheDir(getActivity()).getPath());
        webSettings.setAppCacheEnabled(false);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAllowContentAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setDefaultTextEncodingName("utf-8");

        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        String android_id = Settings.Secure.getString(getActivity().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev> (KHTML, like Gecko) Chrome/<Chrome Rev> Mobile Safari/<WebKit Rev>;devId=" + android_id);

        // webview.setListener(this, this);
        webview.setWebChromeClient(new MyWebChromeClient());
        webview.addJavascriptInterface(new WebAppInterface(), "Android");
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(MAIN_URL)) {
                    mCurrentPage = url;
                    view.loadUrl(url);
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                }
                return true;
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request.getUrl().toString().contains(MAIN_URL)) {
                    mCurrentPage = request.getUrl().toString();
                    view.loadUrl(request.getUrl().toString());
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()));
                    startActivity(i);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                CookieSyncManager.getInstance().sync();
            }
        });

        bottomNavigationView = (BottomNavigationViewEx)
                view.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setTextVisibility(false);
        bottomNavigationView.setIconsMarginTop(10);

        ResourceHelper.removeShiftMode(bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_genres:
                    setCurrentPage(GENRES_URL);
                    break;
                case R.id.action_top:
                    setCurrentPage(TOP_50_URL);
                    break;
                case R.id.action_search:
                    setCurrentPage(SEARCH_URL);
                    break;
                case R.id.action_your:
                    setCurrentPage(LIBRARY_URL);
                    break;
                case R.id.action_account:
                    setCurrentPage(ACCOUNT_URL);
                    break;
            }
            refresh();

            return true;
        });

        refresh();

        return view;
    }

    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private final BlockingQueue<Runnable> mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();
    ThreadPoolExecutor mDecodeThreadPool = new ThreadPoolExecutor(
            NUMBER_OF_CORES,       // Initial pool size
            NUMBER_OF_CORES,       // Max pool size
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            mDecodeWorkQueue);
    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();
    private ArrayList<Future> mCurrentTasks = new ArrayList<>();

    class HTMLDownloadHandler implements Runnable {

        String mUrl;
        Handler.Callback mCallback;

        public HTMLDownloadHandler(String url, Handler.Callback callback){
            mUrl = url;
            mCallback = callback;
        }

        @Override
        public void run() {

            StringBuilder html = new StringBuilder();

            try {
                URLConnection connection = (new URL(mUrl)).openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();

                // Read and store the result line by line then return the entire string.
                InputStream in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                for (String line; (line = reader.readLine()) != null; ) {
                    html.append(line);
                }
                in.close();
            }catch (Exception ex){
                ex.printStackTrace();
            }

            Message message = new Message();
            message.obj = html.toString();

            mCallback.handleMessage(message);
        }
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        loadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(String url) {
        loadingView.setVisibility(View.GONE);
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        loadingView.setVisibility(View.GONE);
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {

    }

    @Override
    public void onExternalPageRequest(String url) {

    }

    class MyWebChromeClient extends WebChromeClient {
        // The undocumented magic method override
        // Eclipse will swear at you if you try to put @Override here

        // For 3.0+ Devices (Start)
        // onActivityResult attached before constructor
        protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, getString(R.string.file_browser)), FILECHOOSER_RESULTCODE);
        }

        // For Lollipop 5.0+ Devices
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }

            uploadMessage = filePathCallback;

            Intent intent = fileChooserParams.createIntent();
            try {
                startActivityForResult(intent, REQUEST_SELECT_FILE);
            } catch (ActivityNotFoundException e) {
                uploadMessage = null;
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.open_file_issue), Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }

        //For Android 4.1 only
        protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, getString(R.string.file_browser)), FILECHOOSER_RESULTCODE);
        }

        protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, getString(R.string.file_chooser)), FILECHOOSER_RESULTCODE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getWebView().onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        getWebView().onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // getWebView().onDestroy();
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
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
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

    class WebAppInterface {
        @JavascriptInterface
        public void getTrackObj(String track_to_mobile) {
            if (callbackListener != null) {
                if (!"undefined".equalsIgnoreCase(track_to_mobile)) {
                    callbackListener.onCustomCallback(CallbackListener.Type.GET_TRACK_OBJECT, track_to_mobile);
                }
            }
        }

        @JavascriptInterface
        public void trackToPlay(String track_to_mobile) {
            if (callbackListener != null) {
                if (!"undefined".equalsIgnoreCase(track_to_mobile)) {
                    callbackListener.onCustomCallback(CallbackListener.Type.TRACK_TO_PLAY, track_to_mobile);
                }
            }
        }

        @JavascriptInterface
        public void userRegister(String user) {
            if (callbackListener != null) {
                if (!"undefined".equalsIgnoreCase(user)) {
                    callbackListener.onCustomCallback(CallbackListener.Type.USER_REGISTER, user);
                }
            }
        }

        @JavascriptInterface
        public void shareMediaItem(String data) {
            if (callbackListener != null) {
                if (!"undefined".equalsIgnoreCase(data)) {
                    callbackListener.onCustomCallback(CallbackListener.Type.SHARE, data);
                }
            }
        }

        @JavascriptInterface
        public void userLogin(String user) {
            if (callbackListener != null) {
                if (!"undefined".equalsIgnoreCase(user)) {
                    callbackListener.onCustomCallback(CallbackListener.Type.USER_LOGIN, user);
                }
            }
        }

        @JavascriptInterface
        public void userLogout(String user) {
            if (callbackListener != null) {
                if (!"undefined".equalsIgnoreCase(user)) {
                    callbackListener.onCustomCallback(CallbackListener.Type.USER_LOGOUT, user);
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    public void refresh() {

        String curPage = getCurrentPage();
        if (curPage == null) {
            curPage = GENRES_URL;
        }

        String loginUrl = "http://jil-music.digi-app.com/login";

        if (webview.getUrl() == null
                || webview.getUrl().equalsIgnoreCase(loginUrl)) {
            webview.loadUrl(curPage);
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
            getActivity().setTitle(mPageTitle.get(curPage));
        }

        BottomNavigationItemView itemView = bottomNavigationView.getBottomNavigationItemView(mPageBottomNav.get(curPage));
        itemView.getItemData().setChecked(true);

        if (getActivity() instanceof DashboardActivity) {
            if (mPageBottomNav.get(curPage) < 3) {
                NavigationView navigationView = ((DashboardActivity) getActivity()).getNavigationView();
                navigationView.getMenu().getItem(mPageBottomNav.get(curPage)).setChecked(true);
            }
        }

        //top_50 ng-star-inserted active
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
