
package com.myseed.tv;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.widget.Toast;

public class MainActivity extends Activity {

    private WebView webView;
    private boolean pageFailed = false;
    private boolean dialogShowing = false;

    private static final String HOME_URL =
        "https://myseed.cam";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        createWebView();
        webView.loadUrl(HOME_URL);
    }

    private void createWebView() {

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.requestFocus();

        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(
                WebView view, String url, Bitmap favicon) {

                pageFailed = false;
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(
                WebView view, String url) {

                pageFailed = false;
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                WebView view, WebResourceRequest request) {

                return false;
            }

            @Override
            public void onReceivedError(
                WebView view,
                WebResourceRequest request,
                WebResourceError error) {

                super.onReceivedError(view, request, error);

                if (request.isForMainFrame()) {
                    pageFailed = true;
                    showError();
                }
            }

            @Override
            public void onReceivedHttpError(
                WebView view,
                WebResourceRequest request,
                android.webkit.WebResourceResponse response) {

                super.onReceivedHttpError(view, request, response);

                if (request.isForMainFrame()) {
                    pageFailed = true;
                    showError();
                }
            }

            @Override
            public void onReceivedSslError(
                WebView view,
                SslErrorHandler handler,
                SslError error) {

                handler.cancel();
                pageFailed = true;
                showError();
            }
        });
    }

    private void showError() {

        if (isFinishing() || dialogShowing) return;

        dialogShowing = true;

        runOnUiThread(() -> {
            if (isFinishing()) {
                dialogShowing = false;
                return;
            }

            new AlertDialog.Builder(this)
                .setTitle("MySeed TV")
                .setMessage(
                    "مش قادرين نوصل للموقع.\n" +
                    "تأكد من الإنترنت وحاول مرة تانية."
                )
                .setCancelable(false)
                .setPositiveButton("إعادة المحاولة",
                    (dialog, which) -> {
                        dialogShowing = false;
                        pageFailed = false;
                        webView.loadUrl(HOME_URL);
                    })
                .setNegativeButton("إغلاق",
                    (dialog, which) -> {
                        dialogShowing = false;
                    })
                .setOnDismissListener(dialog ->
                    dialogShowing = false)
                .show();
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (dialogShowing) {
                return true;
            }

            if (webView != null && webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER &&
            pageFailed) {
            webView.loadUrl(HOME_URL);
            pageFailed = false;
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (webView != null) {
            webView.onResume();
            webView.resumeTimers();
        }
    }

    @Override
    protected void onPause() {
        if (webView != null) {
            webView.onPause();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }

        super.onDestroy();
    }
}

