package com.syncro.web;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegisterServiceResponse {

    @SerializedName("success")
    @Expose
    private String message;

    public String getMessage() {
        return message;
    }

    public void setSuccess(String message) {
        this.message = message;
    }

}