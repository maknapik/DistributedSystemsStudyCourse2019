package model;

import java.io.Serializable;

public class OrderRequest implements Serializable {

    String title;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

}