package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.DeploymentOperations;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupPagedList;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.DeploymentOperationsInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.models.Deployment;
import com.microsoft.azure.management.resources.models.DeploymentOperation;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.resources.models.implementation.DeploymentOperationImpl;
import com.microsoft.azure.management.resources.models.implementation.api.DeploymentOperationInner;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.List;

public class DeploymentOperationsImpl
    implements DeploymentOperations {

    private ResourceManagementClientImpl serviceClient;
    private Deployment deployment;
    private DeploymentOperationsInner deploymentOperations;
    private ResourceGroups resourceGroups;
    private PagedListConverter<DeploymentOperationInner, DeploymentOperation> converter;

    public DeploymentOperationsImpl(ResourceManagementClientImpl serviceClient, Deployment deployment) {
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
    }

    @Override
    public InGroup resourceGroup(String resourceGroupName) {
        return new DeploymentOperationsInGroupImpl(serviceClient, deployment, resourceGroupName);
    }

    @Override
    public List<DeploymentOperation> list() throws CloudException, IOException {
        return new GroupPagedList<DeploymentOperation>(resourceGroups.list()) {
            @Override
            public List<DeploymentOperation> listNextGroup(String resourceGroupName) throws RestException, IOException {
                return converter.convert(deploymentOperations.list(resourceGroupName, deployment.name()).getBody());
            }
        };
    }

    @Override
    public DeploymentOperation get(String operationId) throws IOException, CloudException {
        for (ResourceGroup group : resourceGroups.list()) {
            try {
                DeploymentOperationInner inner = deploymentOperations.get(group.name(), deployment.name(), operationId).getBody();
                if (inner != null) {
                    return new DeploymentOperationImpl(inner, deploymentOperations, resourceGroups);
                }
            } catch (CloudException ex) {
            }
        }
        return null;
    }
}
