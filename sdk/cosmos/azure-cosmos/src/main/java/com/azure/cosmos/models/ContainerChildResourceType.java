// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Specifies the kind of resource that has a Cosmos container as parent resource.
 */
public enum ContainerChildResourceType {
    /**
     * Represents an item resource that is created in a Cosmos container.
     */
    ITEM("Item"),
    /**
     * Represents a stored procedure resource that is created in a Cosmos container.
     */
    STORED_PROCEDURE("StoredProcedure"),
    /**
     * Represents an user defined function resource that is created in a Cosmos container.
     */
    USER_DEFINED_FUNCTION("UserDefinedFunction"),
    /**
     * Represents a trigger resource that is created in a Cosmos container.
     */
    TRIGGER("Trigger");

    ContainerChildResourceType(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    private final String overWireValue;

    @Override
    public String toString() {
        return this.overWireValue;
    }
}
