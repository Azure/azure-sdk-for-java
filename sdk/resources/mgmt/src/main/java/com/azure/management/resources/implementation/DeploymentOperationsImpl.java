/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.Deployment;
import com.azure.management.resources.DeploymentOperation;
import com.azure.management.resources.DeploymentOperations;
import com.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.azure.management.resources.models.DeploymentOperationInner;
import com.azure.management.resources.models.DeploymentOperationsInner;
import reactor.core.publisher.Mono;

/**
 * The implementation of {@link DeploymentOperations}.
 */
final class DeploymentOperationsImpl
        extends ReadableWrappersImpl<DeploymentOperation, DeploymentOperationImpl, DeploymentOperationInner>
        implements DeploymentOperations {
    private final DeploymentOperationsInner client;
    private final Deployment deployment;

    DeploymentOperationsImpl(final DeploymentOperationsInner client,
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
        return client.getAsync(deployment.resourceGroupName(), deployment.name(), operationId).map(deploymentOperationInner -> wrapModel(deploymentOperationInner));
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
        return wrapPageAsync(this.client.listAtManagementGroupScopeAsync(deployment.resourceGroupName(), deployment.name()));
    }
}
