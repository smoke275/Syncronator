package com.syncro.resources.events;

/**
 * Event class for EventBus for JSON events
 */
public class JSONUpdate {
    private String json;

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public JSONUpdate(String json) {
        this.json = json;
    }
}
