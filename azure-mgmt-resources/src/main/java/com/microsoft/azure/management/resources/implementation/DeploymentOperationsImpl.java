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
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.DeploymentOperationInner;
import com.microsoft.azure.management.resources.implementation.api.DeploymentOperationsInner;

import java.io.IOException;

/**
 * The implementation of DeploymentOperations and its parent interfaces.
 */
final class DeploymentOperationsImpl
        implements DeploymentOperations {
    private final DeploymentOperationsInner client;
    private final Deployment deployment;
    private final PagedListConverter<DeploymentOperationInner, DeploymentOperation> converter;

    DeploymentOperationsImpl(final DeploymentOperationsInner client,
                                    final Deployment deployment) {
        this.client = client;
        this.deployment = deployment;
        converter = new PagedListConverter<DeploymentOperationInner, DeploymentOperation>() {
            @Override
            public DeploymentOperation typeConvert(DeploymentOperationInner deploymentInner) {
                return createFluentModel(deploymentInner);
            }
        };
    }

    @Override
    public PagedList<DeploymentOperation> list() throws CloudException, IOException {
        return converter.convert(client.list(deployment.resourceGroupName(), deployment.name()).getBody());
    }

    @Override
    public DeploymentOperation get(String operationId) throws IOException, CloudException {
        return createFluentModel(client.get(deployment.resourceGroupName(), deployment.name(), operationId).getBody());
    }

    private DeploymentOperationImpl createFluentModel(DeploymentOperationInner deploymentOperationInner) {
        return new DeploymentOperationImpl(deploymentOperationInner, this.client);
    }
}
