package com.microsoft.windowsazure.services.serviceBus.models;

import java.util.List;


public class ListSubscriptionsResult {

    private List<Subscription> items;

    public List<Subscription> getItems() {
        return items;
    }

    public void setItems(List<Subscription> items) {
        this.items = items;
    }
}
