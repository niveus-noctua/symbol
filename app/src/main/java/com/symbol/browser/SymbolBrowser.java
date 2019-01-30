package com.symbol.browser;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.symbol.symbol.R;

public class SymbolBrowser extends AppCompatActivity {

    private WebView symbolWebView;

    private class SymbolViewClient extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symbol_browser);

        symbolWebView = findViewById(R.id.symbolWebView);

        symbolWebView.setWebViewClient(new SymbolViewClient());
        symbolWebView.getSettings().setJavaScriptEnabled(true);
        String address = getIntent().getStringExtra("address");
        symbolWebView.loadUrl(address);
    }
}
