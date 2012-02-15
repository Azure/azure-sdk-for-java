package com.microsoft.windowsazure.services.table.models;

import java.util.ArrayList;
import java.util.List;

public class QueryEntitiesResult {
    private String nextPartitionKey;
    private String nextRowKey;
    private List<Entity> entities = new ArrayList<Entity>();

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    public String getNextPartitionKey() {
        return nextPartitionKey;
    }

    public void setNextPartitionKey(String nextPartitionKey) {
        this.nextPartitionKey = nextPartitionKey;
    }

    public String getNextRowKey() {
        return nextRowKey;
    }

    public void setNextRowKey(String nextRowKey) {
        this.nextRowKey = nextRowKey;
    }
}
