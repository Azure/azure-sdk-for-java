package com.microsoft.windowsazure.services.serviceBus.models;

import java.util.List;

import com.microsoft.windowsazure.services.serviceBus.Subscription;

public class ListSubscriptionsResult {

    private List<Subscription> items;

    public List<Subscription> getItems() {
        return items;
    }

    public void setItems(List<Subscription> items) {
        this.items = items;
    }
}
