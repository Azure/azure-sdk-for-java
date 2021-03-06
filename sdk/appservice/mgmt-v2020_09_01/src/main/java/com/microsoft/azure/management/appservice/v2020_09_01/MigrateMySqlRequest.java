/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.appservice.v2020_09_01;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;

/**
 * MySQL migration request.
 */
@JsonFlatten
public class MigrateMySqlRequest extends ProxyOnlyResource {
    /**
     * Connection string to the remote MySQL database.
     */
    @JsonProperty(value = "properties.connectionString", required = true)
    private String connectionString;

    /**
     * The type of migration operation to be done. Possible values include:
     * 'LocalToRemote', 'RemoteToLocal'.
     */
    @JsonProperty(value = "properties.migrationType", required = true)
    private MySqlMigrationType migrationType;

    /**
     * Get connection string to the remote MySQL database.
     *
     * @return the connectionString value
     */
    public String connectionString() {
        return this.connectionString;
    }

    /**
     * Set connection string to the remote MySQL database.
     *
     * @param connectionString the connectionString value to set
     * @return the MigrateMySqlRequest object itself.
     */
    public MigrateMySqlRequest withConnectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    /**
     * Get the type of migration operation to be done. Possible values include: 'LocalToRemote', 'RemoteToLocal'.
     *
     * @return the migrationType value
     */
    public MySqlMigrationType migrationType() {
        return this.migrationType;
    }

    /**
     * Set the type of migration operation to be done. Possible values include: 'LocalToRemote', 'RemoteToLocal'.
     *
     * @param migrationType the migrationType value to set
     * @return the MigrateMySqlRequest object itself.
     */
    public MigrateMySqlRequest withMigrationType(MySqlMigrationType migrationType) {
        this.migrationType = migrationType;
        return this;
    }

}
