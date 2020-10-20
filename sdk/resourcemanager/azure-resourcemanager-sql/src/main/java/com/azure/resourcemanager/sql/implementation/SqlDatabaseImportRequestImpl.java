// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.dag.FunctionalTaskItem;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.ExecutableImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.AuthenticationType;
import com.azure.resourcemanager.sql.models.ExtensionName;
import com.azure.resourcemanager.sql.models.ImportExtensionRequest;
import com.azure.resourcemanager.sql.models.ImportOperationMode;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlDatabaseImportExportResponse;
import com.azure.resourcemanager.sql.models.SqlDatabaseImportRequest;
import com.azure.resourcemanager.sql.models.StorageKeyType;
import com.azure.resourcemanager.storage.models.StorageAccount;
import reactor.core.publisher.Mono;

import java.util.Objects;

/** Implementation for SqlDatabaseImportRequest. */
public class SqlDatabaseImportRequestImpl extends ExecutableImpl<SqlDatabaseImportExportResponse>
    implements SqlDatabaseImportRequest, SqlDatabaseImportRequest.SqlDatabaseImportRequestDefinition {

    private final SqlDatabaseImpl sqlDatabase;
    private final SqlServerManager sqlServerManager;
    private ImportExtensionRequest inner;

    SqlDatabaseImportRequestImpl(SqlDatabaseImpl sqlDatabase, SqlServerManager sqlServerManager) {
        this.sqlDatabase = sqlDatabase;
        this.sqlServerManager = sqlServerManager;
        this.inner = new ImportExtensionRequest();
    }

    @Override
    public SqlDatabase parent() {
        return null;
    }

    @Override
    public ImportExtensionRequest innerModel() {
        return this.inner;
    }

    @Override
    public Mono<SqlDatabaseImportExportResponse> executeWorkAsync() {
        final SqlDatabaseImportRequestImpl self = this;
        return this
            .sqlServerManager
            .serviceClient()
            .getDatabases()
            .createImportOperationAsync(
                this.sqlDatabase.resourceGroupName,
                this.sqlDatabase.sqlServerName,
                this.sqlDatabase.name(),
                ExtensionName.IMPORT,
                this.innerModel())
            .flatMap(
                importExportResponseInner ->
                    self
                        .sqlDatabase
                        .refreshAsync()
                        .map(sqlDatabase -> new SqlDatabaseImportExportResponseImpl(importExportResponseInner)));
    }

    private Mono<Indexable> getOrCreateStorageAccountContainer(
        final StorageAccount storageAccount,
        final String containerName,
        final String fileName,
        final FunctionalTaskItem.Context context) {
        final SqlDatabaseImportRequestImpl self = this;
        return storageAccount
            .getKeysAsync()
            .flatMap(storageAccountKeys -> Mono.justOrEmpty(storageAccountKeys.stream().findFirst()))
            .flatMap(
                storageAccountKey -> {
                    self
                        .inner
                        .withStorageUri(
                            String
                                .format(
                                    "%s%s/%s", storageAccount.endPoints().primary().blob(), containerName, fileName));
                    self.inner.withStorageKeyType(StorageKeyType.STORAGE_ACCESS_KEY);
                    self.inner.withStorageKey(storageAccountKey.value());
                    return context.voidMono();
                });
    }

    @Override
    public SqlDatabaseImportRequestImpl importFrom(String storageUri) {
        if (this.inner == null) {
            this.inner = new ImportExtensionRequest();
        }
        this.inner.withStorageUri(storageUri);
        this.inner.withOperationMode(ImportOperationMode.IMPORT);
        return this;
    }

    @Override
    public SqlDatabaseImportRequestImpl importFrom(
        final StorageAccount storageAccount, final String containerName, final String fileName) {
        Objects.requireNonNull(storageAccount);
        Objects.requireNonNull(containerName);
        Objects.requireNonNull(fileName);
        if (this.inner == null) {
            this.inner = new ImportExtensionRequest();
        }
        this.inner.withOperationMode(ImportOperationMode.IMPORT);
        this
            .addDependency(
                context -> getOrCreateStorageAccountContainer(storageAccount, containerName, fileName, context));
        return this;
    }

    @Override
    public SqlDatabaseImportRequestImpl withStorageAccessKey(String storageAccessKey) {
        this.inner.withStorageKeyType(StorageKeyType.STORAGE_ACCESS_KEY);
        this.inner.withStorageKey(storageAccessKey);
        return this;
    }

    @Override
    public SqlDatabaseImportRequestImpl withSharedAccessKey(String sharedAccessKey) {
        this.inner.withStorageKeyType(StorageKeyType.SHARED_ACCESS_KEY);
        this.inner.withStorageKey(sharedAccessKey);
        return this;
    }

    @Override
    public SqlDatabaseImportRequestImpl withSqlAdministratorLoginAndPassword(
        String administratorLogin, String administratorPassword) {
        this.inner.withAuthenticationType(AuthenticationType.SQL);
        return this.withLoginAndPassword(administratorLogin, administratorPassword);
    }

    @Override
    public SqlDatabaseImportRequestImpl withActiveDirectoryLoginAndPassword(
        String administratorLogin, String administratorPassword) {
        this.inner.withAuthenticationType(AuthenticationType.ADPASSWORD);
        return this.withLoginAndPassword(administratorLogin, administratorPassword);
    }

    SqlDatabaseImportRequestImpl withLoginAndPassword(String administratorLogin, String administratorPassword) {
        this.inner.withAdministratorLogin(administratorLogin);
        this.inner.withAdministratorLoginPassword(administratorPassword);
        return this;
    }
}
