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
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;
import rx.functions.Func1;

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
        return getByIdAsync(operationId).toBlocking().last();
    }

    @Override
    public Observable<DeploymentOperation> getByIdAsync(String operationId) {
        return client.getAsync(deployment.resourceGroupName(), deployment.name(), operationId).map(new Func1<DeploymentOperationInner, DeploymentOperation>() {
            @Override
            public DeploymentOperation call(DeploymentOperationInner deploymentOperationInner) {
                return wrapModel(deploymentOperationInner);
            }
        });
    }

    @Override
    public ServiceFuture<DeploymentOperation> getByIdAsync(String id, ServiceCallback<DeploymentOperation> callback) {
        return ServiceFuture.fromBody(getByIdAsync(id), callback);
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
