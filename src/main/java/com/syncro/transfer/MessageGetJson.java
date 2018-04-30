package com.syncro.transfer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * GSON class for retrieving json
 */
public class MessageGetJson {

    public static final String EVENT = "get_json";

    @SerializedName("mac_id")
    @Expose
    private String macId;

    /**
     * No args constructor for use in serialization
     *
     */
    public MessageGetJson() {
    }

    /**
     *
     * @param macId
     */
    public MessageGetJson(String macId) {
        super();
        this.macId = macId;
    }

    public String getMacId() {
        return macId;
    }

    public void setMacId(String macId) {
        this.macId = macId;
    }

    public MessageGetJson withMacId(String macId) {
        this.macId = macId;
        return this;
    }

}