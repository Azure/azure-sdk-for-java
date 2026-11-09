// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.StoredProcedure;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a stored procedure in the Azure Cosmos DB database service.
 * <p>
 * Cosmos DB allows stored procedures to be executed in the storage tier, directly against a container. The
 * script gets executed under ACID transactions on the primary storage partition of the specified container. For
 * additional details, refer to
 * <a href="https://docs.microsoft.com/en-us/azure/cosmos-db/how-to-write-stored-procedures-triggers-udfs">documentation</a>
 */
public final class CosmosStoredProcedureProperties {

    private StoredProcedure storedProcedure;
    /**
     * Constructor.
     */
    CosmosStoredProcedureProperties() {
        this.storedProcedure = new StoredProcedure();
    }

    /**
     * Sets the id
     *
     * @param id the name of the resource.
     * @return return the Cosmos stored procedure properties with id set
     */
    public CosmosStoredProcedureProperties setId(String id) {
        this.storedProcedure.setId(id);
        return this;
    }

    /**
     * Constructor.
     *
     * @param jsonNode the json node that represents the stored procedure.
     */
    CosmosStoredProcedureProperties(ObjectNode jsonNode) {
        this.storedProcedure = new StoredProcedure(jsonNode);
    }

    /**
     * Constructor.
     *
     * @param id the id of the stored procedure
     * @param body the body of the stored procedure
     */
    public CosmosStoredProcedureProperties(String id, String body) {
        this.storedProcedure = new StoredProcedure();
        storedProcedure.setId(id);
        storedProcedure.setBody(body);
    }

    /**
     * Get the body of the stored procedure.
     *
     * @return the body of the stored procedure.
     */
    public String getBody() {
        return this.storedProcedure.getBody();
    }

    /**
     * Set the body of the stored procedure.
     *
     * @param body the body of the stored procedure.
     * @return return the Cosmos stored procedure properties.
     */
    public CosmosStoredProcedureProperties setBody(String body) {
        this.storedProcedure.setBody(body);
        return this;
    }

    Resource getResource() {
        return this.storedProcedure;
    }

    /**
     * Gets the name of the resource.
     *
     * @return the name of the resource.
     */
    public String getId() {
        return this.storedProcedure.getId();
    }

    /**
     * Gets the ID associated with the resource.
     *
     * @return the ID associated with the resource.
     */
    String getResourceId() {
        return this.storedProcedure.getResourceId();
    }

    /**
     * Get the last modified timestamp associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the timestamp.
     */
    public Instant getTimestamp() {
        return this.storedProcedure.getTimestamp();
    }

    /**
     * Get the entity tag associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the e tag.
     */
    public String getETag() {
        return this.storedProcedure.getETag();
    }

    static List<CosmosStoredProcedureProperties> getFromV2Results(List<StoredProcedure> results) {
        return results.stream().map(sproc -> new CosmosStoredProcedureProperties(sproc.getPropertyBag()))
            .collect(Collectors.toList());
    }
}
