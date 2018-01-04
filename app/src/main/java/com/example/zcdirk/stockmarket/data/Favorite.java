package com.example.zcdirk.stockmarket.data;

/**
 * Created by zcdirk on 11/19/17.
 */

public class Favorite {
    private String symbol;
    private Double price;
    private Double change;

    public Favorite(String symbol, Double price, Double change, Double percent) {
        this.symbol = symbol;
        this.price = price;
        this.change = change;
        this.percent = percent;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getChange() {
        return change;
    }

    public void setChange(Double change) {
        this.change = change;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    private Double percent;
}
