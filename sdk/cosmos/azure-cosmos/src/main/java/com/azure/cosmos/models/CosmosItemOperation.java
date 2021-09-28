// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Encapsulates Cosmos Item Operation
 */
public interface CosmosItemOperation {

    String getId();

    PartitionKey getPartitionKeyValue();

    CosmosItemOperationType getOperationType();

    <T> T getItem();

    <T> T getContext();
}
