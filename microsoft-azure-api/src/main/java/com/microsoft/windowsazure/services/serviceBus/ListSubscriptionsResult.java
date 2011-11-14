package com.microsoft.windowsazure.services.serviceBus;

import java.util.List;

public class ListSubscriptionsResult {

    private List<Subscription> items;

    List<Subscription> getItems() {
        return items;
    }

    public void setItems(List<Subscription> items) {
        this.items = items;
    }
}
