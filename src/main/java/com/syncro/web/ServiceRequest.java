package com.syncro.web;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServiceRequest {

    @SerializedName("mac_id")
    @Expose
    private String macId;
    @SerializedName("team")
    @Expose
    private String team;

    public String getMacId() {
        return macId;
    }

    public void setMacId(String macId) {
        this.macId = macId;
    }

    public ServiceRequest withMacId(String macId) {
        this.macId = macId;
        return this;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public ServiceRequest withTeam(String team) {
        this.team = team;
        return this;
    }

}