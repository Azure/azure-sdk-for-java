// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.resources.fluentcore.dag.FunctionalTaskItem;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.ExecutableImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.AuthenticationType;
import com.azure.resourcemanager.sql.models.ExportRequest;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlDatabaseExportRequest;
import com.azure.resourcemanager.sql.models.SqlDatabaseImportExportResponse;
import com.azure.resourcemanager.sql.models.StorageKeyType;
import com.azure.resourcemanager.storage.models.BlobContainers;
import com.azure.resourcemanager.storage.models.PublicAccess;
import com.azure.resourcemanager.storage.models.StorageAccount;
import reactor.core.publisher.Mono;

import java.util.Objects;

/** Implementation for SqlDatabaseExportRequest. */
public class SqlDatabaseExportRequestImpl extends ExecutableImpl<SqlDatabaseImportExportResponse>
    implements SqlDatabaseExportRequest, SqlDatabaseExportRequest.SqlDatabaseExportRequestDefinition {

    private final SqlDatabaseImpl sqlDatabase;
    private final SqlServerManager sqlServerManager;
    private ExportRequest inner;

    SqlDatabaseExportRequestImpl(SqlDatabaseImpl sqlDatabase, SqlServerManager sqlServerManager) {
        this.sqlDatabase = sqlDatabase;
        this.sqlServerManager = sqlServerManager;
        this.inner = new ExportRequest();
    }

    @Override
    public SqlDatabase parent() {
        return this.sqlDatabase;
    }

    @Override
    public ExportRequest innerModel() {
        return this.inner;
    }

    @Override
    public Mono<SqlDatabaseImportExportResponse> executeWorkAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getDatabases()
            .exportAsync(
                this.sqlDatabase.resourceGroupName,
                this.sqlDatabase.sqlServerName,
                this.sqlDatabase.name(),
                this.innerModel())
            .map(SqlDatabaseImportExportResponseImpl::new);
    }

    @Override
    public SqlDatabaseExportRequestImpl exportTo(String storageUri) {
        if (this.inner == null) {
            this.inner = new ExportRequest();
        }
        this.inner.withStorageUri(storageUri);
        return this;
    }

    private Mono<Indexable> getOrCreateStorageAccountContainer(
        final StorageAccount storageAccount,
        final String containerName,
        final String fileName,
        final FunctionalTaskItem.Context context) {
        final SqlDatabaseExportRequestImpl self = this;
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
                    BlobContainers blobContainers = this.sqlServerManager.storageManager().blobContainers();
                    return blobContainers
                        .getAsync(parent().resourceGroupName(), storageAccount.name(), containerName)
                        .onErrorResume(
                            error -> {
                                if (error instanceof ManagementException) {
                                    if (((ManagementException) error).getResponse().getStatusCode() == 404) {
                                        return blobContainers
                                            .defineContainer(containerName)
                                            .withExistingBlobService(
                                                parent().resourceGroupName(), storageAccount.name())
                                            .withPublicAccess(PublicAccess.NONE)
                                            .createAsync();
                                    }
                                }
                                return Mono.error(error);
                            });
                });
    }

    @Override
    public SqlDatabaseExportRequestImpl exportTo(
        final StorageAccount storageAccount, final String containerName, final String fileName) {
        Objects.requireNonNull(storageAccount);
        Objects.requireNonNull(containerName);
        Objects.requireNonNull(fileName);
        if (this.inner == null) {
            this.inner = new ExportRequest();
        }
        this
            .addDependency(
                context -> getOrCreateStorageAccountContainer(storageAccount, containerName, fileName, context));
        return this;
    }

    @Override
    public SqlDatabaseExportRequestImpl exportTo(
        final Creatable<StorageAccount> storageAccountCreatable, final String containerName, final String fileName) {
        if (this.inner == null) {
            this.inner = new ExportRequest();
        }
        this
            .addDependency(
                context ->
                    storageAccountCreatable
                        .createAsync()
                        .flatMap(
                            storageAccount ->
                                getOrCreateStorageAccountContainer(storageAccount, containerName, fileName, context)));
        return this;
    }

    SqlDatabaseExportRequestImpl withStorageKeyType(StorageKeyType storageKeyType) {
        this.inner.withStorageKeyType(storageKeyType);
        return this;
    }

    @Override
    public SqlDatabaseExportRequestImpl withStorageAccessKey(String storageAccessKey) {
        this.inner.withStorageKeyType(StorageKeyType.STORAGE_ACCESS_KEY);
        this.inner.withStorageKey(storageAccessKey);
        return this;
    }

    @Override
    public SqlDatabaseExportRequestImpl withSharedAccessKey(String sharedAccessKey) {
        this.inner.withStorageKeyType(StorageKeyType.SHARED_ACCESS_KEY);
        this.inner.withStorageKey(sharedAccessKey);
        return this;
    }

    SqlDatabaseExportRequestImpl withStorageKey(String storageKey) {
        this.inner.withStorageKey(storageKey);
        return this;
    }

    SqlDatabaseExportRequestImpl withAuthenticationType(AuthenticationType authenticationType) {
        this.inner.withAuthenticationType(authenticationType);
        return this;
    }

    @Override
    public SqlDatabaseExportRequestImpl withSqlAdministratorLoginAndPassword(
        String administratorLogin, String administratorPassword) {
        this.inner.withAuthenticationType(AuthenticationType.SQL);
        return this.withLoginAndPassword(administratorLogin, administratorPassword);
    }

    @Override
    public SqlDatabaseExportRequestImpl withActiveDirectoryLoginAndPassword(
        String administratorLogin, String administratorPassword) {
        this.inner.withAuthenticationType(AuthenticationType.ADPASSWORD);
        return this.withLoginAndPassword(administratorLogin, administratorPassword);
    }

    SqlDatabaseExportRequestImpl withLoginAndPassword(String administratorLogin, String administratorPassword) {
        this.inner.withAdministratorLogin(administratorLogin);
        this.inner.withAdministratorLoginPassword(administratorPassword);
        return this;
    }
}
