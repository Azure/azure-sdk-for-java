// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.fluent.models.LogicalDatabaseTransparentDataEncryptionInner;
import com.azure.resourcemanager.sql.models.TransparentDataEncryption;
import com.azure.resourcemanager.sql.models.TransparentDataEncryptionName;
import com.azure.resourcemanager.sql.models.TransparentDataEncryptionState;
import reactor.core.publisher.Mono;

/** Implementation for TransparentDataEncryption. */
class TransparentDataEncryptionImpl
    extends RefreshableWrapperImpl<LogicalDatabaseTransparentDataEncryptionInner, TransparentDataEncryption>
    implements TransparentDataEncryption {
    private final String sqlServerName;
    private final String resourceGroupName;
    private final SqlServerManager sqlServerManager;
    private final ResourceId resourceId;

    protected TransparentDataEncryptionImpl(String resourceGroupName, String sqlServerName,
        LogicalDatabaseTransparentDataEncryptionInner innerObject, SqlServerManager sqlServerManager) {
        super(innerObject);
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlServerManager = sqlServerManager;
        this.resourceId = ResourceId.fromString(this.innerModel().id());
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }

    @Override
    public String sqlServerName() {
        return this.sqlServerName;
    }

    @Override
    public String databaseName() {
        return resourceId.parent().name();
    }

    @Override
    public TransparentDataEncryptionState status() {
        return this.innerModel().state();
    }

    @Override
    public TransparentDataEncryption updateStatus(TransparentDataEncryptionState transparentDataEncryptionState) {
        this.sqlServerManager.serviceClient()
            .getTransparentDataEncryptions()
            .createOrUpdate(this.resourceGroupName, this.sqlServerName, this.databaseName(),
                TransparentDataEncryptionName.CURRENT,
                new LogicalDatabaseTransparentDataEncryptionInner().withState(transparentDataEncryptionState));
        this.refresh();

        return this;
    }

    @Override
    public Mono<TransparentDataEncryption>
        updateStatusAsync(TransparentDataEncryptionState transparentDataEncryptionState) {
        final TransparentDataEncryptionImpl self = this;
        return this.sqlServerManager.serviceClient()
            .getTransparentDataEncryptions()
            .createOrUpdateAsync(self.resourceGroupName, self.sqlServerName, self.databaseName(),
                TransparentDataEncryptionName.CURRENT,
                new LogicalDatabaseTransparentDataEncryptionInner().withState(transparentDataEncryptionState))
            .then(refreshAsync());
    }

    @Override
    protected Mono<LogicalDatabaseTransparentDataEncryptionInner> getInnerAsync() {
        return this.sqlServerManager.serviceClient()
            .getTransparentDataEncryptions()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.databaseName(),
                TransparentDataEncryptionName.CURRENT);
    }
}
