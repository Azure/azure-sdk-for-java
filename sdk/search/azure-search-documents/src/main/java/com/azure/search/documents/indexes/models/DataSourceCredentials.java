// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents credentials that can be used to connect to a datasource.
 */
@Fluent
public final class DataSourceCredentials {
    /*
     * The connection string for the datasource.
     */
    @JsonProperty(value = "connectionString")
    private String connectionString;

    /**
     * Get the connectionString property: The connection string for the
     * datasource.
     *
     * @return the connectionString value.
     */
    public String getConnectionString() {
        return this.connectionString;
    }

    /**
     * Set the connectionString property: The connection string for the
     * datasource.
     *
     * @param connectionString the connectionString value to set.
     * @return the DataSourceCredentials object itself.
     */
    public DataSourceCredentials setConnectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }
}
