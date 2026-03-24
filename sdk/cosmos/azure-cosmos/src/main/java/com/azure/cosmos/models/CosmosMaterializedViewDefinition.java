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
 * CosmosMaterializedViewDefinition definition = new CosmosMaterializedViewDefinition()
 *     .setSourceCollectionId("gsi-src")
 *     .setDefinition("SELECT c.customerId, c.emailAddress FROM c");
 * }</pre>
 */
public final class CosmosMaterializedViewDefinition {

    private final JsonSerializable jsonSerializable;

    /**
     * Constructor
     */
    public CosmosMaterializedViewDefinition() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Constructor.
     *
     * @param objectNode the {@link ObjectNode} that represents the materialized view definition.
     */
    CosmosMaterializedViewDefinition(ObjectNode objectNode) {
        this.jsonSerializable = new JsonSerializable(objectNode);
    }

    /**
     * Gets the source collection id for the materialized view.
     *
     * @return the source collection id.
     */
    public String getSourceCollectionId() {
        return this.jsonSerializable.getString(Constants.Properties.MATERIALIZED_VIEW_SOURCE_COLLECTION_ID);
    }

    /**
     * Sets the source collection id for the materialized view.
     * The SDK will automatically resolve this collection id to its resource id (RID)
     * during container creation.
     *
     * @param sourceCollectionId the source collection id.
     * @return CosmosMaterializedViewDefinition
     */
    public CosmosMaterializedViewDefinition setSourceCollectionId(String sourceCollectionId) {
        this.jsonSerializable.set(Constants.Properties.MATERIALIZED_VIEW_SOURCE_COLLECTION_ID, sourceCollectionId);
        return this;
    }

    void setSourceCollectionRidInternal(String sourceCollectionRid) {
        this.jsonSerializable.set(Constants.Properties.MATERIALIZED_VIEW_SOURCE_COLLECTION_RID, sourceCollectionRid);
    }

    /**
     * Gets the source collection resource id (RID) for the materialized view as returned by the server.
     * This is a read-only field populated from server responses.
     *
     * @return the source collection resource id.
     */
    public String getSourceCollectionRid() {
        return this.jsonSerializable.getString(Constants.Properties.MATERIALIZED_VIEW_SOURCE_COLLECTION_RID);
    }

    /**
     * Gets the build status of the materialized view as returned by the server.
     * This is a read-only field populated from server responses.
     *
     * @return the materialized view build status.
     */
    public String getStatus() {
        return this.jsonSerializable.getString(Constants.Properties.MATERIALIZED_VIEW_STATUS);
    }

    /**
     * Gets the query definition for the materialized view.
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
     * @return CosmosMaterializedViewDefinition
     */
    public CosmosMaterializedViewDefinition setDefinition(String definition) {
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
