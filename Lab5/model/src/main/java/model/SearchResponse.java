package model;

import java.io.Serializable;

public class SearchResponse implements Serializable {

    private double price;

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

}
