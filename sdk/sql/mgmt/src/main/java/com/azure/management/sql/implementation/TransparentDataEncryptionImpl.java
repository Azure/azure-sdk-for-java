/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.fluentcore.arm.ResourceId;
import com.azure.management.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.azure.management.sql.TransparentDataEncryption;
import com.azure.management.sql.TransparentDataEncryptionActivity;
import com.azure.management.sql.TransparentDataEncryptionStatus;
import com.azure.management.sql.models.TransparentDataEncryptionActivityInner;
import com.azure.management.sql.models.TransparentDataEncryptionInner;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation for TransparentDataEncryption.
 */
class TransparentDataEncryptionImpl
        extends RefreshableWrapperImpl<TransparentDataEncryptionInner, TransparentDataEncryption>
        implements TransparentDataEncryption {
    private final String sqlServerName;
    private final String resourceGroupName;
    private final SqlServerManager sqlServerManager;
    private final ResourceId resourceId;

    protected TransparentDataEncryptionImpl(String resourceGroupName, String sqlServerName, TransparentDataEncryptionInner innerObject, SqlServerManager sqlServerManager) {
        super(innerObject);
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlServerManager = sqlServerManager;
        this.resourceId = ResourceId.fromString(this.inner().getId());
    }

    @Override
    public String name() {
        return this.inner().getName();
    }

    @Override
    public String id() {
        return this.inner().getId();
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
    public TransparentDataEncryptionStatus status() {
        return this.inner().status();
    }

    @Override
    public TransparentDataEncryption updateStatus(TransparentDataEncryptionStatus transparentDataEncryptionState) {
        this.inner().withStatus(transparentDataEncryptionState);
        TransparentDataEncryptionInner transparentDataEncryptionInner = this.sqlServerManager.inner().transparentDataEncryptions()
            .createOrUpdate(this.resourceGroupName, this.sqlServerName, this.databaseName(), transparentDataEncryptionState);
        this.setInner(transparentDataEncryptionInner);

        return this;
    }

    @Override
    public Mono<TransparentDataEncryption> updateStatusAsync(TransparentDataEncryptionStatus transparentDataEncryptionState) {
        final TransparentDataEncryptionImpl self = this;
        return this.sqlServerManager.inner().transparentDataEncryptions()
            .createOrUpdateAsync(self.resourceGroupName, self.sqlServerName, self.databaseName(), transparentDataEncryptionState)
            .map(transparentDataEncryptionInner -> {
                self.setInner(transparentDataEncryptionInner);
                return self;
            });
    }

    @Override
    public List<TransparentDataEncryptionActivity> listActivities() {
        List<TransparentDataEncryptionActivity> transparentDataEncryptionActivities = new ArrayList<>();
        PagedIterable<TransparentDataEncryptionActivityInner> transparentDataEncryptionActivityInners = this.sqlServerManager.inner().transparentDataEncryptionActivities()
            .listByConfiguration(this.resourceGroupName, this.sqlServerName, this.databaseName());
        if (transparentDataEncryptionActivityInners != null) {
            for (TransparentDataEncryptionActivityInner transparentDataEncryptionActivityInner : transparentDataEncryptionActivityInners) {
                transparentDataEncryptionActivities.add(new TransparentDataEncryptionActivityImpl(transparentDataEncryptionActivityInner));
            }
        }
        return Collections.unmodifiableList(transparentDataEncryptionActivities);
    }

    @Override
    public PagedFlux<TransparentDataEncryptionActivity> listActivitiesAsync() {
        return this.sqlServerManager.inner().transparentDataEncryptionActivities()
            .listByConfigurationAsync(this.resourceGroupName, this.sqlServerName, this.databaseName())
            .mapPage(transparentDataEncryptionActivityInner -> new TransparentDataEncryptionActivityImpl(transparentDataEncryptionActivityInner));
    }

    @Override
    protected Mono<TransparentDataEncryptionInner> getInnerAsync() {
        return this.sqlServerManager.inner().transparentDataEncryptions().getAsync(this.resourceGroupName, this.sqlServerName, this.databaseName());
    }
}
