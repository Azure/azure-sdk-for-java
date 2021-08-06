// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.PartitionKey;

public interface CosmosItemOperation {
    String getId();

    PartitionKey getPartitionKeyValue();

    CosmosItemOperationType getOperationType();

    <T> T getItem();

    <T> T getContext();
}
