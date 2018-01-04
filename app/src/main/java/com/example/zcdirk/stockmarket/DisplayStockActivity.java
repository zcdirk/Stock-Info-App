package com.example.zcdirk.stockmarket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zcdirk.stockmarket.data.Favorite;
import com.example.zcdirk.stockmarket.data.Stock;
import com.example.zcdirk.stockmarket.data.Table;
import com.example.zcdirk.stockmarket.utilities.JsonParser;
import com.example.zcdirk.stockmarket.utilities.NetworkUtils;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class DisplayStockActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private WebView chartWeb;

    private static CurrentTab ct;
    private static HistoricalTab ht;
    private static NewsTab nt;

    public static String getQuoteInput() {
        return quoteInput;
    }

    private static String quoteInput;

    private final String chartHTML1 = "<script src='https://code.highcharts.com/highcharts.js'></script><div id='container'></div><script type='text/javascript' async>Highcharts.chart('container',";
    private final String chartHTML2 = ");</script>";

    private final String historicalHTML1 = "<script src='https://code.highcharts.com/stock/highstock.js'></script>" +
            "<script src='https://code.highcharts.com/stock/modules/exporting.js'></script>" +
            "<div id='container'></div><script type='text/javascript' async>Highcharts.stockChart('container',";
    private final String historicalHTML2 = ");</script>";

    private static Stock currentStock = null;
    private static String currentIndicator = "Price";
    public static Stock getCurrentStock() {
        return currentStock;
    }
    public static String getCurrentIndicator() {
        return currentIndicator;
    }

    private FetchTableTask fetchTableTask;
    private FetchSmaTask fetchSmaTask;
    private FetchEmaTask fetchEmaTask;
    private FetchMacdTask fetchMacdTask;
    private FetchBbandsTask fetchBbandsTask;
    private FetchCciTask fetchCciTask;
    private FetchAdxTask fetchAdxTask;
    private FetchRsiTask fetchRsiTask;
    private FetchStochTask fetchStochTask;

    private CallbackManager callbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_stock);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        ct = new CurrentTab();
        ht = new HistoricalTab();
        nt = new NewsTab();

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        quoteInput = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        currentStock = new Stock();
        setTitle(quoteInput);

        fetchTableTask = new FetchTableTask();
        NetworkUtils.Utils.execute(fetchTableTask, quoteInput);
        fetchSmaTask = new FetchSmaTask();
        NetworkUtils.Utils.execute(fetchSmaTask, quoteInput);
        fetchEmaTask = new FetchEmaTask();
        NetworkUtils.Utils.execute(fetchEmaTask, quoteInput);
        fetchStochTask = new FetchStochTask();
        NetworkUtils.Utils.execute(fetchStochTask, quoteInput);
        fetchRsiTask = new FetchRsiTask();
        NetworkUtils.Utils.execute(fetchRsiTask, quoteInput);
        fetchAdxTask = new FetchAdxTask();
        NetworkUtils.Utils.execute(fetchAdxTask, quoteInput);
        fetchCciTask = new FetchCciTask();
        NetworkUtils.Utils.execute(fetchCciTask, quoteInput);
        fetchBbandsTask = new FetchBbandsTask();
        NetworkUtils.Utils.execute(fetchBbandsTask, quoteInput);
        fetchMacdTask = new FetchMacdTask();
        NetworkUtils.Utils.execute(fetchMacdTask, quoteInput);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // get the table data
    public class FetchTableTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            currentStock = new Stock();
        }
        @Override
        protected Void doInBackground(String... params) {
            // If there's no input, just return
            if (params.length == 0) return null;
            String input = params[0];
            try {
                // deal with the table part
                URL tableUrl = NetworkUtils.getQuoteUrl("table", input);
                String tableResponse = NetworkUtils.getResponseFromHttpUrl(tableUrl);
                JsonParser.convertTable(tableResponse, currentStock);

                // news
                URL newsUrl = NetworkUtils.getQuoteUrl("news", input);
                String newsResponce = NetworkUtils.getResponseFromHttpUrl(newsUrl);
                JsonParser.convertNews(newsResponce, currentStock);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // load table
            ProgressBar pb = (ProgressBar) mViewPager.findViewById(R.id.table_progress_bar);
            pb.setVisibility(View.INVISIBLE);
            if (currentStock.getTable() != null) {
                CurrentTab.loadTabl();
            } else {
                TextView error = (TextView) mViewPager.findViewById(R.id.table_error_message);
                error.setVisibility(View.VISIBLE);
            }

            // load price chart
            if (currentIndicator.equals("Price")) {
                pb = (ProgressBar) mViewPager.findViewById(R.id.chart_progress_bar);
                pb.setVisibility(View.INVISIBLE);
                if (currentStock.getPrice() != null) {
                    chartWeb = (WebView)mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getPrice() + chartHTML2, "text/html", "utf-8");
                } else {
                    TextView error = (TextView) mViewPager.findViewById(R.id.chart_error_message);
                    error.setVisibility(View.VISIBLE);
                }
            }

            // laod historical chart
            pb = (ProgressBar) mViewPager.findViewById(R.id.historical_progress_bar);
            pb.setVisibility(View.INVISIBLE);
            if (currentStock.getHistorical() != null) {
                WebView historicalWeb = (WebView) mViewPager.findViewById(R.id.historical);
                historicalWeb.loadData(historicalHTML1 + currentStock.getHistorical() + historicalHTML2, "text/html", "utf-8");
            } else {
                TextView error = (TextView) mViewPager.findViewById(R.id.historical_error_message);
                error.setVisibility(View.VISIBLE);
            }

            if (nt != null) {
                nt.loadNewsTable(false);
            }
        }
    }

    // get the macd data
    public class FetchMacdTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            // If there's no input, just return
            if (params.length == 0) return null;
            String input = params[0];
            try {
                // macd
                URL macdUrl = NetworkUtils.getQuoteUrl("macd", input);
                return NetworkUtils.getResponseFromHttpUrl(macdUrl);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String s) {
            currentStock.setMacd(s);
            if (currentIndicator.equals("MACD")) {
                ProgressBar pb = (ProgressBar) mViewPager.findViewById(R.id.chart_progress_bar);
                pb.setVisibility(View.INVISIBLE);
                if (currentStock.getMacd() != null) {
                    chartWeb = (WebView)mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getMacd() + chartHTML2, "text/html", "utf-8");
                } else {
                    TextView error = (TextView) mViewPager.findViewById(R.id.chart_error_message);
                    error.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    // get the bbands data
    public class FetchBbandsTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            // If there's no input, just return
            if (params.length == 0) return null;
            String input = params[0];
            try {
                // bbands
                URL bbandsUrl = NetworkUtils.getQuoteUrl("bbands", input);
                return NetworkUtils.getResponseFromHttpUrl(bbandsUrl);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String s) {
            currentStock.setBbands(s);
            if (currentIndicator.equals("BBANDS")) {
                ProgressBar pb = (ProgressBar) mViewPager.findViewById(R.id.chart_progress_bar);
                pb.setVisibility(View.INVISIBLE);
                if (currentStock.getBbands() != null) {
                    chartWeb = (WebView)mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getBbands() + chartHTML2, "text/html", "utf-8");
                } else {
                    TextView error = (TextView) mViewPager.findViewById(R.id.chart_error_message);
                    error.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    // get the cci data
    public class FetchCciTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            // If there's no input, just return
            if (params.length == 0) return null;
            String input = params[0];
            try {
                // cci
                URL cciUrl = NetworkUtils.getQuoteUrl("cci", input);
                return NetworkUtils.getResponseFromHttpUrl(cciUrl);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String s) {
            currentStock.setCci(s);
            if (currentIndicator.equals("CCI")) {
                ProgressBar pb = (ProgressBar) mViewPager.findViewById(R.id.chart_progress_bar);
                pb.setVisibility(View.INVISIBLE);
                if (currentStock.getCci() != null) {
                    chartWeb = (WebView)mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getCci() + chartHTML2, "text/html", "utf-8");
                } else {
                    TextView error = (TextView) mViewPager.findViewById(R.id.chart_error_message);
                    error.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    // get the adx data
    public class FetchAdxTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            // If there's no input, just return
            if (params.length == 0) return null;
            String input = params[0];
            try {
                // adx
                URL adxUrl = NetworkUtils.getQuoteUrl("adx", input);
                return NetworkUtils.getResponseFromHttpUrl(adxUrl);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String s) {
            currentStock.setAdx(s);
            if (currentIndicator.equals("ADX")) {
                ProgressBar pb = (ProgressBar) mViewPager.findViewById(R.id.chart_progress_bar);
                pb.setVisibility(View.INVISIBLE);
                if (currentStock.getAdx() != null) {
                    chartWeb = (WebView)mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getAdx() + chartHTML2, "text/html", "utf-8");
                } else {
                    TextView error = (TextView) mViewPager.findViewById(R.id.chart_error_message);
                    error.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    // get the rsi data
    public class FetchRsiTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            // If there's no input, just return
            if (params.length == 0) return null;
            String input = params[0];
            try {
                // rsi
                URL rsiUrl = NetworkUtils.getQuoteUrl("rsi", input);
                return NetworkUtils.getResponseFromHttpUrl(rsiUrl);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String s) {
            currentStock.setRsi(s);
            if (currentIndicator.equals("RSI")) {
                ProgressBar pb = (ProgressBar) mViewPager.findViewById(R.id.chart_progress_bar);
                pb.setVisibility(View.INVISIBLE);
                if (currentStock.getRsi() != null) {
                    chartWeb = (WebView)mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getRsi() + chartHTML2, "text/html", "utf-8");
                } else {
                    TextView error = (TextView) mViewPager.findViewById(R.id.chart_error_message);
                    error.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    // get the stoch data
    public class FetchStochTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            // If there's no input, just return
            if (params.length == 0) return null;
            String input = params[0];
            try {
                // stoch
                URL stochUrl = NetworkUtils.getQuoteUrl("stoch", input);
                return NetworkUtils.getResponseFromHttpUrl(stochUrl);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String s) {
            currentStock.setStoch(s);
            if (currentIndicator.equals("STOCH")) {
                ProgressBar pb = (ProgressBar) mViewPager.findViewById(R.id.chart_progress_bar);
                pb.setVisibility(View.INVISIBLE);
                if (currentStock.getStoch() != null) {
                    chartWeb = (WebView)mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getStoch() + chartHTML2, "text/html", "utf-8");
                } else {
                    TextView error = (TextView) mViewPager.findViewById(R.id.chart_error_message);
                    error.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    // get the sma data
    public class FetchSmaTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            // If there's no input, just return
            if (params.length == 0) return null;
            String input = params[0];
            try {
                // sma
                URL smaUrl = NetworkUtils.getQuoteUrl("sma", input);
                return NetworkUtils.getResponseFromHttpUrl(smaUrl);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String s) {
            currentStock.setSma(s);
            if (currentIndicator.equals("SMA")) {
                ProgressBar pb = (ProgressBar) mViewPager.findViewById(R.id.chart_progress_bar);
                pb.setVisibility(View.INVISIBLE);
                if (currentStock.getSma() != null) {
                    chartWeb = (WebView)mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getSma() + chartHTML2, "text/html", "utf-8");
                } else {
                    TextView error = (TextView) mViewPager.findViewById(R.id.chart_error_message);
                    error.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    // get the ema data
    public class FetchEmaTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            // If there's no input, just return
            if (params.length == 0) return null;
            String input = params[0];
            try {
                // ema
                URL emaUrl = NetworkUtils.getQuoteUrl("ema", input);
                return NetworkUtils.getResponseFromHttpUrl(emaUrl);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String s) {
            currentStock.setEma(s);
            if (currentIndicator.equals("EMA")) {
                ProgressBar pb = (ProgressBar) mViewPager.findViewById(R.id.chart_progress_bar);
                pb.setVisibility(View.INVISIBLE);
                if (currentStock.getEma() != null) {
                    chartWeb = (WebView)mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getEma() + chartHTML2, "text/html", "utf-8");
                } else {
                    TextView error = (TextView) mViewPager.findViewById(R.id.chart_error_message);
                    error.setVisibility(View.VISIBLE);
                }
            }
        }
    }


    public class FetchShareUrl extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            LoginManager.getInstance().logOut();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            // If there's no input, just return
            if (params.length == 0) return null;
            String input = "{\"async\": true,\"type\": \"png\",\"width\": 400,\"options\":" + params[0] + "}";
            String res = "";
            try {
                URL url = new URL("http://export.highcharts.com/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept","application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(input);

                os.flush();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        res += line;
                    }
                } else {
                    res="";
                }
                conn.disconnect();
                return "http://export.highcharts.com/" + res;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }
        @Override
        protected void onPostExecute(String s) {
            ShareDialog shareDialog = new ShareDialog(DisplayStockActivity.this);
            callbackManager = CallbackManager.Factory.create();
            shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                @Override
                public void onSuccess(Sharer.Result result) {
                    Context context = getApplicationContext();
                    CharSequence text = "Posted Successfully";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                @Override
                public void onCancel() {
                    Context context = getApplicationContext();
                    CharSequence text = "Not Post";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                @Override
                public void onError(FacebookException error) {
                    Context context = getApplicationContext();
                    CharSequence text = "Post Error";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            });

            if (ShareDialog.canShow(ShareLinkContent.class)) {
                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(s))
                        .build();
                shareDialog.show(linkContent);
            }
        }
    }

    public void onFacebookShare(View view) {
        switch (currentIndicator) {
            case "Price":
                NetworkUtils.Utils.execute(new FetchShareUrl(), currentStock.getPrice());
                break;
            case "EMA":
                NetworkUtils.Utils.execute(new FetchShareUrl(), currentStock.getEma());
                break;
            case "ADX":
                NetworkUtils.Utils.execute(new FetchShareUrl(), currentStock.getAdx());
                break;
            case "BBANDS":
                NetworkUtils.Utils.execute(new FetchShareUrl(), currentStock.getBbands());
                break;
            case "SMA":
                NetworkUtils.Utils.execute(new FetchShareUrl(), currentStock.getSma());
                break;
            case "STOCH":
                NetworkUtils.Utils.execute(new FetchShareUrl(), currentStock.getStoch());
                break;
            case "RSI":
                NetworkUtils.Utils.execute(new FetchShareUrl(), currentStock.getRsi());
                break;
            case "CCI":
                NetworkUtils.Utils.execute(new FetchShareUrl(), currentStock.getCci());
                break;
            case "MACD":
                NetworkUtils.Utils.execute(new FetchShareUrl(), currentStock.getMacd());
                break;
        }
    }

    public void onFavorite(View view) {
        if (currentStock.getTable() == null) return;
        if (currentStock.getFavorite()) {
            Button favButton = (Button) findViewById(R.id.button2);
            favButton.setBackgroundResource(R.drawable.empty);
            List<Favorite> fList = MainActivity.getFavoriteList();
            int size = fList.size();
            for (int i = 0; i < size; i++) {
                if (fList.get(i).getSymbol().equals(quoteInput)) {
                    fList.remove(i);
                    break;
                }
            }
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            StringBuilder favString = new StringBuilder();
            for (Favorite f: MainActivity.getFavoriteList()) {
                favString.append("#" + f.getSymbol() + "@" + f.getPrice() + "@");
                favString.append(f.getChange() + "@" + f.getPercent());
            }
            sp.edit().putString("fav", favString.toString()).commit();
            currentStock.setFavorite(false);
        } else {
            Button favButton = (Button) findViewById(R.id.button2);
            favButton.setBackgroundResource(R.drawable.filled);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String favString = sp.getString("fav", "");
            Table table = currentStock.getTable();
            MainActivity.getFavoriteList().add(new Favorite(table.getSymbol(), table.getPrice(),
                    table.getChange(), table.getPercent()));
            favString += "#" + table.getSymbol() + "@" + table.getPrice() + "@"
                    + table.getChange() + "@" + table.getPercent();
            sp.edit().putString("fav", favString).commit();
            currentStock.setFavorite(true);
        }
    }

    public void onChange(View view) {
        Spinner sp = (Spinner)findViewById(R.id.spinner2);
        String tag = sp.getSelectedItem().toString();
        currentIndicator = tag;
        Button changeButton = (Button) findViewById(R.id.button3);
        changeButton.setEnabled(false);

        chartWeb = (WebView)mViewPager.findViewById(R.id.chart);
        chartWeb.loadData("", "text/html", "utf-8");
        TextView error = (TextView) mViewPager.findViewById(R.id.chart_error_message);
        error.setVisibility(View.INVISIBLE);
        ProgressBar progressBar = (ProgressBar) mViewPager.findViewById(R.id.chart_progress_bar);
        progressBar.setVisibility(View.INVISIBLE);

        if (tag.equals("Price")) {
            if (currentStock.getPrice() != null) {
                if (currentStock.getPrice().length() < 600) {
                    error.setVisibility(View.VISIBLE);
                } else {
                    chartWeb = (WebView)mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getPrice() + chartHTML2, "text/html", "utf-8");
                }
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        } else if (tag.equals("SMA")) {
            if (currentStock.getSma() != null) {
                if (currentStock.getSma().length() < 600) {
                    error.setVisibility(View.VISIBLE);
                } else {
                    chartWeb = (WebView) mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getSma() + chartHTML2, "text/html", "utf-8");
                }
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        } else if (tag.equals("EMA")) {
            if (currentStock.getEma() != null) {
                if (currentStock.getEma().length() < 600) {
                    error.setVisibility(View.VISIBLE);
                } else {
                    chartWeb = (WebView) mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getEma() + chartHTML2, "text/html", "utf-8");
                }
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        } else if (tag.equals("STOCH")) {
            if (currentStock.getStoch() != null) {
                if (currentStock.getStoch().length() < 600) {
                    error.setVisibility(View.VISIBLE);
                } else {
                    chartWeb = (WebView) mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getStoch() + chartHTML2, "text/html", "utf-8");
                }
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        } else if (tag.equals("RSI")) {
            if (currentStock.getRsi() != null) {
                if (currentStock.getRsi().length() < 600) {
                    error.setVisibility(View.VISIBLE);
                } else {
                    chartWeb = (WebView) mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getRsi() + chartHTML2, "text/html", "utf-8");
                }
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        } else if (tag.equals("ADX")) {
            if (currentStock.getAdx() != null) {
                if (currentStock.getAdx().length() < 600) {
                    error.setVisibility(View.VISIBLE);
                } else {
                    chartWeb = (WebView) mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getAdx() + chartHTML2, "text/html", "utf-8");
                }
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        } else if (tag.equals("CCI")) {
            if (currentStock.getCci() != null) {
                if (currentStock.getCci().length() < 600) {
                    error.setVisibility(View.VISIBLE);
                } else {
                    chartWeb = (WebView) mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getCci() + chartHTML2, "text/html", "utf-8");
                }
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        } else if (tag.equals("BBANDS")) {
            if (currentStock.getBbands() != null) {
                if (currentStock.getBbands().length() < 600) {
                    error.setVisibility(View.VISIBLE);
                } else {
                    chartWeb = (WebView) mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getBbands() + chartHTML2, "text/html", "utf-8");
                }
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        } else if (tag.equals("MACD")) {
            if (currentStock.getMacd() != null) {
                if (currentStock.getMacd().length() < 600) {
                    error.setVisibility(View.VISIBLE);
                } else {
                    chartWeb = (WebView) mViewPager.findViewById(R.id.chart);
                    chartWeb.loadData(chartHTML1 + currentStock.getMacd() + chartHTML2, "text/html", "utf-8");
                }
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (ct == null) ct = new CurrentTab();
                    return ct;
                case 1:
                    if (ht == null) ht = new HistoricalTab();
                    return ht;
                case 2:
                    if (nt == null) nt = new NewsTab();
                    return nt;
                default:
                    return null;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {}

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Current";
                case 1:
                    return "Historical";
                case 2:
                    return "News";
            }
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fetchTableTask.cancel(true);
        fetchSmaTask.cancel(true);
        fetchEmaTask.cancel(true);
        fetchMacdTask.cancel(true);
        fetchBbandsTask.cancel(true);
        fetchCciTask.cancel(true);
        fetchAdxTask.cancel(true);
        fetchRsiTask.cancel(true);
        fetchStochTask.cancel(true);
        currentIndicator = "Price";
    }
}
