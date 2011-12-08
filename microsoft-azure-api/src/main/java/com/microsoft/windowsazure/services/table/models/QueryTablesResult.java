package com.microsoft.windowsazure.services.table.models;

public class QueryTablesResult {
    private String continuationToken;

    public String getContinuationToken() {
        return continuationToken;
    }

    public void setContinuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
    }

}
