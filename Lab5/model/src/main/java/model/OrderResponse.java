package model;

import java.io.Serializable;

public class OrderResponse implements Serializable {

    String payload;

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}