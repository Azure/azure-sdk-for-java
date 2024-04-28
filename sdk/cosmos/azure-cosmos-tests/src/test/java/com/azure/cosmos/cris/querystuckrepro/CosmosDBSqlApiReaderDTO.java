package com.azure.cosmos.cris.querystuckrepro;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;

public class CosmosDBSqlApiReaderDTO {
    private final PartReadAttris partReadattris;
    private CosmosAsyncContainer readContainer;

    private CosmosAsyncClient client;
    private String query;
    private String partitionKeyStr;

    public CosmosDBSqlApiReaderDTO(PartReadAttris partReadattris, CosmosAsyncClient client, CosmosAsyncContainer readContainer, String query, String partitionKeyStr) {
        this.partReadattris = partReadattris;
        this.readContainer = readContainer;
        this.client = client;
        this.query = query;
        this.partitionKeyStr = partitionKeyStr;
    }

    public PartReadAttris getPartReadAttris() {
        return this.partReadattris;
    }

    public CosmosAsyncContainer getReadContainer() {
        return this.readContainer;
    }

    public CosmosAsyncClient getClient() {
        return this.client;
    }

    public String getQuery() {
        return this.query;
    }

    public String getPartitionKeyStr() {
        return this.partitionKeyStr;
    }

}
