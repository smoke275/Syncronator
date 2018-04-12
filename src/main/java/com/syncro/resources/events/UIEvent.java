package com.syncro.resources.events;

public class UIEvent {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UIEvent(String message) {
        this.message = message;
    }
}
