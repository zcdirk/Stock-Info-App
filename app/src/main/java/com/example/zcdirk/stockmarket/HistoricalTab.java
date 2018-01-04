package com.example.zcdirk.stockmarket;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Created by zcdirk on 11/10/17.
 */

public class HistoricalTab extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.historical_tab, container, false);

        WebView historicalWeb = (WebView) rootView.findViewById(R.id.historical);

        // enable javascript here
        WebSettings webSettings = historicalWeb.getSettings();
        webSettings.setJavaScriptEnabled(true);

        return rootView;
    }
}
