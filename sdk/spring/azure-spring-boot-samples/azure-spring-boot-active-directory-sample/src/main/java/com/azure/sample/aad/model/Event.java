package com.azure.sample.aad.model;

import java.time.ZonedDateTime;

public class Event {

    private String content;

    private ZonedDateTime dateTime;

    private String location;

    public Event(String content, ZonedDateTime dateTime, String location) {
        this.content = content;
        this.dateTime = dateTime;
        this.location = location;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
