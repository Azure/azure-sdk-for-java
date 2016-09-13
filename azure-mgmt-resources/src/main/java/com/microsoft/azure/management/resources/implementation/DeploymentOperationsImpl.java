/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.DeploymentOperation;
import com.microsoft.azure.management.resources.DeploymentOperations;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import java.io.IOException;

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
    public PagedList<DeploymentOperation> list() throws CloudException, IOException {
        return wrapList(client.list(deployment.resourceGroupName(), deployment.name()));
    }

    @Override
    public DeploymentOperation getById(String operationId) throws CloudException, IllegalArgumentException, IOException {
        return wrapModel(client.get(deployment.resourceGroupName(), deployment.name(), operationId));
    }

    @Override
    protected DeploymentOperationImpl wrapModel(DeploymentOperationInner inner) {
        return new DeploymentOperationImpl(inner, this.client);
    }
}
