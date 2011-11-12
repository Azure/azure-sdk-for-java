package com.microsoft.windowsazure.services.serviceBus;

import java.util.List;

public class ListQueuesResult {

    private List<Queue> items;

    List<Queue> getItems() {
        return items;
    }

    public void setItems(List<Queue> items) {
        this.items = items;
    }
}
