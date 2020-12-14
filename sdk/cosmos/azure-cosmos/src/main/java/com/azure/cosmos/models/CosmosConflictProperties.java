// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Conflict;
import com.azure.cosmos.implementation.OperationKind;
import com.azure.cosmos.implementation.Resource;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Cosmos conflict properties.
 */
public final class CosmosConflictProperties {

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
    public OperationKind getOperationKind() {
        return this.conflict.getOperationKind();
    }

    /**
     * Gets the type of the conflicting resource.
     *
     * @return the resource type.
     */
    String getResourceType() {
        return this.conflict.getResourceType();
    }

    Resource getResource() {
        return this.conflict;
    }

    /**
     * Gets the conflicting resource in the Azure Cosmos DB service.
     *
     * @param <T>   the type of the object.
     * @param klass The returned type of conflicting resource.
     * @return The conflicting resource.
     */
    public <T> T getItem(Class<T> klass) {
        return this.conflict.getItem(klass);
    }

    /**
     * Gets the name of the resource.
     *
     * @return the name of the resource.
     */
    public String getId() {
        return this.conflict.getId();
    }

    /**
     * Sets the name of the resource.
     *
     * @param id the name of the resource.
     * @return the current instance of {@link CosmosConflictProperties}.
     */
    public CosmosConflictProperties setId(String id) {
        this.conflict.setId(id);
        return this;
    }

    /**
     * Gets the ID associated with the resource.
     *
     * @return the ID associated with the resource.
     */
    String getResourceId() {
        return this.conflict.getResourceId();
    }

    /**
     * Get the last modified timestamp associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the timestamp.
     */
    public Instant getTimestamp() {
        return this.conflict.getTimestamp();
    }

    /**
     * Get the entity tag associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the e tag.
     */
    public String getETag() {
        return this.conflict.getETag();
    }

    static List<CosmosConflictProperties> getFromV2Results(List<Conflict> results) {
        return results.stream().map(conflict -> new CosmosConflictProperties(conflict.toJson()))
                   .collect(Collectors.toList());
    }
}
