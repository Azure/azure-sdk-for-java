// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a SQL query in the Azure Cosmos DB database service.
 */
public final class SqlQuerySpec extends JsonSerializable {

    private SqlParameterList parameters;

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
     * @param queryText
     *            the query text.
     */
    public SqlQuerySpec(String queryText) {
        super();
        this.queryText(queryText);
    }

    /**
     * Initializes a new instance of the SqlQuerySpec class with the text of the
     * query and parameters.
     * 
     * @param queryText  the query text.
     * @param parameters the query parameters.
     */
    public SqlQuerySpec(String queryText, SqlParameterList parameters) {
        super();
        this.queryText(queryText);
        this.parameters = parameters;
    }

    /**
     * Gets the text of the query.
     * 
     * @return the query text.
     */
    public String queryText() {
        return super.getString("query");
    }

    /**
     * Sets the text of the query.
     * 
     * @param queryText
     *            the query text.
     * @return the SqlQuerySpec.
     */
    public SqlQuerySpec queryText(String queryText) {
        super.set("query", queryText);
        return this;
    }

    /**
     * Gets the collection of query parameters.
     * 
     * @return the query parameters.
     */
    public SqlParameterList parameters() {
        if (this.parameters == null) {
            Collection<SqlParameter> sqlParameters = super.getCollection("parameters", SqlParameter.class);
            if (sqlParameters == null) {
                sqlParameters = new ArrayList<SqlParameter>();
            }

            this.parameters = new SqlParameterList(sqlParameters);
        }

        return this.parameters;
    }

    /**
     * Sets the collection of query parameters.
     * 
     * @param parameters
     *            the query parameters.
     * @return the SqlQuerySpec.
     */
    public SqlQuerySpec parameters(SqlParameterList parameters) {
        this.parameters = parameters;
        return this;
    }

    @Override
    void populatePropertyBag() {
        boolean defaultParameters = (this.parameters != null && this.parameters.size() != 0);

        if (defaultParameters) {
            super.set("parameters", this.parameters);
        } else {
            super.remove("parameters");
        }
    }
}
