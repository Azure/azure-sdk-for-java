// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Objects;

/**
 * Represents a composite path of the IndexingPolicy in the Azure Cosmos DB database service.
 * A composite path is used in a composite index. For example if you want to run a query like
 * "SELECT * FROM c ORDER BY c.age, c.height", then you need to add "/age" and "/height"
 * as composite paths to your composite index.
 */
public final class CompositePath {

    private JsonSerializable jsonSerializable;

    /**
     * Constructor.
     */
    public CompositePath() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the included path.
     */
    CompositePath(String jsonString) {
        this.jsonSerializable = new JsonSerializable(jsonString);
    }


    /**
     * Constructor.
     *
     * @param objectNode the object node that represents the included path.
     */
    CompositePath(ObjectNode objectNode) {
        this.jsonSerializable = new JsonSerializable(objectNode);
    }

    /**
     * Gets path.
     *
     * @return the path.
     */
    public String getPath() {
        return this.jsonSerializable.getString(Constants.Properties.PATH);
    }

    /**
     * Sets path.
     *
     * @param path the path.
     * @return the CompositePath.
     */
    public CompositePath setPath(String path) {
        this.jsonSerializable = new JsonSerializable();
        this.jsonSerializable.set(Constants.Properties.PATH, path);

        return this;
    }

    /**
     * Gets the sort order for the composite path.
     * <p>
     * For example if you want to run the query "SELECT * FROM c ORDER BY c.age asc, c.height desc",
     * then you need to make the order for "/age" "ascending" and the order for "/height" "descending".
     *
     * @return the sort order.
     */
    public CompositePathSortOrder getOrder() {
        String strValue = this.jsonSerializable.getString(Constants.Properties.ORDER);
        if (!StringUtils.isEmpty(strValue)) {
            try {
                return CompositePathSortOrder
                           .valueOf(StringUtils.upperCase(this.jsonSerializable.getString(Constants.Properties.ORDER)));
            } catch (IllegalArgumentException e) {
                this.jsonSerializable.getLogger().warn("INVALID getIndexingMode getValue {}.",
                    this.jsonSerializable.getString(Constants.Properties.ORDER));
                return CompositePathSortOrder.ASCENDING;
            }
        }
        return CompositePathSortOrder.ASCENDING;
    }

    /**
     * Gets the sort order for the composite path.
     * <p>
     * For example if you want to run the query "SELECT * FROM c ORDER BY c.age asc, c.height desc",
     * then you need to make the order for "/age" "ascending" and the order for "/height" "descending".
     *
     * @param order the sort order.
     * @return the CompositePath.
     */
    public CompositePath setOrder(CompositePathSortOrder order) {
        this.jsonSerializable.set(Constants.Properties.ORDER, order.toString());
        return this;
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
    }

    JsonSerializable getJsonSerializable() {
        return this.jsonSerializable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CompositePath that = (CompositePath) o;
        return Objects.equals(getJsonSerializable(), that.getJsonSerializable());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getJsonSerializable());
    }
}
