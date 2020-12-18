package com.azure.cosmos.implementation.throughputBudget;

public abstract class ThroughputBudgetControlContainerItem {
    private final String id;
    private final String group;
    private String _etag;

    public ThroughputBudgetControlContainerItem(String id, String group) {
        this.id = id;
        this.group = group;
    }

    public String getEtag() {
        return _etag;
    }

    public void setEtag(String etag) {
        this._etag = etag;
    }

    public String getId() {
        return id;
    }

    public String getGroup() {
        return group;
    }
}
