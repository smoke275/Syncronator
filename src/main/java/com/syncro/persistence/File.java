package com.syncro.persistence;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Serialization class for GSON and retrofit
 */
public class File {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("location")
    @Expose
    private String location;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}