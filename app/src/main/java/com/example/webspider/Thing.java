package com.example.webspider;

public class Thing {

    public Thing(String item, String address) {
        this.item = item;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private String item;

    private String address;

    public void setItem(String item) {
        this.item = item;
    }

    public String getItem() {

        return item;
    }
}