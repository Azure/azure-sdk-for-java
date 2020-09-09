// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.resourcemanager.resources.models.DeploymentOperation;
import com.azure.resourcemanager.resources.models.ProvisioningOperation;
import com.azure.resourcemanager.resources.models.TargetResource;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import com.azure.resourcemanager.resources.fluent.inner.DeploymentOperationInner;
import com.azure.resourcemanager.resources.fluent.DeploymentOperationsClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/**
 * The implementation of {@link DeploymentOperation}.
 */
final class DeploymentOperationImpl extends
        IndexableRefreshableWrapperImpl<DeploymentOperation, DeploymentOperationInner>
        implements
        DeploymentOperation {
    private String resourceGroupName;
    private String deploymentName;

    private final DeploymentOperationsClient client;

    DeploymentOperationImpl(DeploymentOperationInner innerModel, final DeploymentOperationsClient client) {
        super(innerModel);
        this.client = client;
        this.resourceGroupName = ResourceUtils.groupFromResourceId(innerModel.id());
        this.deploymentName = ResourceUtils.extractFromResourceId(innerModel.id(), "deployments");
    }

    @Override
    public String operationId() {
        return inner().operationId();
    }

    @Override
    public String provisioningState() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().provisioningState();
    }

    @Override
    public ProvisioningOperation provisioningOperation() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().provisioningOperation();
    }

    @Override
    public OffsetDateTime timestamp() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().timestamp();
    }

    @Override
    public String statusCode() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().statusCode();
    }

    @Override
    public Object statusMessage() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().statusMessage();
    }

    @Override
    public TargetResource targetResource() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().targetResource();
    }

    @Override
    protected Mono<DeploymentOperationInner> getInnerAsync() {
        return client.getAsync(resourceGroupName, deploymentName, operationId());
    }
}
