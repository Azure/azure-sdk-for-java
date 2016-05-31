/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.DeploymentOperation;
import com.microsoft.azure.management.resources.DeploymentOperations;
import com.microsoft.azure.management.resources.ResourceGroup;

import java.io.IOException;

/**
 * An instance of this class provides access to deployment operations in a
 * resource group.
 */
class DeploymentOperationsInGroupImpl
        implements DeploymentOperations.InGroup {
    private final DeploymentOperations deploymentOperations;
    private final ResourceGroup resourceGroup;

    DeploymentOperationsInGroupImpl(final DeploymentOperations deploymentOperations, final ResourceGroup resourceGroup) {
        this.deploymentOperations  = deploymentOperations;
        this.resourceGroup = resourceGroup;
    }

    @Override
    public PagedList<DeploymentOperation> list() throws CloudException, IOException {
        return deploymentOperations.list(this.resourceGroup.name());
    }

    @Override
    public DeploymentOperation get(String operationId) throws IOException, CloudException {
        return deploymentOperations.get(this.resourceGroup.name(), operationId);

    }
}

