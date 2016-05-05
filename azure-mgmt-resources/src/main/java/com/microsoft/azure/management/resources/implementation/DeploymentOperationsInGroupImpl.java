package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.*;
import java.io.IOException;

public class DeploymentOperationsInGroupImpl
        implements DeploymentOperations.InGroup {
    private final DeploymentOperations deploymentOperations;
    private final ResourceGroup resourceGroup;

    public DeploymentOperationsInGroupImpl(final DeploymentOperations deploymentOperations, final ResourceGroup resourceGroup) {
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

