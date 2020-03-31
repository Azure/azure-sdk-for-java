// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Represents a SQL query in the Azure Cosmos DB database service.
 */
public final class SqlQuerySpec extends JsonSerializable {

    private List<SqlParameter> parameters;

    /**
     * Initializes a new instance of the SqlQuerySpec class.
     */
    public SqlQuerySpec() {
        super();
    }

    /**
     * Initializes a new instance of the SqlQuerySpec class with the text of the
     * query.
     *
     * @param queryText the query text.
     */
    public SqlQuerySpec(String queryText) {
        super();
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
        super();
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
        super();
        this.setQueryText(queryText);
        this.parameters = Arrays.asList(parameters);
    }

    /**
     * Gets the text of the query.
     *
     * @return the query text.
     */
    public String getQueryText() {
        return super.getString("query");
    }

    /**
     * Sets the text of the query.
     *
     * @param queryText the query text.
     * @return the SqlQuerySpec.
     */
    public SqlQuerySpec setQueryText(String queryText) {
        super.set("query", queryText);
        return this;
    }

    /**
     * Gets the collection of query parameters.
     *
     * @return the query parameters.
     */
    public List<SqlParameter> getParameters() {
        if (this.parameters == null) {
            Collection<SqlParameter> sqlParameters = super.getCollection("parameters", SqlParameter.class);
            if (sqlParameters == null) {
                sqlParameters = new ArrayList<>();
            }

            this.parameters = new ArrayList<>(sqlParameters);
        }

        return this.parameters;
    }

    /**
     * Sets the collection of query parameters.
     *
     * @param parameters the query parameters.
     * @return the SqlQuerySpec.
     */
    public SqlQuerySpec setParameters(List<SqlParameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    @Override
    protected void populatePropertyBag() {
        super.populatePropertyBag();
        boolean defaultParameters = (this.parameters != null && this.parameters.size() != 0);

        if (defaultParameters) {
            super.set("parameters", this.parameters);
        } else {
            super.remove("parameters");
        }
    }
}
