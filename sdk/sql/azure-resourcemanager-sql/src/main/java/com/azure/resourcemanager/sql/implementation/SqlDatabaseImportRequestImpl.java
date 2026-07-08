// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.resources.fluentcore.dag.FunctionalTaskItem;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.ExecutableImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.AuthenticationType;
import com.azure.resourcemanager.sql.models.ImportExistingDatabaseDefinition;
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
    private ImportExistingDatabaseDefinition inner;

    SqlDatabaseImportRequestImpl(SqlDatabaseImpl sqlDatabase, SqlServerManager sqlServerManager) {
        this.sqlDatabase = sqlDatabase;
        this.sqlServerManager = sqlServerManager;
        this.inner = new ImportExistingDatabaseDefinition();
    }

    @Override
    public SqlDatabase parent() {
        return null;
    }

    @Override
    public ImportExistingDatabaseDefinition innerModel() {
        return this.inner;
    }

    @Override
    public Mono<SqlDatabaseImportExportResponse> executeWorkAsync() {
        final SqlDatabaseImportRequestImpl self = this;
        return this.sqlServerManager.serviceClient()
            .getDatabases()
            .importMethodAsync(this.sqlDatabase.resourceGroupName, this.sqlDatabase.sqlServerName,
                this.sqlDatabase.name(), this.innerModel())
            .flatMap(importExportResponseInner -> self.sqlDatabase.refreshAsync()
                .map(sqlDatabase -> new SqlDatabaseImportExportResponseImpl(importExportResponseInner)));
    }

    private Mono<Indexable> getOrCreateStorageAccountContainer(final StorageAccount storageAccount,
        final String containerName, final String fileName, final FunctionalTaskItem.Context context) {
        final SqlDatabaseImportRequestImpl self = this;
        self.inner.withStorageUri(
            String.format("%s%s/%s", storageAccount.endPoints().primary().blob(), containerName, fileName));

        if (!storageAccount.isSharedKeyAccessAllowed()
            // self.inner.storageKey could be set before, e.g. with managed identity ID
            || !CoreUtils.isNullOrEmpty(self.inner.storageKey())) {
            return context.voidMono();
        }

        return storageAccount.getKeysAsync()
            .flatMap(storageAccountKeys -> Mono.justOrEmpty(storageAccountKeys.stream().findFirst()))
            .flatMap(storageAccountKey -> {
                self.inner.withStorageKeyType(StorageKeyType.STORAGE_ACCESS_KEY);
                self.inner.withStorageKey(storageAccountKey.value());
                return context.voidMono();
            });
    }

    @Override
    public SqlDatabaseImportRequestImpl importFrom(String storageUri) {
        if (this.inner == null) {
            this.inner = new ImportExistingDatabaseDefinition();
        }
        this.inner.withStorageUri(storageUri);
        return this;
    }

    @Override
    public SqlDatabaseImportRequestImpl importFrom(final StorageAccount storageAccount, final String containerName,
        final String fileName) {
        Objects.requireNonNull(storageAccount);
        Objects.requireNonNull(containerName);
        Objects.requireNonNull(fileName);
        if (this.inner == null) {
            this.inner = new ImportExistingDatabaseDefinition();
        }
        this.addDependency(
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
    public SqlDatabaseImportRequestImpl withSqlAdministratorLoginAndPassword(String administratorLogin,
        String administratorPassword) {
        this.inner.withAuthenticationType(AuthenticationType.SQL.toString());
        return this.withLoginAndPassword(administratorLogin, administratorPassword);
    }

    @Override
    public SqlDatabaseImportRequestImpl withActiveDirectoryLoginAndPassword(String administratorLogin,
        String administratorPassword) {
        this.inner.withAuthenticationType(AuthenticationType.ADPASSWORD.toString());
        return this.withLoginAndPassword(administratorLogin, administratorPassword);
    }

    @Override
    public SqlDatabaseImportRequestImpl withManagedIdentity(String managedIdentityResourceId) {
        Objects.requireNonNull(managedIdentityResourceId);
        this.inner.withAuthenticationType(AuthenticationType.MANAGED_IDENTITY.toString());
        this.inner.withAdministratorLogin(managedIdentityResourceId);
        // No administrator password is required for managed identity authentication.
        this.inner.withAdministratorLoginPassword(null);

        // Use the same MI for storage account access.
        this.inner.withStorageKeyType(StorageKeyType.MANAGED_IDENTITY);
        this.inner.withStorageKey(managedIdentityResourceId);
        return this;
    }

    SqlDatabaseImportRequestImpl withLoginAndPassword(String administratorLogin, String administratorPassword) {
        this.inner.withAdministratorLogin(administratorLogin);
        this.inner.withAdministratorLoginPassword(administratorPassword);
        return this;
    }
}
