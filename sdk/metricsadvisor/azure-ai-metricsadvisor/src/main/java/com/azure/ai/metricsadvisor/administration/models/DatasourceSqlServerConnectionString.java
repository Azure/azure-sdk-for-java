// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.DataSourceSqlServerConnectionStringAccessor;
import com.azure.core.annotation.Fluent;

/**
 * The connection credential entity for SQLServer.
 */
@Fluent
public final class DatasourceSqlServerConnectionString extends DatasourceCredentialEntity {
    private String id;
    private String name;
    private String description;
    private String connectionString;

    static {
        DataSourceSqlServerConnectionStringAccessor.setAccessor(
            new DataSourceSqlServerConnectionStringAccessor.Accessor() {
                @Override
                public void setId(DatasourceSqlServerConnectionString entity, String id) {
                    entity.setId(id);
                }

                @Override
                public String getConnectionString(DatasourceSqlServerConnectionString entity) {
                    return entity.getConnectionString();
                }
            });
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * Creates DataSourceSqlServerConnectionString.
     *
     * @param name The name
     * @param connectionString The connection string
     */
    public DatasourceSqlServerConnectionString(String name, String connectionString) {
        this.name = name;
        this.connectionString = connectionString;
    }

    /**
     * Sets the name.
     *
     * @param name The name
     * @return an updated object with name set
     */
    public DatasourceSqlServerConnectionString setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the connection string.
     *
     * @param connectionString The connection string
     * @return an updated object with connection string set
     */
    public DatasourceSqlServerConnectionString setConnectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    /**
     * Sets the description.
     *
     * @param description The description.
     * @return an updated object with description set
     */
    public DatasourceSqlServerConnectionString setDescription(String description) {
        this .description = description;
        return this;
    }

    private void setId(String id) {
        this.id = id;
    }

    private String getConnectionString() {
        return this.connectionString;
    }
}
