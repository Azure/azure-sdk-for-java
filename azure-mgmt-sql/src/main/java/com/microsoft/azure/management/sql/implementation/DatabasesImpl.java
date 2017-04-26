/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlDatabases;
import com.microsoft.azure.management.sql.SqlServer;
import rx.Completable;

import java.util.List;

/**
 * Implementation of SqlServer.Databases, which enables the creating the database from the SQLServer directly.
 */
@LangDefinition
public class DatabasesImpl implements SqlServer.Databases {

    private final String resourceGroupName;
    private final String sqlServerName;
    private final SqlDatabases.SqlDatabaseCreatable databases;
    private final Region region;

    DatabasesImpl(SqlServerManager manager, String resourceGroupName, String sqlServerName, Region region) {
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.region = region;
        this.databases = new SqlDatabasesImpl(manager);
    }

    protected SqlDatabases databases() {
        return this.databases;
    }

    @Override
    public SqlDatabase get(String databaseName) {
        return this.databases().getBySqlServer(
                this.resourceGroupName, this.sqlServerName, databaseName);
}

    @Override
    public SqlDatabase.DefinitionStages.Blank define(String databaseName) {
        return this.databases.definedWithSqlServer(
                this.resourceGroupName, this.sqlServerName, databaseName, this.region);
    }

    @Override
    public List<SqlDatabase> list() {
        return this.databases().listBySqlServer(
                this.resourceGroupName, this.sqlServerName);
    }

    @Override
    public void delete(String databaseName) {
        this.databases().deleteByParent(
                this.resourceGroupName, this.sqlServerName, databaseName);
    }

    @Override
    public Completable deleteAsync(String databaseName) {
        return this.databases().deleteByParentAsync(
                this.resourceGroupName, this.sqlServerName, databaseName);
    }
}
