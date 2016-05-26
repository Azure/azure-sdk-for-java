/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.DeploymentOperations;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupPagedList;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.DeploymentOperationsInner;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.DeploymentOperation;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.api.DeploymentOperationInner;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.List;

/**
 * An instance of this class provides access to deployment operations in Azure.
 */
final class DeploymentOperationsImpl
        implements DeploymentOperations {
    private final DeploymentOperationsInner client;
    private final Deployment deployment;
    private final ResourceGroups resourceGroups;
    private final PagedListConverter<DeploymentOperationInner, DeploymentOperation> converter;

    DeploymentOperationsImpl(final DeploymentOperationsInner client,
                                    final Deployment deployment,
                                    final ResourceGroups resourceGroups) {
        this.client = client;
        this.deployment = deployment;
        this.resourceGroups = resourceGroups;
        converter = new PagedListConverter<DeploymentOperationInner, DeploymentOperation>() {
            @Override
            public DeploymentOperation typeConvert(DeploymentOperationInner deploymentInner) {
                return createFluentModel(deploymentInner);
            }
        };
    }

    @Override
    public InGroup resourceGroup(ResourceGroup resourceGroup) {
        return new DeploymentOperationsInGroupImpl(this, resourceGroup);
    }

    @Override
    public PagedList<DeploymentOperation> list() throws CloudException, IOException {
        return new GroupPagedList<DeploymentOperation>(resourceGroups.list()) {
            @Override
            public List<DeploymentOperation> listNextGroup(String resourceGroupName) throws RestException, IOException {
                return converter.convert(client.list(resourceGroupName, deployment.name()).getBody());
            }
        };
    }

    @Override
    public PagedList<DeploymentOperation> list(String groupName) throws CloudException, IOException {
        return converter.convert(client.list(groupName, deployment.name()).getBody());
    }

    @Override
    public DeploymentOperation get(String operationId) throws IOException, CloudException {
        for (ResourceGroup group : resourceGroups.list()) {
            try {
                DeploymentOperationInner inner = client.get(group.name(), deployment.name(), operationId).getBody();
                if (inner != null) {
                    return createFluentModel(inner);
                }
            } catch (CloudException ex) {
                // Do nothing
            }
        }
        return null;
    }

    @Override
    public DeploymentOperation get(String groupName, String operationId) throws IOException, CloudException {
        return createFluentModel(client.get(groupName, deployment.name(), operationId).getBody());
    }

    private DeploymentOperationImpl createFluentModel(DeploymentOperationInner deploymentOperationInner) {
        return new DeploymentOperationImpl(deploymentOperationInner, this.client);
    }
}
