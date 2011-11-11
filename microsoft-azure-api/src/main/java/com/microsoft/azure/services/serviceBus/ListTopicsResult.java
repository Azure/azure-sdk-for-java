package com.microsoft.azure.services.serviceBus;

import java.util.List;

public class ListTopicsResult {

    private List<Topic> items;

    List<Topic> getItems() {
        return items;
    }

    public void setItems(List<Topic> items) {
        this.items = items;
    }
}
