package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.DeploymentOperations;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.DeploymentOperationsInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.models.Deployment;
import com.microsoft.azure.management.resources.models.DeploymentOperation;
import com.microsoft.azure.management.resources.models.implementation.DeploymentOperationImpl;
import com.microsoft.azure.management.resources.models.implementation.api.DeploymentOperationInner;

import java.io.IOException;
import java.util.List;

public class DeploymentOperationsInGroupImpl
        implements DeploymentOperations.InGroup {

    private ResourceManagementClientImpl serviceClient;
    private DeploymentOperationsInner deploymentOperations;
    private ResourceGroups resourceGroups;
    private Deployment deployment;
    private PagedListConverter<DeploymentOperationInner, DeploymentOperation> converter;
    private String resourceGroupName;


    public DeploymentOperationsInGroupImpl(ResourceManagementClientImpl serviceClient, Deployment deployment, String resourceGroupName) {
        this.serviceClient = serviceClient;
        this.deploymentOperations = serviceClient.deploymentOperations();
        this.resourceGroups = new ResourceGroupsImpl(serviceClient);
        this.deployment = deployment;
        converter = new PagedListConverter<DeploymentOperationInner, DeploymentOperation>() {
            @Override
            public DeploymentOperation typeConvert(DeploymentOperationInner deploymentInner) {
                return new DeploymentOperationImpl(deploymentInner, deploymentOperations, resourceGroups);
            }
        };
        this.resourceGroupName = resourceGroupName;
    }

    @Override
    public List<DeploymentOperation> list() throws CloudException, IOException {
        return converter.convert(deploymentOperations.list(resourceGroupName, deployment.name()).getBody());
    }

    @Override
    public DeploymentOperation get(String operationId) throws IOException, CloudException {
        return new DeploymentOperationImpl(deploymentOperations.get(resourceGroupName, deployment.name(), operationId).getBody(), deploymentOperations, resourceGroups);
    }
}

