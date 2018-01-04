package com.example.zcdirk.stockmarket;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.zcdirk.stockmarket.data.Favorite;
import com.example.zcdirk.stockmarket.data.Table;

/**
 * Created by zcdirk on 11/10/17.
 */

public class CurrentTab extends Fragment {
    private static LayoutInflater mInflater = null;
    static ListView listView;
    private static View rootView;
    private static Table mTable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        rootView = inflater.inflate(R.layout.current_tab, container, false);

        WebView chartWeb = (WebView) rootView.findViewById(R.id.chart);
        // enable javascript here
        WebSettings webSettings = chartWeb.getSettings();
        webSettings.setJavaScriptEnabled(true);

        loadTabl();

        final Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner2);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                Button changeButton = (Button) rootView.findViewById(R.id.button3);
                if (DisplayStockActivity.getCurrentIndicator().equals(spinner.getSelectedItem().toString())) {
                    changeButton.setEnabled(false);
                } else {
                    changeButton.setEnabled(true);
                }
                // check whether you should disable change button here
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });


        for (Favorite t: MainActivity.getFavoriteList()) {
            if (t.getSymbol().equals(DisplayStockActivity.getQuoteInput())) {
                DisplayStockActivity.getCurrentStock().setFavorite(true);
                Button favButton = (Button) rootView.findViewById(R.id.button2);
                favButton.setBackgroundResource(R.drawable.filled);
            }
        }

        return rootView;
    }

    public static void loadTabl() {
        mTable = DisplayStockActivity.getCurrentStock().getTable();
        if (mTable == null || rootView == null) return;
        listView = (ListView) rootView.findViewById(R.id.table_view);
        listView.setAdapter(new TableViewAdapter(mTable));
    }


    public static class TableViewAdapter extends BaseAdapter {
        View[] itemViews;

        public TableViewAdapter(Table table) {

            if (table == null) return;
            itemViews = new View[8];

            // symbol
            View symbol = mInflater.inflate(R.layout.table_items, null);
            TextView tag = (TextView) symbol.findViewById(R.id.tag);
            tag.setText("Stock Symbol");
            TextView value = (TextView) symbol.findViewById(R.id.value);
            value.setText(table.getSymbol());
            itemViews[0] = symbol;

            // price
            View price = mInflater.inflate(R.layout.table_items, null);
            tag = (TextView) price.findViewById(R.id.tag);
            tag.setText("Last Price");
            value = (TextView) price.findViewById(R.id.value);
            value.setText(table.getPrice().toString());
            itemViews[1] = price;

            // change
            View change = mInflater.inflate(R.layout.table_items, null);
            tag = (TextView) change.findViewById(R.id.tag);
            tag.setText("Change");
            value = (TextView) change.findViewById(R.id.value);
            value.setText(table.getChange() + "(" + table.getPercent() + "%)");
            // TODO: change the position of the arrow
            if (table.getChange() < 0) {
                change.findViewById(R.id.down).setVisibility(View.VISIBLE);
            } else {
                change.findViewById(R.id.up).setVisibility(View.VISIBLE);
            }
            itemViews[2] = change;

            // time
            View time = mInflater.inflate(R.layout.table_items, null);
            tag = (TextView) time.findViewById(R.id.tag);
            tag.setText("Timestamp");
            value = (TextView) time.findViewById(R.id.value);
            value.setText(table.getTime());
            itemViews[3] = time;

            // open
            View open = mInflater.inflate(R.layout.table_items, null);
            tag = (TextView) open.findViewById(R.id.tag);
            tag.setText("Open");
            value = (TextView) open.findViewById(R.id.value);
            value.setText(table.getOpen().toString());
            itemViews[4] = open;

            // close
            View close = mInflater.inflate(R.layout.table_items, null);
            tag = (TextView) close.findViewById(R.id.tag);
            tag.setText("Close");
            value = (TextView) close.findViewById(R.id.value);
            value.setText(table.getClose().toString());
            itemViews[5] = close;

            // range
            View range = mInflater.inflate(R.layout.table_items, null);
            tag = (TextView) range.findViewById(R.id.tag);
            tag.setText("Day's Range");
            value = (TextView) range.findViewById(R.id.value);
            value.setText(table.getRange());
            itemViews[6] = range;

            // volume
            View volume = mInflater.inflate(R.layout.table_items, null);
            tag = (TextView) volume.findViewById(R.id.tag);
            tag.setText("Volume");
            value = (TextView) volume.findViewById(R.id.value);
            value.setText(table.getVolume().toString());
            itemViews[7] = volume;

            // TODO Auto-generated constructor stub
//            itemViews = new View[8];
//            for(int i = 0; i < 8; i++) {
//                //News getInfo = (News) mlistInfo.get(i);
//                //调用makeItemView，实例化一个Item
//                itemViews[i] = makeItemView();
//            }
        }

        private View makeItemView() {
            View itemView = mInflater.inflate(R.layout.table_items, null);
            TextView tag = (TextView) itemView.findViewById(R.id.tag);
            tag.setText("111");
            TextView value = (TextView) itemView.findViewById(R.id.value);
            value.setText("222");
            return itemView;
        }

        public int getCount() {
            return itemViews.length;
        }

        public View getItem(int position) {
            return itemViews[position];
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                return itemViews[position];
            return convertView;
        }
    }
}
