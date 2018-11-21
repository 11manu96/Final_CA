package edu.rice.comp504.model.res;

import com.google.gson.Gson;

/**
 * Abstract class of response warping the message sending from server to client.
 */
public abstract class AResponse {
    private  String type;   // The type of the response, use class name to denote type
    private long timestamp; // The time when response is being created

    /**
     * Constructor.
     * @param type the type of the response, i.e. the name of class
     */
    public AResponse(String type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Convert the object to json string.
     * @return the json encoding of object itself
     */
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
