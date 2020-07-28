// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.sql.models.CreateMode;
import com.azure.resourcemanager.sql.models.RestorePoint;
import com.azure.resourcemanager.sql.models.SampleName;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlElasticPoolOperations;
import com.azure.resourcemanager.storage.models.StorageAccount;
import java.time.OffsetDateTime;
import java.util.Objects;

/** Implementation for SqlDatabase as inline definition inside a SqlElasticPool definition. */
public class SqlDatabaseForElasticPoolImpl
    implements SqlDatabase.DefinitionStages.WithExistingDatabaseAfterElasticPool<
            SqlElasticPoolOperations.DefinitionStages.WithCreate>,
        SqlDatabase.DefinitionStages.WithStorageKeyAfterElasticPool<
            SqlElasticPoolOperations.DefinitionStages.WithCreate>,
        SqlDatabase.DefinitionStages.WithAuthenticationAfterElasticPool<
            SqlElasticPoolOperations.DefinitionStages.WithCreate>,
        SqlDatabase.DefinitionStages.WithCreateMode<SqlElasticPoolOperations.DefinitionStages.WithCreate>,
        SqlDatabase.DefinitionStages.WithAttachAfterElasticPoolOptions<
            SqlElasticPoolOperations.DefinitionStages.WithCreate> {

    private SqlDatabaseImpl sqlDatabase;
    private SqlElasticPoolImpl sqlElasticPool;

    SqlDatabaseForElasticPoolImpl(SqlElasticPoolImpl sqlElasticPool, SqlDatabaseImpl sqlDatabase) {
        Objects.requireNonNull(sqlElasticPool);
        Objects.requireNonNull(sqlDatabase);
        Objects.requireNonNull(sqlDatabase.inner());
        this.sqlElasticPool = sqlElasticPool;
        this.sqlDatabase = sqlDatabase;
        this.sqlDatabase.inner().withLocation(sqlElasticPool.regionName());
        this.sqlDatabase.inner().withElasticPoolId(this.sqlElasticPool.id());
        this.sqlDatabase.inner().withSku(null);
    }

    @Override
    public SqlElasticPoolImpl attach() {
        return this.sqlElasticPool;
    }

    @Override
    public SqlDatabaseForElasticPoolImpl withSourceDatabase(String sourceDatabaseId) {
        this.sqlDatabase.inner().withSourceDatabaseId(sourceDatabaseId);
        return this;
    }

    @Override
    public SqlDatabaseForElasticPoolImpl withSourceDatabase(SqlDatabase sourceDatabase) {
        this.sqlDatabase.inner().withSourceDatabaseId(sourceDatabase.databaseId());
        return this;
    }

    @Override
    public SqlDatabaseForElasticPoolImpl withMode(CreateMode createMode) {
        this.sqlDatabase.withMode(createMode);
        return this;
    }

    @Override
    public SqlDatabaseForElasticPoolImpl withCollation(String collation) {
        this.sqlDatabase.withCollation(collation);
        return this;
    }

    @Override
    public SqlDatabaseForElasticPoolImpl withMaxSizeBytes(long maxSizeBytes) {
        this.sqlDatabase.withMaxSizeBytes(maxSizeBytes);
        return this;
    }

    @Override
    public SqlDatabaseForElasticPoolImpl importFrom(String storageUri) {
        this.sqlDatabase.importFrom(storageUri);
        return this;
    }

    @Override
    public SqlDatabaseForElasticPoolImpl importFrom(
        StorageAccount storageAccount, String containerName, String fileName) {
        this.sqlDatabase.importFrom(storageAccount, containerName, fileName);
        return this;
    }

    @Override
    public SqlDatabaseForElasticPoolImpl withStorageAccessKey(String storageAccessKey) {
        this.sqlDatabase.withStorageAccessKey(storageAccessKey);
        return this;
    }

    @Override
    public SqlDatabaseForElasticPoolImpl withSharedAccessKey(String sharedAccessKey) {
        this.sqlDatabase.withSharedAccessKey(sharedAccessKey);
        return this;
    }

    @Override
    public SqlDatabaseForElasticPoolImpl withSqlAdministratorLoginAndPassword(
        String administratorLogin, String administratorPassword) {
        this.sqlDatabase.withSqlAdministratorLoginAndPassword(administratorLogin, administratorPassword);
        return this;
    }

    @Override
    public SqlDatabaseForElasticPoolImpl withActiveDirectoryLoginAndPassword(
        String administratorLogin, String administratorPassword) {
        this.sqlDatabase.withActiveDirectoryLoginAndPassword(administratorLogin, administratorPassword);
        return this;
    }

    @Override
    public SqlDatabaseForElasticPoolImpl fromRestorePoint(RestorePoint restorePoint) {
        this.sqlDatabase.fromRestorePoint(restorePoint);
        return this;
    }

    @Override
    public SqlDatabaseForElasticPoolImpl fromRestorePoint(
        RestorePoint restorePoint, OffsetDateTime restorePointDateTime) {
        this.sqlDatabase.fromRestorePoint(restorePoint, restorePointDateTime);
        return this;
    }

    @Override
    public SqlDatabaseForElasticPoolImpl fromSample(SampleName sampleName) {
        this.sqlDatabase.fromSample(sampleName);
        return this;
    }
}
