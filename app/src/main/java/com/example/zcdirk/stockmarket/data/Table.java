package com.example.zcdirk.stockmarket.data;

/**
 * Created by zcdirk on 11/11/17.
 */

public class Table {
    private String symbol = null;
    private Double price = null;
    private Double open = null;
    private String range = null;
    private Long volume = null;
    private Double change = null;
    private Double percent = null;
    private String time = null;
    private Double close = null;

    public Table() {}

//    public Table(String symbol, Double price, Double open,
//                 String range, Long volume, Double change,
//                 Double percent, String time, Double close) {
//        this.symbol = symbol;
//        this.price = price;
//        this.open = open;
//        this.range = range;
//        this.volume = volume;
//        this.change = change;
//        this.percent = percent;
//        this.time = time;
//        this.close = close;
//    }

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

    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }
}
