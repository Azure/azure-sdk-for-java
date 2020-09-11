// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.models.Deployment;
import com.azure.resourcemanager.resources.models.DeploymentOperation;
import com.azure.resourcemanager.resources.models.DeploymentOperations;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.azure.resourcemanager.resources.fluent.inner.DeploymentOperationInner;
import com.azure.resourcemanager.resources.fluent.DeploymentOperationsClient;
import reactor.core.publisher.Mono;

/**
 * The implementation of {@link DeploymentOperations}.
 */
final class DeploymentOperationsImpl
        extends ReadableWrappersImpl<DeploymentOperation, DeploymentOperationImpl, DeploymentOperationInner>
        implements DeploymentOperations {
    private final DeploymentOperationsClient client;
    private final Deployment deployment;

    DeploymentOperationsImpl(final DeploymentOperationsClient client,
                             final Deployment deployment) {
        this.client = client;
        this.deployment = deployment;
    }

    @Override
    public PagedIterable<DeploymentOperation> list() {
        return wrapList(client.listByResourceGroup(deployment.resourceGroupName(), deployment.name()));
    }

    @Override
    public DeploymentOperation getById(String operationId) {
        return getByIdAsync(operationId).block();
    }

    @Override
    public Mono<DeploymentOperation> getByIdAsync(String operationId) {
        return client.getAsync(deployment.resourceGroupName(), deployment.name(), operationId)
            .map(deploymentOperationInner -> wrapModel(deploymentOperationInner));
    }

    @Override
    protected DeploymentOperationImpl wrapModel(DeploymentOperationInner inner) {
        if (inner == null) {
            return null;
        }
        return new DeploymentOperationImpl(inner, this.client);
    }

    @Override
    public PagedFlux<DeploymentOperation> listAsync() {
        return wrapPageAsync(this.client
            .listAtManagementGroupScopeAsync(deployment.resourceGroupName(), deployment.name()));
    }
}
