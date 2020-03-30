/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;

import com.azure.management.resources.fluentcore.dag.FunctionalTaskItem;
import com.azure.management.resources.fluentcore.model.Indexable;
import com.azure.management.resources.fluentcore.model.implementation.ExecutableImpl;
import com.azure.management.sql.AuthenticationType;
import com.azure.management.sql.ImportExtensionRequest;
import com.azure.management.sql.SqlDatabase;
import com.azure.management.sql.SqlDatabaseImportExportResponse;
import com.azure.management.sql.SqlDatabaseImportRequest;
import com.azure.management.sql.StorageKeyType;
import com.azure.management.storage.StorageAccount;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Implementation for SqlDatabaseImportRequest.
 */
public class SqlDatabaseImportRequestImpl extends ExecutableImpl<SqlDatabaseImportExportResponse>
    implements
        SqlDatabaseImportRequest,
    SqlDatabaseImportRequest.SqlDatabaseImportRequestDefinition {

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
    public ImportExtensionRequest inner() {
        return this.inner;
    }

    @Override
    public Mono<SqlDatabaseImportExportResponse> executeWorkAsync() {
        final SqlDatabaseImportRequestImpl self = this;
        return this.sqlServerManager.inner().databases()
            .createImportOperationAsync(this.sqlDatabase.resourceGroupName, this.sqlDatabase.sqlServerName, this.sqlDatabase.name(), this.inner())
            .flatMap(importExportResponseInner -> self.sqlDatabase
                .refreshAsync()
                .map(sqlDatabase -> new SqlDatabaseImportExportResponseImpl(importExportResponseInner)));
    }

    private Mono<Indexable> getOrCreateStorageAccountContainer(final StorageAccount storageAccount, final String containerName, final String fileName, final FunctionalTaskItem.Context context) {
        final SqlDatabaseImportRequestImpl self = this;
        return storageAccount.getKeysAsync()
            .flatMap(storageAccountKeys -> Mono.justOrEmpty(storageAccountKeys.stream().findFirst()))
            .flatMap(storageAccountKey -> {
                    self.inner.withStorageUri(String.format("%s%s/%s", storageAccount.endPoints().primary().getBlob(), containerName, fileName));
                    self.inner.withStorageKeyType(StorageKeyType.STORAGE_ACCESS_KEY);
                    self.inner.withStorageKey(storageAccountKey.getValue());
                    return context.voidMono();
            });
    }

    @Override
    public SqlDatabaseImportRequestImpl importFrom(String storageUri) {
        if (this.inner == null) {
            this.inner = new ImportExtensionRequest();
        }
        this.inner.withStorageUri(storageUri);
        return this;
    }

    @Override
    public SqlDatabaseImportRequestImpl importFrom(final StorageAccount storageAccount, final String containerName, final String fileName) {
        Objects.requireNonNull(storageAccount);
        Objects.requireNonNull(containerName);
        Objects.requireNonNull(fileName);
        if (this.inner == null) {
            this.inner = new ImportExtensionRequest();
        }
        final SqlDatabaseImportRequestImpl self = this;
        this.addDependency(context -> getOrCreateStorageAccountContainer(storageAccount, containerName, fileName, context));
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
    public SqlDatabaseImportRequestImpl withSqlAdministratorLoginAndPassword(String administratorLogin, String administratorPassword) {
        this.inner.withAuthenticationType(AuthenticationType.SQL);
        return this.withLoginAndPassword(administratorLogin, administratorPassword);
    }

    @Override
    public SqlDatabaseImportRequestImpl withActiveDirectoryLoginAndPassword(String administratorLogin, String administratorPassword) {
        this.inner.withAuthenticationType(AuthenticationType.ADPASSWORD);
        return this.withLoginAndPassword(administratorLogin, administratorPassword);
    }

    SqlDatabaseImportRequestImpl withLoginAndPassword(String administratorLogin, String administratorPassword) {
        this.inner.withAdministratorLogin(administratorLogin);
        this.inner.withAdministratorLoginPassword(administratorPassword);
        return this;
    }
}
