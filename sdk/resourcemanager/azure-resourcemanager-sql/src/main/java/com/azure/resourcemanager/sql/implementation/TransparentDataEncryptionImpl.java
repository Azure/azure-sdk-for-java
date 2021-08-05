// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.TransparentDataEncryption;
import com.azure.resourcemanager.sql.models.TransparentDataEncryptionActivity;
import com.azure.resourcemanager.sql.models.TransparentDataEncryptionName;
import com.azure.resourcemanager.sql.models.TransparentDataEncryptionStatus;
import com.azure.resourcemanager.sql.fluent.models.TransparentDataEncryptionActivityInner;
import com.azure.resourcemanager.sql.fluent.models.TransparentDataEncryptionInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** Implementation for TransparentDataEncryption. */
class TransparentDataEncryptionImpl
    extends RefreshableWrapperImpl<TransparentDataEncryptionInner, TransparentDataEncryption>
    implements TransparentDataEncryption {
    private final String sqlServerName;
    private final String resourceGroupName;
    private final SqlServerManager sqlServerManager;
    private final ResourceId resourceId;

    protected TransparentDataEncryptionImpl(
        String resourceGroupName,
        String sqlServerName,
        TransparentDataEncryptionInner innerObject,
        SqlServerManager sqlServerManager) {
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
    public TransparentDataEncryptionStatus status() {
        return this.innerModel().status();
    }

    @Override
    public TransparentDataEncryption updateStatus(TransparentDataEncryptionStatus transparentDataEncryptionState) {
        this.innerModel().withStatus(transparentDataEncryptionState);
        TransparentDataEncryptionInner transparentDataEncryptionInner =
            this
                .sqlServerManager
                .serviceClient()
                .getTransparentDataEncryptions()
                .createOrUpdateWithResponse(
                    this.resourceGroupName,
                    this.sqlServerName,
                    this.databaseName(),
                    TransparentDataEncryptionName.CURRENT,
                    transparentDataEncryptionState,
                    Context.NONE)
                .getValue();
        this.setInner(transparentDataEncryptionInner);

        return this;
    }

    @Override
    public Mono<TransparentDataEncryption> updateStatusAsync(
        TransparentDataEncryptionStatus transparentDataEncryptionState) {
        final TransparentDataEncryptionImpl self = this;
        return this
            .sqlServerManager
            .serviceClient()
            .getTransparentDataEncryptions()
            .createOrUpdateAsync(
                self.resourceGroupName,
                self.sqlServerName,
                self.databaseName(),
                TransparentDataEncryptionName.CURRENT,
                transparentDataEncryptionState)
            .map(
                transparentDataEncryptionInner -> {
                    self.setInner(transparentDataEncryptionInner);
                    return self;
                });
    }

    @Override
    public List<TransparentDataEncryptionActivity> listActivities() {
        List<TransparentDataEncryptionActivity> transparentDataEncryptionActivities = new ArrayList<>();
        PagedIterable<TransparentDataEncryptionActivityInner> transparentDataEncryptionActivityInners =
            this
                .sqlServerManager
                .serviceClient()
                .getTransparentDataEncryptionActivities()
                .listByConfiguration(
                    this.resourceGroupName,
                    this.sqlServerName,
                    this.databaseName(),
                    TransparentDataEncryptionName.CURRENT);
        for (TransparentDataEncryptionActivityInner transparentDataEncryptionActivityInner
            : transparentDataEncryptionActivityInners) {
            transparentDataEncryptionActivities
                .add(new TransparentDataEncryptionActivityImpl(transparentDataEncryptionActivityInner));
        }
        return Collections.unmodifiableList(transparentDataEncryptionActivities);
    }

    @Override
    public PagedFlux<TransparentDataEncryptionActivity> listActivitiesAsync() {
        return PagedConverter.mapPage(this
            .sqlServerManager
            .serviceClient()
            .getTransparentDataEncryptionActivities()
            .listByConfigurationAsync(
                this.resourceGroupName, this.sqlServerName, this.databaseName(), TransparentDataEncryptionName.CURRENT),
            TransparentDataEncryptionActivityImpl::new);
    }

    @Override
    protected Mono<TransparentDataEncryptionInner> getInnerAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getTransparentDataEncryptions()
            .getAsync(
                this.resourceGroupName, this.sqlServerName, this.databaseName(), TransparentDataEncryptionName.CURRENT);
    }
}
