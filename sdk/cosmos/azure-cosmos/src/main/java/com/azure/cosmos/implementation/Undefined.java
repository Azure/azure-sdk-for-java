// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

/**
 * Represents the 'Undefined' partition key in the Azure Cosmos DB database service.
 */
public class Undefined extends JsonSerializable {

    private final static Undefined value = new Undefined();

    /**
     * Constructor. CREATE a new instance of the Undefined object.
    */
    private Undefined() {
    }

    /**
     * Returns the singleton value of Undefined.
     *
     * @return the Undefined value
    */
    public static Undefined value() {
        return value;
    }

    /**
     * Returns the string representation of Undfined.
    */
    public String toString() {
        return "{}";
    }
}
