// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Resource types in the Azure Cosmos DB database service.
 */
public enum CosmosResourceType {

    SYSTEM(-100, "System"),
    ATTACHMENT(3, "Attachment"),
    DOCUMENT_COLLECTION(1, "DocumentCollection"),
    CONFLICT(107, "Conflict"),
    DATABASE(0, "Database"),
    DOCUMENT(2, "Document"),
    INDEX(104, "Index"),
    OFFER(113, "Offer"),
    PERMISSION(5, "Permission"),
    STORED_PROCEDURE(109, "StoredProcedure"),
    TRIGGER(110, "Trigger"),
    USER(4, "User"),
    USER_DEFINED_FUNCTION(111, "UserDefinedFunction");

    private final int value;
    private final String overWireValue;

    CosmosResourceType(int value, String overWireValue) {
        this.value = value;
        this.overWireValue = overWireValue;
    }

    @Override
    public String toString() {
        return this.overWireValue;
    }
}
