package org.kymjs.blog.ui;

import org.kymjs.blog.R;
import org.kymjs.kjframe.ui.BindView;
import org.kymjs.kjframe.ui.ViewInject;
import org.kymjs.kjframe.utils.StringUtils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * 我的博客网站专有显示器(由于html可配置，所以处理起来要方便很多)
 * 
 * @author kymjs
 * 
 */
public class MyBlogBrowser extends TitleBarActivity {

    @BindView(id = R.id.webview)
    WebView mWebView;
    @BindView(id = R.id.progress)
    ProgressBar mProgress;

    public static final String BROWSER_KEY = "browser_url";
    public static final String BROWSER_TITLE_KEY = "browser_title_url";
    public static final String DEFAULT = "http://blog.kymjs.com/";

    private String mCurrentUrl = DEFAULT;
    private String strTitle;

    @Override
    public void setRootView() {
        setContentView(R.layout.aty_browser);
    }

    @Override
    public void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            mCurrentUrl = intent.getStringExtra(BROWSER_KEY);
            strTitle = intent.getStringExtra(BROWSER_TITLE_KEY);
            if (StringUtils.isEmpty(mCurrentUrl)) {
                mCurrentUrl = DEFAULT;
            }
            if (StringUtils.isEmpty(strTitle)) {
                strTitle = getString(R.string.app_name);
            }
        }
    }

    @Override
    public void initWidget() {
        super.initWidget();
        initWebView();
        findViewById(R.id.browser_bottom).setVisibility(View.GONE);
        mWebView.loadUrl(mCurrentUrl);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
    }

    @Override
    public void widgetClick(View v) {
        super.widgetClick(v);
        switch (v.getId()) {
        case R.id.browser_back:
            mWebView.goBack();
            break;
        case R.id.browser_forward:
            mWebView.goForward();
            break;
        case R.id.browser_refresh:
            mWebView.loadUrl(mWebView.getUrl());
            break;
        case R.id.browser_system_browser:
            try {
                // 启用外部浏览器
                Uri uri = Uri.parse(mCurrentUrl);
                Intent it = new Intent(Intent.ACTION_VIEW, uri);
                aty.startActivity(it);
            } catch (Exception e) {
                ViewInject.toast("网页地址错误");
            }
            break;
        }
    }

    /**
     * 载入链接之前会被调用
     * 
     * @param view
     *            WebView
     * @param url
     *            链接地址
     */
    protected void onUrlLoading(WebView view, String url) {
        mProgress.setVisibility(View.VISIBLE);
    }

    /**
     * 链接载入成功后会被调用
     * 
     * @param view
     *            WebView
     * @param url
     *            链接地址
     */
    protected void onUrlFinished(WebView view, String url) {
        mCurrentUrl = url;
        mProgress.setVisibility(View.GONE);
        if (aty != null && mWebView != null) { // 必须做判断，由于webview加载属于耗时操作，可能会本Activity已经关闭了才被调用
            mTvTitle.setText(mWebView.getTitle());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                finish();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * 当前WebView显示页面的标题
     * 
     * @param view
     *            WebView
     * @param title
     *            web页面标题
     */
    protected void onWebTitle(WebView view, String title) {
        if (aty != null && mWebView != null) { // 必须做判断，由于webview加载属于耗时操作，可能会本Activity已经关闭了才被调用
            mTvTitle.setText(mWebView.getTitle());
        }
    }

    /**
     * 当前WebView显示页面的图标
     * 
     * @param view
     *            WebView
     * @param icon
     *            web页面图标
     */
    protected void onWebIcon(WebView view, Bitmap icon) {}

    /**
     * 初始化浏览器设置信息
     */
    private void initWebView() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(false); // 启用支持javascript
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);// 优先使用缓存
        webSettings.setAllowFileAccess(true);// 可以访问文件
        webSettings.setBuiltInZoomControls(true);// 支持缩放
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            webSettings.setDisplayZoomControls(false);// 支持缩放
        }
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            onWebTitle(view, title);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
            onWebIcon(view, icon);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) { // 进度
            super.onProgressChanged(view, newProgress);
            if (newProgress > 60) {
                mProgress.setVisibility(View.GONE);
            }
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            onUrlLoading(view, url);
            boolean flag = super.shouldOverrideUrlLoading(view, url);
            mCurrentUrl = url;
            return flag;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            onUrlFinished(view, url);
        }
    }

    /**
     * 移除字符串中的Html标签
     * 
     * @author kymjs (https://github.com/kymjs)
     * @param pHTMLString
     * @return
     */
    public static Spanned stripTags(final String pHTMLString) {
        String str = pHTMLString.replaceAll(
                "<footer\\s+([^>]*)\\s*>*</footer>", "");
        return Html.fromHtml(str);
    }
}
