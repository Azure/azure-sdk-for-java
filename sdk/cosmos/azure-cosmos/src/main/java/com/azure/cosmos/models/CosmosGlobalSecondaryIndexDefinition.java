// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents the materialized view definition for a container in the Azure Cosmos DB service.
 * A materialized view is derived from a source container and is defined by a SQL-like query.
 * <p>
 * Example:
 * <pre>{@code
 * CosmosGlobalSecondaryIndexDefinition definition = new CosmosGlobalSecondaryIndexDefinition()
 *     .setSourceContainer("gsi-src")
 *     .setDefinition("SELECT c.customerId, c.emailAddress FROM c");
 * }</pre>
 */
public final class CosmosGlobalSecondaryIndexDefinition {

    private final JsonSerializable jsonSerializable;

    /**
     * Constructor
     */
    public CosmosGlobalSecondaryIndexDefinition() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Constructor.
     *
     * @param objectNode the {@link ObjectNode} that represents the materialized view definition.
     */
    CosmosGlobalSecondaryIndexDefinition(ObjectNode objectNode) {
        this.jsonSerializable = new JsonSerializable(objectNode);
    }

    /**
     * Gets the source container id for the materialized view.
     *
     * @return the source container id.
     */
    public String getSourceContainerId() {
        return this.jsonSerializable.getString(Constants.Properties.MATERIALIZED_VIEW_SOURCE_COLLECTION_ID);
    }

    /**
     * Sets the source container id for the materialized view.
     * The SDK will automatically resolve this container id to its resource id (RID)
     * during container creation.
     *
     * @param sourceContainerId the source container id.
     * @return CosmosGlobalSecondaryIndexDefinition
     */
    public CosmosGlobalSecondaryIndexDefinition setSourceContainerId(String sourceContainerId) {
        this.jsonSerializable.set(Constants.Properties.MATERIALIZED_VIEW_SOURCE_COLLECTION_ID, sourceContainerId);
        return this;
    }

    void setSourceContainerRidInternal(String sourceCollectionRid) {
        this.jsonSerializable.set(Constants.Properties.MATERIALIZED_VIEW_SOURCE_COLLECTION_RID, sourceCollectionRid);
    }

    /**
     * Gets the source container resource id (RID) for the global secondary index as returned by the server.
     * This is a read-only field populated from server responses.
     *
     * @return the source container resource id.
     */
    public String getSourceContainerRid() {
        return this.jsonSerializable.getString(Constants.Properties.MATERIALIZED_VIEW_SOURCE_COLLECTION_RID);
    }

    /**
     * Gets the build status of the GlobalSecondaryIndex view as returned by the server.
     * This is a read-only field populated from server responses.
     *
     * @return the GlobalSecondaryIndex view build status.
     */
    public String getStatus() {
        return this.jsonSerializable.getString(Constants.Properties.MATERIALIZED_VIEW_STATUS);
    }

    /**
     * Gets the query definition for the GlobalSecondaryIndex view.
     *
     * @return the query definition.
     */
    public String getDefinition() {
        return this.jsonSerializable.getString(Constants.Properties.MATERIALIZED_VIEW_QUERY_DEFINITION);
    }

    /**
     * Sets the query definition for the materialized view.
     *
     * @param definition the query definition (e.g. {@code "SELECT c.customerId, c.emailAddress FROM c"}).
     * @return CosmosGlobalSecondaryIndexDefinition
     */
    public CosmosGlobalSecondaryIndexDefinition setDefinition(String definition) {
        this.jsonSerializable.set(Constants.Properties.MATERIALIZED_VIEW_QUERY_DEFINITION, definition);
        return this;
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
    }

    JsonSerializable getJsonSerializable() {
        return this.jsonSerializable;
    }

    @Override
    public String toString() {
        try {
            return Utils.getSimpleObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to convert object to string", e);
        }
    }
}
