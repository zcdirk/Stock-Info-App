package com.example.zcdirk.stockmarket.utilities;

import com.example.zcdirk.stockmarket.data.News;
import com.example.zcdirk.stockmarket.data.Stock;
import com.example.zcdirk.stockmarket.data.Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by zcdirk on 11/11/17.
 */

public final class JsonParser {
    public static void convertTable(String jsonString, Stock s) throws JSONException {
        if (s == null) s = new Stock();
        JSONObject jsonObject = new JSONObject(jsonString);

        /* Is there an error? */
        if (jsonObject.has("error")) {
            return;
        }

        // set historical chart
        String historical = jsonObject.getString("historical");
        historical = historical.replace("{\"type\":\"week\",\"count\":1,\"text\":\"1w\"},", "");
        historical = historical.replace("{\"type\":\"ytd\",\"text\":\"YTD\"},", "");
        s.setHistorical(historical);

        // set price chart
        s.setPrice(jsonObject.getString("price"));

        // parse the table here
        JSONObject tableObject = jsonObject.getJSONObject("table");
        Table table = new Table();
        table.setSymbol(tableObject.getString("symbol"));
        table.setPrice(tableObject.getDouble("price"));
        table.setOpen(tableObject.getDouble("open"));
        table.setRange(tableObject.getString("range"));
        table.setVolume(tableObject.getLong("volume"));
        table.setChange(tableObject.getDouble("change"));
        table.setPercent(tableObject.getDouble("percent"));
        table.setTime(tableObject.getString("time"));
        table.setClose(tableObject.getDouble("close"));
        s.setTable(table);
    }

    public static void convertNews(String jsonString, Stock s) throws JSONException {
        if (s == null) s = new Stock();
        s.setNews(new ArrayList<News>());
        JSONArray jsonArray = new JSONArray(jsonString);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject temp = jsonArray.getJSONObject(i);
            News news = new News(temp.getString("title"), temp.getString("author"),
                    temp.getString("date"), temp.getString("link"));
            s.getNews().add(news);
        }
    }

    public static String[] convertAutoComplete(String jsonString) throws JSONException {
        if (jsonString.equals("") || jsonString.equals("[]")) return new String[]{};
        JSONArray jsonArray = new JSONArray(jsonString);
        String[] res = new String[Math.min(jsonArray.length(), 5)];
        for (int i = 0; i < jsonArray.length() && i < 5; i++) {
            JSONObject temp = jsonArray.getJSONObject(i);
            res[i] = temp.getString("Symbol") + " - " + temp.getString("Name") + " ("
                    + temp.getString("Exchange") + ")";
        }
        return res;
    }
}
