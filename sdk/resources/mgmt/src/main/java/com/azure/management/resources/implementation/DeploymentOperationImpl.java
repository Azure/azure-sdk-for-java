// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.implementation;

import com.azure.management.resources.DeploymentOperation;
import com.azure.management.resources.TargetResource;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import com.azure.management.resources.models.DeploymentOperationInner;
import com.azure.management.resources.models.DeploymentOperationsInner;
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

    private final DeploymentOperationsInner client;

    DeploymentOperationImpl(DeploymentOperationInner innerModel, final DeploymentOperationsInner client) {
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
