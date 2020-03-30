/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

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
        this.resourceGroupName = ResourceUtils.groupFromResourceId(innerModel.getId());
        this.deploymentName = ResourceUtils.extractFromResourceId(innerModel.getId(), "deployments");
    }

    @Override
    public String operationId() {
        return inner().getOperationId();
    }

    @Override
    public String provisioningState() {
        if (this.inner().getProperties() == null) {
            return null;
        }
        return this.inner().getProperties().getProvisioningState();
    }

    @Override
    public OffsetDateTime timestamp() {
        if (this.inner().getProperties() == null) {
            return null;
        }
        return this.inner().getProperties().getTimestamp();
    }

    @Override
    public String statusCode() {
        if (this.inner().getProperties() == null) {
            return null;
        }
        return this.inner().getProperties().getStatusCode();
    }

    @Override
    public Object statusMessage() {
        if (this.inner().getProperties() == null) {
            return null;
        }
        return this.inner().getProperties().getStatusMessage();
    }

    @Override
    public TargetResource targetResource() {
        if (this.inner().getProperties() == null) {
            return null;
        }
        return this.inner().getProperties().getTargetResource();
    }

    @Override
    protected Mono<DeploymentOperationInner> getInnerAsync() {
        return client.getAsync(resourceGroupName, deploymentName, operationId());
    }
}
