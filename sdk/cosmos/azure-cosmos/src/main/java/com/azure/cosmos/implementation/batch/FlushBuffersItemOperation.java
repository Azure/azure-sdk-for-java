package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.CosmosItemOperation;
import com.azure.cosmos.CosmosItemOperationType;
import com.azure.cosmos.models.PartitionKey;

public class FlushBuffersItemOperation implements CosmosItemOperation {
    private static final String fixedId = "FlushBuffersItemOperation_7fea4e74-bcbb-4d86-aea1-3ef270e574aa";

    @Override
    public String getId() {
        return fixedId;
    }

    @Override
    public PartitionKey getPartitionKeyValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CosmosItemOperationType getOperationType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getItem() {
        return null;
    }
}
