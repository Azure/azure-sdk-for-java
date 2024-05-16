// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Specifies whether or not the resource is to be indexed in the Azure Cosmos DB database service.
 */
public enum IndexingDirective {

    /**
     * Use any pre-defined/pre-configured defaults.
     */
    DEFAULT("Default"),

    /**
     * Index the resource.
     */
    INCLUDE("Include"),

    /**
     * Do not index the resource.
     */
    EXCLUDE("Exclude");

    IndexingDirective(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    private final String overWireValue;

    @Override
    public String toString() {
        return this.overWireValue;
    }
}
