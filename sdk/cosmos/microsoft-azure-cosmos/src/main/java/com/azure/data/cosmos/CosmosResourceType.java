// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

/**
 * Resource types in the Azure Cosmos DB database service.
 */
public enum CosmosResourceType {

    System(-100),
    Attachment(3),
    DocumentCollection(1),
    Conflict(107),
    Database(0),
    Document(2),
    Index(104),
    Offer(113),
    Permission(5),
    StoredProcedure(109),
    Trigger(110),
    User(4),
    UserDefinedFunction(111);
    
    final private int value;

    CosmosResourceType(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
