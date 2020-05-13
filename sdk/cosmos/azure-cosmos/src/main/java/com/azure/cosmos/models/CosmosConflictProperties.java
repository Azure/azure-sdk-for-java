// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Conflict;
import com.azure.cosmos.implementation.Resource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Cosmos conflict properties.
 */
public final class CosmosConflictProperties extends ResourceWrapper{

    private Conflict conflict;

    /**
     * Initialize a conflict object.
     */
    CosmosConflictProperties() {
        this.conflict = new Conflict();
    }

    /**
     * Initialize a conflict object from json string.
     *
     * @param jsonString the json string that represents the conflict.
     */
    CosmosConflictProperties(String jsonString) {
        this.conflict = new Conflict(jsonString);
    }

    /**
     * Gets the operation kind.
     *
     * @return the operation kind.
     */
    public String getOperationKind() {
        return this.conflict.getOperationKind();
    }

    /**
     * Gets the type of the conflicting resource.
     *
     * @return the resource type.
     */
    public String getResourceType() {
        return this.conflict.getResouceType();
    }

    /**
     * Gets the resource ID for the conflict in the Azure Cosmos DB service.
     *
     * @return resource Id for the conflict.
     */
    String getSourceResourceId() {
        return this.conflict.getResourceId();
    }

    static List<CosmosConflictProperties> getFromV2Results(List<Conflict> results) {
        return results.stream().map(conflict -> new CosmosConflictProperties(conflict.toJson()))
                   .collect(Collectors.toList());
    }

    @Override
    Resource getResource() {
        return this.conflict;
    }
}
