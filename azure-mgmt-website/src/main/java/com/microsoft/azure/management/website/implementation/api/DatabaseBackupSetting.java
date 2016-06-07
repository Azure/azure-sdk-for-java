/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Note: properties are serialized in JSON format and stored in DB.
 * if new properties are added they might not be in the previous
 * data rows
 * so please handle nulls.
 */
public class DatabaseBackupSetting {
    /**
     * SqlAzure / MySql.
     */
    private String databaseType;

    /**
     * The name property.
     */
    private String name;

    /**
     * Contains a connection string name that is linked to the
     * SiteConfig.ConnectionStrings.
     * This is used during restore with overwrite connection
     * strings options.
     */
    private String connectionStringName;

    /**
     * Contains a connection string to a database which is being backed
     * up/restored. If the restore should happen to a new database, the
     * database name inside is the new one.
     */
    private String connectionString;

    /**
     * Get the databaseType value.
     *
     * @return the databaseType value
     */
    public String databaseType() {
        return this.databaseType;
    }

    /**
     * Set the databaseType value.
     *
     * @param databaseType the databaseType value to set
     * @return the DatabaseBackupSetting object itself.
     */
    public DatabaseBackupSetting withDatabaseType(String databaseType) {
        this.databaseType = databaseType;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the DatabaseBackupSetting object itself.
     */
    public DatabaseBackupSetting withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the connectionStringName value.
     *
     * @return the connectionStringName value
     */
    public String connectionStringName() {
        return this.connectionStringName;
    }

    /**
     * Set the connectionStringName value.
     *
     * @param connectionStringName the connectionStringName value to set
     * @return the DatabaseBackupSetting object itself.
     */
    public DatabaseBackupSetting withConnectionStringName(String connectionStringName) {
        this.connectionStringName = connectionStringName;
        return this;
    }

    /**
     * Get the connectionString value.
     *
     * @return the connectionString value
     */
    public String connectionString() {
        return this.connectionString;
    }

    /**
     * Set the connectionString value.
     *
     * @param connectionString the connectionString value to set
     * @return the DatabaseBackupSetting object itself.
     */
    public DatabaseBackupSetting withConnectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

}
