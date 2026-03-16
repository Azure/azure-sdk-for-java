// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents the materialized view definition for a container in the Azure Cosmos DB service.
 * A materialized view is derived from a source container and is defined by a SQL-like query.
 * <p>
 * Example:
 * <pre>{@code
 * "materializedViewDefinition": {
 *     "sourceCollectionId": "gsi-src",
 *     "definition": "SELECT c.customerId, c.emailAddress FROM c"
 * }
 * }</pre>
 */
public final class CosmosMaterializedViewDefinition {

    private JsonSerializable jsonSerializable;

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
    public String getSourceCollectionRid() {
        return this.jsonSerializable.getString(Constants.Properties.MATERIALIZED_VIEW_SOURCE_COLLECTION_RID);
    }

    /**
     * Sets the source collection id for the materialized view.
     *
     * @param sourceCollectionRid the source collection id.
     * @return CosmosMaterializedViewDefinition
     */
    public CosmosMaterializedViewDefinition setSourceCollectionRid(String sourceCollectionRid) {
        this.jsonSerializable.set(Constants.Properties.MATERIALIZED_VIEW_SOURCE_COLLECTION_RID, sourceCollectionRid);
        return this;
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
}
