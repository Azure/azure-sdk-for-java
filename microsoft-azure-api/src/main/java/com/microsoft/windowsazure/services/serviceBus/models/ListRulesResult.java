package com.microsoft.windowsazure.services.serviceBus.models;

import java.util.List;

public class ListRulesResult {

    private List<Rule> items;

    public List<Rule> getItems() {
        return items;
    }

    public void setItems(List<Rule> items) {
        this.items = items;
    }

}
