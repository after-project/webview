package com.after_project.webview;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewAssetLoader;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    WebClientCallback webClientCallback = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find webview view
        WebView webView = (WebView)findViewById(R.id.webview);

        // configuring Webview
        {

            //Load Assets - first create a assets directory in your Android App project - with assets directory created now you can add all your html files there.
            //with assets directory created and added your site files then now we are able to call addPathHandler
            final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                    .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                    .build();


            //Set Webview Client -
            // use the LocalContentWebViewClient class to handle extends androidx.webkit.WebViewClientCompat methods
            // to view more available methods to add, you can type @Override/implement methods... inside your LocalContentWebViewClient class
            {
                LocalContentWebViewClient LC = new LocalContentWebViewClient(assetLoader);
                webView.setWebViewClient(LC);
            }

            //Set Webview Settings
            {
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            }

            //you always go need to use a context, it resumes to specific a activity that you want to use as context.
            // example: if you are about to show toast only in MainActivity.java, then set MainActivity.this to your variable context.
            Context context = MainActivity.this;


            //Set WebAppInterface - WebAppInterface is the class you have created here in bellow source code at your MainAtivity.java
            // to set addJavascriptInterface use your WebAppInterface Class -
            // so now the html/javascript can connect to WebAppInterface Class and run your Android app functions
            webView.addJavascriptInterface(new WebAppInterface(context), "android");
        }


        //Click Button Load Url to Webview
        {
            Button buttonLoadUrl = (Button) findViewById(R.id.ButtonLoadUrl);
            buttonLoadUrl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    webView.loadUrl("https://appassets.androidplatform.net/assets/homepage.html");
                }
            });
        }



        //Click Button Load Data to Webview
        {
            Button buttonLoadData = (Button) findViewById(R.id.ButtonLoadData);
            buttonLoadData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String html = "<html><body onload=\"android.onload('document is loaded!');\"></body></html></html>";
                    webView.loadData(html, "text/html", "utf-8");
                }
            });
        }


        //Click Button Load Site
        {

            /** if your Android app use a webview to load a site from Internet then don't forget to add this permisson -
             *  <uses-permission android:name="android.permission.INTERNET" />
             *  to your manifest file.
             *  example:
             * <manifest ... >
             *     <uses-permission android:name="android.permission.INTERNET" />
             *     ...
             * </manifest>
             */


            Button buttonLoadSite = (Button) findViewById(R.id.ButtonLoadSite);
            buttonLoadSite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webView.loadUrl("https://bing.com");
                }
            });
        }

        //Click Button Load Blank content to Webview
        {
            Button buttonLoadBlank = (Button) findViewById(R.id.ButtonLoadBlank);
            buttonLoadBlank.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String my_url = "about:blank";
                    webView.loadUrl(my_url);
                }
            });
        }



        //Click Button Load Local Html File to Webview
        {
            Button buttonLoadLocalHtmlFile = (Button) findViewById(R.id.ButtonLoadLocalHtmlFile);
            buttonLoadLocalHtmlFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    webView.loadUrl("file:///android_asset/homepage.html");
                }
            });
        }


        //Click Button Run JavaScript in Webview
        {
            Button buttonRunJavaScript = (Button) findViewById(R.id.ButtonRunJavaScript);
            buttonRunJavaScript.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webView.loadUrl("javascript:android.show_app_toast('app toast called by javascript');");
                }
            });
        }


        //webClientCallback - if you don't want callback from methods of WebviewClient then set to null
        webClientCallback = null;

        //webClientCallback - to enable callback from methods of WebviewClient, add this block
        {
            webClientCallback = new WebClientCallback() {
                @Override
                public void onLoadFinish(WebView view, String url) {
                    Toast.makeText(MainActivity.this,"Page load Finished!",Toast.LENGTH_LONG).show();
                }

                @Override
                public Boolean onshouldOverrideUrlLoading(WebView view, String url) {
                    return null;
                }

                @Override
                public void onLoadError(WebView view, WebResourceRequest request, WebResourceErrorCompat error) {

                }
            };

        }


    }


    //LocalContentWebViewClient Class and implemented methods from extends androidx.webkit.WebViewClientCompat
    private class LocalContentWebViewClient extends androidx.webkit.WebViewClientCompat {
        private final androidx.webkit.WebViewAssetLoader mAssetLoader;
        LocalContentWebViewClient(WebViewAssetLoader assetLoader) {
            mAssetLoader = assetLoader;
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(webClientCallback != null){
                Boolean r = webClientCallback.onshouldOverrideUrlLoading(view,url);
                if(r !=null){
                    return r;
                }
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
        @Override
        public void onReceivedError(@NonNull WebView view, @NonNull WebResourceRequest request, @NonNull WebResourceErrorCompat error) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                super.onReceivedError(view, request, error);
            }
            if(webClientCallback != null) webClientCallback.onLoadError(view, request, error);
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(webClientCallback != null) webClientCallback.onLoadFinish(view,url);
        }
        @Override
        @RequiresApi(21)
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return mAssetLoader.shouldInterceptRequest(request.getUrl());
        }
        @Override
        @SuppressWarnings("deprecation")
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return mAssetLoader.shouldInterceptRequest(Uri.parse(url));
        }
    }

    //WebAppInterface CLASS - here is your Android app functions methods available to been called under your html/javascript
    // to create a new WebAppInterface function method:  @JavascriptInterface public void function_name (){}
    // to call your new function method on html file use this javascript : android.function_name(); - explaining (1ºthe js interface name as you named "android" declared at "Configure Webview" step at line webView.addJavascriptInterface) dot (2ºyour name function method to call)
    //
    // Examples
    //
    // -  ex. javascript code : <script>  android.show_app_toast("app toast called by javascript");  </script>
    //
    // - ex. how to make Android app execute js: webView.loadUrl("javascript:android.show_app_toast("app toast called by javascript");");

    private class WebAppInterface {
        WebAppInterface(Context c) {
        }
        @JavascriptInterface
        public void onload(String any_string_value) {

            Toast.makeText(MainActivity.this,any_string_value,Toast.LENGTH_LONG).show();

        }
        @JavascriptInterface
        public void show_app_toast(String string_toast_text) {

            Toast.makeText(MainActivity.this,string_toast_text,Toast.LENGTH_LONG).show();

        }
    }

    //WebClientCallback - creating a WebClient callback
    //for each method implemented in LocalContentWebViewClient Class
    // here you can create a new one with same name and parameters to be used to callback.
    protected interface WebClientCallback {
        void onLoadFinish(WebView view, String url);
        Boolean onshouldOverrideUrlLoading(WebView view, String url);
        void onLoadError(WebView view, WebResourceRequest request, WebResourceErrorCompat error);
    }

}