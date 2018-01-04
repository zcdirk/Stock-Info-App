package com.example.zcdirk.stockmarket.data;

/**
 * Created by zcdirk on 11/11/17.
 */

public class News {
    private String title;
    private String author;
    private String date;
    private String link;

    public News(String title, String author, String date, String link) {
        this.title = title;
        this.author = author;
        this.date = date;
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
