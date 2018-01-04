package com.example.zcdirk.stockmarket.data;

import java.util.List;

/**
 * Created by zcdirk on 11/11/17.
 */

public class Stock {
    private Table table = null;
    private String price = null;
    private String sma = null;
    private String ema = null;
    private String stoch = null;
    private String rsi = null;
    private String adx = null;
    private String cci = null;
    private String bbands = null;
    private String macd = null;
    private String historical = null;
    private List<News> news = null;

    public Boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }

    private Boolean favorite = false;

    public Stock() { }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSma() {
        return sma;
    }

    public void setSma(String sma) {
        this.sma = sma;
    }

    public String getEma() {
        return ema;
    }

    public void setEma(String ema) {
        this.ema = ema;
    }

    public String getStoch() {
        return stoch;
    }

    public void setStoch(String stoch) {
        this.stoch = stoch;
    }

    public String getRsi() {
        return rsi;
    }

    public void setRsi(String rsi) {
        this.rsi = rsi;
    }

    public String getAdx() {
        return adx;
    }

    public void setAdx(String adx) {
        this.adx = adx;
    }

    public String getCci() {
        return cci;
    }

    public void setCci(String cci) {
        this.cci = cci;
    }

    public String getBbands() {
        return bbands;
    }

    public void setBbands(String bbands) {
        this.bbands = bbands;
    }

    public String getMacd() {
        return macd;
    }

    public void setMacd(String macd) {
        this.macd = macd;
    }

    public String getHistorical() {
        return historical;
    }

    public void setHistorical(String historical) {
        this.historical = historical;
    }

    public List<News> getNews() {
        return news;
    }

    public void setNews(List<News> news) {
        this.news = news;
    }
}
