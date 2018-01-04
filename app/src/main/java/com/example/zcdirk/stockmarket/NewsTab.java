package com.example.zcdirk.stockmarket;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.zcdirk.stockmarket.data.News;

import java.util.List;

/**
 * Created by zcdirk on 11/10/17.
 */

public class NewsTab extends Fragment {
    private static LayoutInflater mInflater = null;
    static ListView listView;
    private static List<News> newsList;
    private static View rootView;
    private boolean visitedOutside = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        rootView = inflater.inflate(R.layout.news_tab, container, false);
        loadNewsTable(true);
        return rootView;
    }

    public void loadNewsTable(boolean insideCall) {
        newsList = DisplayStockActivity.getCurrentStock().getNews();
        if (insideCall) {
            if (visitedOutside) {
                ProgressBar pb = (ProgressBar) rootView.findViewById(R.id.news_progress_bar);
                pb.setVisibility(View.INVISIBLE);
                if (newsList == null || newsList.size() == 0) {
                    TextView error = (TextView) rootView.findViewById(R.id.news_error_message);
                    error.setVisibility(View.VISIBLE);
                    return;
                }
            } else {
                if (newsList == null) return;
            }
        } else {
            visitedOutside = true;
            if (rootView == null) return;
            ProgressBar pb = (ProgressBar) rootView.findViewById(R.id.news_progress_bar);
            pb.setVisibility(View.INVISIBLE);
            if (newsList == null || newsList.size() == 0) {
                TextView error = (TextView) rootView.findViewById(R.id.news_error_message);
                error.setVisibility(View.VISIBLE);
                return;
            }
        }

        listView = (ListView) rootView.findViewById(R.id.list_view);
        listView.setAdapter(new ListViewAdapter(newsList));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                News getObject = newsList.get(position);
                String link = getObject.getLink();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(browserIntent);
            }
        });
        //长按菜单显示
//        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//            public void onCreateContextMenu(ContextMenu conMenu, View view , ContextMenu.ContextMenuInfo info) {
//                conMenu.setHeaderTitle("菜单");
//                conMenu.add(0, 0, 0, "条目一");
//                conMenu.add(0, 1, 1, "条目二");
//                conMenu.add(0, 2, 2, "条目三");
//            }
//        });
    }


    public static class ListViewAdapter extends BaseAdapter {
        View[] itemViews;

        public ListViewAdapter(List<News> mlistInfo) {
            // TODO Auto-generated constructor stub
            if (mlistInfo == null) return;
            itemViews = new View[mlistInfo.size()];
            for(int i = 0; i < mlistInfo.size(); i++) {
                News getInfo = (News) mlistInfo.get(i);
                //调用makeItemView，实例化一个Item
                itemViews[i] = makeItemView(getInfo);
            }
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

        private View makeItemView(News news) {
            View itemView = mInflater.inflate(R.layout.list_items, null);
            TextView title = (TextView) itemView.findViewById(R.id.title);
            title.setText(news.getTitle());
            TextView author = (TextView) itemView.findViewById(R.id.author);
            author.setText(news.getAuthor());
            TextView date = (TextView) itemView.findViewById(R.id.date);
            date.setText(news.getDate());
            return itemView;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                return itemViews[position];
            return convertView;
        }
    }
}
