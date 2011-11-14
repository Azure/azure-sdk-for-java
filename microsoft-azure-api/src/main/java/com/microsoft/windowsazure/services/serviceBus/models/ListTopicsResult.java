package com.microsoft.windowsazure.services.serviceBus.models;

import java.util.List;

import com.microsoft.windowsazure.services.serviceBus.Topic;

public class ListTopicsResult {

    private List<Topic> items;

    public List<Topic> getItems() {
        return items;
    }

    public void setItems(List<Topic> items) {
        this.items = items;
    }
}
