/*
4. favorite 看是否加载完
 */

package com.example.zcdirk.stockmarket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zcdirk.stockmarket.data.Favorite;
import com.example.zcdirk.stockmarket.utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.zcdirk.stockmarket.MESSAGE";

    private AutoCompleteTextView quoteInput;

    public static List<Favorite> getFavoriteList() {
        return favoriteList;
    }

    private static List<Favorite> favoriteList;

    private List<String> sortByList = Arrays.asList("Sort by", "Default", "Symbol", "Price", "Change", "Change(%)");
    private List<String> orderList = Arrays.asList("Order", "Ascending", "Descending");

    private FavoriteListAdapter favoriteListAdapter;

    private boolean autoRefresh = false;
    private Timer timer;
    private int sortByPosition = 0, orderByPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        quoteInput = findViewById(R.id.quote_input);

        AutocompleteAdapter adapter = new AutocompleteAdapter(this, R.layout.dropdown_item);
        quoteInput.setAdapter(adapter);
        quoteInput.setThreshold(1);
        quoteInput.setTextColor(Color.WHITE);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.dropdown_item, sortByList) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0 || sortByPosition == position) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        Spinner sortSpinner = (Spinner) findViewById(R.id.sort_by_spinner);
        sortSpinner.setAdapter(spinnerAdapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                sortByPosition = position;
                if (position == 0) return;
                sortFavoriteList(sortByPosition, orderByPosition);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
        spinnerAdapter = new ArrayAdapter<String>(this, R.layout.dropdown_item, orderList) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0 || orderByPosition == position) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        Spinner orderSpinner = (Spinner) findViewById(R.id.order_spinner);
        orderSpinner.setAdapter(spinnerAdapter);
        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                orderByPosition = position;
                if (position == 0) return;
                sortFavoriteList(sortByPosition, orderByPosition);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }
        });

        setupFavoriteList();

        // set favorite list table
        ListView listView = findViewById(R.id.favorite_list);
        favoriteListAdapter = new FavoriteListAdapter(this, R.layout.favorite_list_item);
        listView.setAdapter(favoriteListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String message = favoriteList.get(position).getSymbol();
                Intent intent = new Intent(getApplicationContext(), DisplayStockActivity.class);
                intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
            }
        });

        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu conMenu, View view , ContextMenu.ContextMenuInfo info) {
                conMenu.setHeaderTitle("Remvoe from favorites?");
                conMenu.add(0, 0, 0, "No");
                conMenu.add(0, 1, 1, "Yes");
            }
        });

        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setVisibility(View.INVISIBLE);

        onRefresh(findViewById(R.id.refresh_button));

    }

    private void sortFavoriteList(int sort, int order) {
        if (order == 1) {  // ascending
            if (sort == 1 || sort == 0) { // default

            } else if (sort == 2) { // symbol
                favoriteList.sort(new Comparator<Favorite>() {
                    @Override
                    public int compare(Favorite favorite, Favorite t1) {
                        return favorite.getSymbol().compareTo(t1.getSymbol());
                    }
                });
            } else if (sort == 3) { // price
                favoriteList.sort(new Comparator<Favorite>() {
                    @Override
                    public int compare(Favorite favorite, Favorite t1) {
                        return favorite.getPrice().compareTo(t1.getPrice());
                    }
                });
            } else if (sort == 4){ // change
                favoriteList.sort(new Comparator<Favorite>() {
                    @Override
                    public int compare(Favorite favorite, Favorite t1) {
                        return favorite.getChange().compareTo(t1.getChange());
                    }
                });
            } else {
                favoriteList.sort(new Comparator<Favorite>() {
                    @Override
                    public int compare(Favorite favorite, Favorite t1) {
                        return favorite.getPercent().compareTo(t1.getPercent());
                    }
                });
            }
        } else if (order == 2) { // descending
            if (sort == 1 || sort == 0) { // default

            } else if (sort == 2) { // symbol
                favoriteList.sort(new Comparator<Favorite>() {
                    @Override
                    public int compare(Favorite favorite, Favorite t1) {
                        return -favorite.getSymbol().compareTo(t1.getSymbol());
                    }
                });
            } else if (sort == 3) { // price
                favoriteList.sort(new Comparator<Favorite>() {
                    @Override
                    public int compare(Favorite favorite, Favorite t1) {
                        return -favorite.getPrice().compareTo(t1.getPrice());
                    }
                });
            } else if (sort == 4) { // change
                favoriteList.sort(new Comparator<Favorite>() {
                    @Override
                    public int compare(Favorite favorite, Favorite t1) {
                        return -favorite.getChange().compareTo(t1.getChange());
                    }
                });
            } else {
                favoriteList.sort(new Comparator<Favorite>() {
                    @Override
                    public int compare(Favorite favorite, Favorite t1) {
                        return -favorite.getPercent().compareTo(t1.getPercent());
                    }
                });
            }
        }
        favoriteListAdapter.notifyDataSetChanged();
    }

    public boolean onContextItemSelected(MenuItem aItem) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)aItem.getMenuInfo();
        switch (aItem.getItemId()) {
            case 0:
                return true;
            case 1:
                //Toast.makeText(MainActivity.this, "delete:" + info.position,Toast.LENGTH_SHORT).show();
                favoriteList.remove(info.position);
                favoriteListAdapter.notifyDataSetChanged();
                saveFavoriteListToSharedPreference();
                return true;
        }
        return false;
    }

    private void setupFavoriteList() {
        favoriteList = new ArrayList<>();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String favString = sp.getString("fav", null);
        if (favString == null) return;
        String[] favorites = favString.split("#");
        for (String favorite: favorites) {
            if (favorite.equals("")) continue;
            String[] temp = favorite.split("@");
            favoriteList.add(new Favorite(temp[0], Double.parseDouble(temp[1]),
                    Double.parseDouble(temp[2]), Double.parseDouble(temp[3])));
        }
        // System.out.println(favoriteList);
    }

    public void onAuto(View view) {
        if (!autoRefresh) {
            autoRefresh = true;
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onRefresh(null);
                        }
                    });
                }
            }, 0, 5000);
        } else {
            autoRefresh = false;
            timer.cancel();
        }
    }

    // action after click get quote button
    public void onGetQuote(View view) {
        EditText editText = findViewById(R.id.quote_input);
        String message = editText.getText().toString().toUpperCase().split(" ")[0];
        if (!message.equals("")) {
            Intent intent = new Intent(this, DisplayStockActivity.class);
            intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);
        } else {
            Context context = getApplicationContext();
            CharSequence text = "Please enter a stock name or symbol";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    public void saveFavoriteListToSharedPreference() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        StringBuilder favString = new StringBuilder();
        for (Favorite f: favoriteList) {
            favString.append("#").append(f.getSymbol()).append("@").append(f.getPrice()).append("@");
            favString.append(f.getChange()).append("@").append(f.getPercent());
        }
        sp.edit().putString("fav", favString.toString()).apply();
    }

    // action after click clear button
    public void onClear(View view) {
        quoteInput.setText("");
    }

    // action after click refresh button
    public void onRefresh(View view) {
        try {
            ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
            pb.setVisibility(View.VISIBLE);
            JSONArray arr = new JSONArray();
            for (int i = 0; i < favoriteList.size(); i++) {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("symbol", favoriteList.get(i).getSymbol());
                arr.put(jsonParam);
            }
            NetworkUtils.Utils.execute(new FetchRefreshTask(), arr.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public class FetchRefreshTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            // If there's no input, just return
            if (params.length == 0) return null;
            String input = params[0];
            String res = "";
            try {
                URL url = new URL("http://zcstock.us-east-1.elasticbeanstalk.com/refresh");
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
                return res;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(String str) {
            System.out.println(str);
            // parse JSON here
            try {
                JSONArray array = new JSONArray(str);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject temp = array.getJSONObject(i);
                    favoriteList.get(i).setSymbol(temp.getString("symbol"));
                    favoriteList.get(i).setPrice(temp.getDouble("price"));
                    favoriteList.get(i).setChange(temp.getDouble("change"));
                    favoriteList.get(i).setPercent(temp.getDouble("percent"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            favoriteListAdapter.notifyDataSetChanged();
            saveFavoriteListToSharedPreference();
            ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
            pb.setVisibility(View.INVISIBLE);
        }
    }


    public class FavoriteListAdapter extends ArrayAdapter  {

        public FavoriteListAdapter(Context context, int resource) {
            super(context, resource);
        }
        @Override
        public int getCount() {
            return favoriteList.size();
        }

        @Override
        public Favorite getItem(int position) {
            return favoriteList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.favorite_list_item, parent,false);
            Favorite temp = favoriteList.get(position);
            TextView symbol = (TextView) view.findViewById(R.id.fav_symbol);
            symbol.setText(temp.getSymbol());
            TextView price = (TextView) view.findViewById(R.id.fav_price);
            price.setText(Double.toString(temp.getPrice()));
            TextView change = (TextView) view.findViewById(R.id.fav_change);
            change.setText(temp.getChange() + "(" + temp.getPercent() + "%)");
            if (temp.getChange() < 0) {
                change.setTextColor(Color.RED);
            }
            return view;
        }
    }

    public class AutocompleteAdapter extends ArrayAdapter implements Filterable {
        private List<String> mData;
        private String AUTO_URL = "http://zcstock.us-east-1.elasticbeanstalk.com/auto/";

        public AutocompleteAdapter(Context context, int resource) {
            super(context, resource);
            mData = new ArrayList<>();
        }
        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public String getItem(int position) {
            return mData.get(position);
        }

        @Override
        public Filter getFilter() {
            Filter myFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if(constraint != null){
                        try{
                            //get data from the web
                            String term = constraint.toString();
                            mData = NetworkUtils.Utils.excuteAndGet(new DownloadAutoData(), term);
                        }catch (Exception e){
                            Log.d("HUS","EXCEPTION "+e);
                        }
                        filterResults.values = mData;
                        filterResults.count = mData.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if(results != null && results.count > 0){
                        notifyDataSetChanged();
                    }else{
                        notifyDataSetInvalidated();
                    }
                }
            };

            return myFilter;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.dropdown_item, parent,false);
            TextView itemText = (TextView) view.findViewById(R.id.itemText);
            itemText.setText(mData.get(position));
            return view;
        }

        //download autocomplete data list
        private class DownloadAutoData extends AsyncTask<String, Void, ArrayList<String>> {

            @Override
            protected ArrayList doInBackground(String... params) {
                try {
                    //Create a new COUNTRY SEARCH url Ex "search.php?term=india"
                    String NEW_URL = AUTO_URL + URLEncoder.encode(params[0],"UTF-8");

                    URL url = new URL(NEW_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = reader.readLine()) != null){
                        sb.append(line).append("\n");
                    }

                    //parse JSON and store it in the list
                    String jsonString =  sb.toString();
                    ArrayList list = new ArrayList<>();

                    JSONArray jsonArray = new JSONArray(jsonString);
                    for (int i = 0; i < jsonArray.length() && i < 5; i++) {
                        JSONObject temp = jsonArray.getJSONObject(i);
                        list.add(temp.getString("Symbol") + " - " + temp.getString("Name") + " ("
                                + temp.getString("Exchange") + ")");
                    }
                    return list;
                } catch (Exception e) {
                    Log.d("HUS", "EXCEPTION " + e);
                    return null;
                }
            }
        }
    }

}
