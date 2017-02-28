/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.DeploymentOperation;
import com.microsoft.azure.management.resources.DeploymentOperations;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import rx.Observable;

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
    public PagedList<DeploymentOperation> list() {
        return wrapList(client.listByResourceGroup(deployment.resourceGroupName(), deployment.name()));
    }

    @Override
    public DeploymentOperation getById(String operationId) {
        return wrapModel(client.get(deployment.resourceGroupName(), deployment.name(), operationId));
    }

    @Override
    protected DeploymentOperationImpl wrapModel(DeploymentOperationInner inner) {
        if (inner == null) {
            return null;
        }
        return new DeploymentOperationImpl(inner, this.client);
    }

    @Override
    public Observable<DeploymentOperation> listAsync() {
        return wrapPageAsync(this.client.listByResourceGroupAsync(deployment.resourceGroupName(), deployment.name()));
    }
}
