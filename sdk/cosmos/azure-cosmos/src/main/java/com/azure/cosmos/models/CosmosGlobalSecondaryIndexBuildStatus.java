// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Represents the build status of a global secondary index as returned by the Azure Cosmos DB service.
 */
public enum CosmosGlobalSecondaryIndexBuildStatus {

    /**
     * The global secondary index has been fully built and is actively serving queries.
     */
    ACTIVE("Active"),

    /**
     * The build status returned by the service is not a value that this SDK version recognizes,
     * or no status was returned.
     */
    UNKNOWN("Unknown");

    private final String overWireValue;

    CosmosGlobalSecondaryIndexBuildStatus(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    /**
     * Returns the {@link CosmosGlobalSecondaryIndexBuildStatus} that matches the provided wire value, or
     * {@link #UNKNOWN} when the value is {@code null} or not recognized by this SDK version.
     *
     * @param value the over-the-wire status value returned by the service.
     * @return the matching enum value, or {@link #UNKNOWN}.
     */
    public static CosmosGlobalSecondaryIndexBuildStatus fromString(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        for (CosmosGlobalSecondaryIndexBuildStatus status : values()) {
            if (status.overWireValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return this.overWireValue;
    }
}
