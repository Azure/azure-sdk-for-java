// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a SQL query in the Azure Cosmos DB database service.
 */
public final class SqlQuerySpec {

    private List<SqlParameter> parameters;

    private JsonSerializable jsonSerializable;

    /**
     * Initializes a new instance of the SqlQuerySpec class.
     */
    public SqlQuerySpec() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Initializes a new instance of the SqlQuerySpec class.
     *
     * @param objectNode the object node that represents the included path.
     */
    SqlQuerySpec(ObjectNode objectNode) {
        this.jsonSerializable = new JsonSerializable(objectNode);
    }

    /**
     * Initializes a new instance of the SqlQuerySpec class with the text of the
     * query.
     *
     * @param queryText the query text.
     */
    public SqlQuerySpec(String queryText) {
        this.jsonSerializable = new JsonSerializable();
        this.setQueryText(queryText);
    }

    /**
     * Initializes a new instance of the SqlQuerySpec class with the text of the
     * query and parameters.
     *
     * @param queryText the query text.
     * @param parameters the query parameters.
     */
    public SqlQuerySpec(String queryText, List<SqlParameter> parameters) {
        this.jsonSerializable = new JsonSerializable();
        this.setQueryText(queryText);
        this.parameters = parameters;
    }

    /**
     * Initializes a new instance of the SqlQuerySpec class with the text of the
     * query and parameters.
     *
     * @param queryText the query text.
     * @param parameters the query parameters.
     */
    public SqlQuerySpec(String queryText, SqlParameter... parameters) {
        this.jsonSerializable = new JsonSerializable();
        this.setQueryText(queryText);
        this.parameters = Collections.synchronizedList(Arrays.asList(parameters));
    }

    /**
     * Gets the text of the query.
     *
     * @return the query text.
     */
    public String getQueryText() {
        return this.jsonSerializable.getString("query");
    }

    /**
     * Sets the text of the query.
     *
     * @param queryText the query text.
     * @return the SqlQuerySpec.
     */
    public SqlQuerySpec setQueryText(String queryText) {
        this.jsonSerializable.set("query", queryText);
        return this;
    }

    /**
     * Gets the container of query parameters.
     *
     * @return the query parameters.
     */
    public List<SqlParameter> getParameters() {
        if (this.parameters == null) {
            Collection<SqlParameter> sqlParameters = this.jsonSerializable.getCollection("parameters", SqlParameter.class);
            if (sqlParameters == null) {
                sqlParameters = new ArrayList<>();
            }

            this.parameters = Collections.synchronizedList(new ArrayList<>(sqlParameters));
        }

        return this.parameters;
    }

    /**
     * Sets the container of query parameters.
     *
     * @param parameters the query parameters.
     * @return the SqlQuerySpec.
     */
    public SqlQuerySpec setParameters(List<SqlParameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
        boolean defaultParameters = (this.parameters != null && this.parameters.size() != 0);

        if (defaultParameters) {
            this.jsonSerializable.set("parameters", this.parameters);
        } else {
            this.jsonSerializable.remove("parameters");
        }
    }

    JsonSerializable getJsonSerializable() { return this.jsonSerializable; }
}
