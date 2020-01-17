package com.digiapp.jilmusic.ui;

import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ValueCallback;

import com.digiapp.jilmusic.R;

import org.jetbrains.annotations.NonNls;
import org.xwalk.core.XWalkView;

public class WebViewTestActivity extends AppCompatActivity {

    public static volatile XWalkView webview;
    @NonNls
    static final String MAIN_URL = "http://jil-music.digi-app.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view_test);

        webview = findViewById(R.id.webView);
        webview.setBackgroundColor(getResources().getColor(R.color.color_back));
        /*webview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }

            public void onPageFinished(WebView view, String url) {
                CookieSyncManager.getInstance().sync();
            }
        });*/

        /*WebSettings webSettings = webview.getSettings();

        webSettings.setAppCacheMaxSize(1024 * 1024 * 80);
        webSettings.setAppCachePath(ContextCompat.getCodeCacheDir(this).getPath());
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setAllowContentAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        String android_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev> (KHTML, like Gecko) Chrome/<Chrome Rev> Mobile Safari/<WebKit Rev>;devId=" + android_id);

        webview.loadUrl(MAIN_URL);
        */

        String android_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        webview.load(MAIN_URL, null);
        webview.setUserAgentString("Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev> (KHTML, like Gecko) Chrome/<Chrome Rev> Mobile Safari/<WebKit Rev>;devId=" + android_id);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webview.evaluateJavascript("javascript:window.myAndroid.appToAngular.playFromApp();",new ValueCallback<String>(){

                @Override
                public void onReceiveValue(String value) {
                    Log.d("debug",value);
                }
            });
        }
    }
}
