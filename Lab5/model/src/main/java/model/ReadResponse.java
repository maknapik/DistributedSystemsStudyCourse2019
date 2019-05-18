package model;

import java.io.Serializable;

public class ReadResponse implements Serializable {

    private String payload = "";

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }

}