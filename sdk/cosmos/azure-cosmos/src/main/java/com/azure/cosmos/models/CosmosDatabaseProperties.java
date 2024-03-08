// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.Resource;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a CosmosAsyncDatabase in the Azure Cosmos database service. A cosmos database manages users,
 * permissions and a set of containers
 * <p>
 * Each Azure Cosmos DB Service is able to support multiple independent named databases, with the database being the
 * logical container for data. Each Database consists of one or more cosmos containers, each of which in turn contain
 * one or
 * more cosmos items. Since databases are an an administrative resource and the Service Key will be required in
 * order to access and successfully complete any action using the User APIs.
 */
public final class CosmosDatabaseProperties {

    private Database database;
    /**
     * Constructor
     *
     * @param id id of the database
     */
    public CosmosDatabaseProperties(String id) {
        this.database = new Database();
        this.database.setId(id);
    }

    CosmosDatabaseProperties(ObjectNode jsonNode, String dummy) {
        this.database = new Database(jsonNode);
    }

    // Converting container to CosmosContainerProperties
    CosmosDatabaseProperties(Database database) {
        this.database = database;
    }

    Resource getResource() {
        return this.database;
    }

    /**
     * Gets the name of the resource.
     *
     * @return the name of the resource.
     */
    public String getId() {
        return this.database.getId();
    }

    /**
     * Sets the name of the resource.
     *
     * @param id the name of the resource.
     * @return the current instance of {@link CosmosDatabaseProperties}.
     */
    public CosmosDatabaseProperties setId(String id) {
        this.database.setId(id);
        return this;
    }

    /**
     * Gets the ID associated with the resource.
     *
     * @return the ID associated with the resource.
     */
    public String getResourceId() {
        return this.database.getResourceId();
    }

    /**
     * Get the last modified timestamp associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the timestamp.
     */
    public Instant getTimestamp() {
        return this.database.getTimestamp();
    }

    /**
     * Get the entity tag associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the e tag.
     */
    public String getETag() {
        return this.database.getETag();
    }

    static List<CosmosDatabaseProperties> getFromV2Results(List<Database> results) {
        return results.stream().map(CosmosDatabaseProperties::new).collect(Collectors.toList());
    }
}
