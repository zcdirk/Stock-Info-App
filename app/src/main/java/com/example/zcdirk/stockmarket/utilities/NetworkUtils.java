package com.example.zcdirk.stockmarket.utilities;
/**
 * Created by zcdirk on 11/10/17.
 */

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String BASE_URL = "http://zcstock.us-east-1.elasticbeanstalk.com";

    /**
     * Builds the auto complete URL
     *
     * @param input The auto complete string
     * @return The URL to use to get auto complete data.
     */
    public static URL getAutoCompleteUrl(String input) {
        URL url = null;
        try {
            url = new URL(BASE_URL + "/auto/" + input);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "Built URI " + url);
        return url;
    }

    /**
     * Builds the get quote url.
     *
     * @param type The type of the indicator
     * @param input The input string
     * @return The URL to use to query the data.
     */
    public static URL getQuoteUrl(String type, String input) {
        URL url = null;
        try {
            url = new URL(BASE_URL + "/getstock/" + type + "/" + input);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "Built URI " + url);
        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static class Utils {

        @SuppressLint("NewApi")
        public static <P, T extends AsyncTask<P, ?, ?>> void execute(T task, P... params) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
            } else {
                task.execute(params);
            }
        }
        public static <P, T extends AsyncTask<P, ?, ?>> ArrayList excuteAndGet(T task, P... params) throws ExecutionException, InterruptedException {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                return (ArrayList)task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params).get();
            } else {
                return (ArrayList)task.execute(params).get();
            }
        }
    }
}