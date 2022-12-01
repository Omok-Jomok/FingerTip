package com.example.fingertip;

public class SearchRecordItem {
    String product;
    int month;
    int day;

    public SearchRecordItem(){}

    public String getProduct() {
        return product;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public SearchRecordItem(String product, int month, int day){
        this.product = product;
        this.month = month;
        this.day = day;
    }
}
