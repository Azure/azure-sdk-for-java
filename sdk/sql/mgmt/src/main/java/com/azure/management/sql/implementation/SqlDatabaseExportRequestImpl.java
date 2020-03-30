/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;

import com.azure.management.resources.fluentcore.dag.FunctionalTaskItem;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.model.Indexable;
import com.azure.management.resources.fluentcore.model.implementation.ExecutableImpl;
import com.azure.management.sql.AuthenticationType;
import com.azure.management.sql.ExportRequest;
import com.azure.management.sql.SqlDatabase;
import com.azure.management.sql.SqlDatabaseExportRequest;
import com.azure.management.sql.SqlDatabaseImportExportResponse;
import com.azure.management.sql.StorageKeyType;
import com.azure.management.storage.StorageAccount;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.implementation.models.StorageErrorException;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Implementation for SqlDatabaseExportRequest.
 */
public class SqlDatabaseExportRequestImpl extends ExecutableImpl<SqlDatabaseImportExportResponse>
    implements
        SqlDatabaseExportRequest,
        SqlDatabaseExportRequest.SqlDatabaseExportRequestDefinition {

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
    public ExportRequest inner() {
        return this.inner;
    }

    @Override
    public Mono<SqlDatabaseImportExportResponse> executeWorkAsync() {
        return this.sqlServerManager.inner().databases()
            .exportAsync(this.sqlDatabase.resourceGroupName, this.sqlDatabase.sqlServerName, this.sqlDatabase.name(), this.inner())
            .map(importExportResponseInner -> new SqlDatabaseImportExportResponseImpl(importExportResponseInner));
    }

    @Override
    public SqlDatabaseExportRequestImpl exportTo(String storageUri) {
        if (this.inner == null) {
            this.inner = new ExportRequest();
        }
        this.inner.withStorageUri(storageUri);
        return this;
    }

    private Mono<Indexable> getOrCreateStorageAccountContainer(final StorageAccount storageAccount, final String containerName, final String fileName, final FunctionalTaskItem.Context context) {
        final SqlDatabaseExportRequestImpl self = this;
        return storageAccount.getKeysAsync()
            .flatMap(storageAccountKeys -> Mono.justOrEmpty(storageAccountKeys.stream().findFirst()))
            .flatMap(storageAccountKey -> {
                self.inner.withStorageUri(String.format("%s%s/%s", storageAccount.endPoints().primary().getBlob(), containerName, fileName));
                self.inner.withStorageKeyType(StorageKeyType.STORAGE_ACCESS_KEY);
                self.inner.withStorageKey(storageAccountKey.getValue());
                try {
                    BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                            .endpoint(storageAccount.endPoints().primary().getBlob())
                            .sasToken(storageAccountKey.getValue())
                            .buildClient();
                    blobServiceClient.createBlobContainer(containerName);
                } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    throw Exceptions.propagate(indexOutOfBoundsException);
                } catch (StorageErrorException stgException) {
                    throw Exceptions.propagate(stgException);
                }
                return context.voidMono();
            });
    }

    @Override
    public SqlDatabaseExportRequestImpl exportTo(final StorageAccount storageAccount, final String containerName, final String fileName) {
        Objects.requireNonNull(storageAccount);
        Objects.requireNonNull(containerName);
        Objects.requireNonNull(fileName);
        if (this.inner == null) {
            this.inner = new ExportRequest();
        }
        final SqlDatabaseExportRequestImpl self = this;
        this.addDependency(context -> getOrCreateStorageAccountContainer(storageAccount, containerName, fileName, context));
        return this;
    }

    @Override
    public SqlDatabaseExportRequestImpl exportTo(final Creatable<StorageAccount> storageAccountCreatable, final String containerName, final String fileName) {
        if (this.inner == null) {
            this.inner = new ExportRequest();
        }
        this.addDependency(context -> storageAccountCreatable.createAsync()
            .last()
            .flatMap(storageAccount -> getOrCreateStorageAccountContainer((StorageAccount) storageAccount, containerName, fileName, context)));
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
    public SqlDatabaseExportRequestImpl withSqlAdministratorLoginAndPassword(String administratorLogin, String administratorPassword) {
        this.inner.withAuthenticationType(AuthenticationType.SQL);
        return this.withLoginAndPassword(administratorLogin, administratorPassword);
    }

    @Override
    public SqlDatabaseExportRequestImpl withActiveDirectoryLoginAndPassword(String administratorLogin, String administratorPassword) {
        this.inner.withAuthenticationType(AuthenticationType.ADPASSWORD);
        return this.withLoginAndPassword(administratorLogin, administratorPassword);
    }

    SqlDatabaseExportRequestImpl withLoginAndPassword(String administratorLogin, String administratorPassword) {
        this.inner.withAdministratorLogin(administratorLogin);
        this.inner.withAdministratorLoginPassword(administratorPassword);
        return this;
    }
}
