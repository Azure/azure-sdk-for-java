// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.ai.metricsadvisor.implementation.util.SqlServerConnectionStringCredentialEntityAccessor;

/**
 * The connection credential entity for SQLServer.
 */
public final class SqlServerConnectionStringCredentialEntity extends DataSourceCredentialEntity {
    private String id;
    private String name;
    private String description;
    private String connectionString;

    static {
        SqlServerConnectionStringCredentialEntityAccessor.setAccessor(
            new SqlServerConnectionStringCredentialEntityAccessor.Accessor() {
                @Override
                public void setId(SqlServerConnectionStringCredentialEntity entity, String id) {
                    entity.setId(id);
                }

                @Override
                public String getConnectionString(SqlServerConnectionStringCredentialEntity entity) {
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
     * Creates SQLServerConnectionStringCredentialEntity.
     *
     * @param name The name
     * @param connectionString The connection string
     */
    public SqlServerConnectionStringCredentialEntity(String name, String connectionString) {
        this.name = name;
        this.connectionString = connectionString;
    }

    /**
     * Sets the name.
     *
     * @param name The name
     * @return an updated object with name set
     */
    public SqlServerConnectionStringCredentialEntity setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the connection string.
     *
     * @param connectionString The connection string
     * @return an updated object with connection string set
     */
    public SqlServerConnectionStringCredentialEntity setConnectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    /**
     * Sets the description.
     *
     * @param description The description.
     * @return an updated object with description set
     */
    public SqlServerConnectionStringCredentialEntity setDescription(String description) {
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
