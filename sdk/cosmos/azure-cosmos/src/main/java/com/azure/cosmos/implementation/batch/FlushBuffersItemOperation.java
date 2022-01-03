// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.models.CosmosItemOperationType;
import com.azure.cosmos.models.PartitionKey;

public class FlushBuffersItemOperation extends CosmosItemOperationBase {
    private static final String fixedId = "FlushBuffersItemOperation_7fea4e74-bcbb-4d86-aea1-3ef270e574aa";
    private static final FlushBuffersItemOperation singletonInstance = new FlushBuffersItemOperation();

    private FlushBuffersItemOperation() {
    }

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

    @Override
    public <T> T getContext() {
        return null;
    }

    @Override
    JsonSerializable getSerializedOperationInternal() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public static FlushBuffersItemOperation singleton() {
        return singletonInstance;
    }
}
