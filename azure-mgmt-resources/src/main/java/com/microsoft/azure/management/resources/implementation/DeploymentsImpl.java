package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.Deployments;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.DeploymentsInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupsInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.models.Deployment;
import com.microsoft.azure.management.resources.models.implementation.DeploymentImpl;
import com.microsoft.azure.management.resources.models.implementation.api.DeploymentExtendedInner;

import java.io.IOException;
import java.util.List;

public final class DeploymentsImpl
    implements Deployments {

    ResourceManagementClientImpl serviceClient;
    DeploymentsInner deployments;
    ResourceGroupsInner resourceGroups;
    String resourceGroupName;

    public DeploymentsImpl(ResourceManagementClientImpl serviceClient) {
        this.serviceClient = serviceClient;
        this.deployments = serviceClient.deployments();
        this.resourceGroups = serviceClient.resourceGroups();
    }

    public DeploymentsImpl(ResourceManagementClientImpl serviceClient, String resourceGroupName) throws CloudException, IOException {
        this.serviceClient = serviceClient;
        this.deployments = serviceClient.deployments();
        this.resourceGroups = serviceClient.resourceGroups();
        this.resourceGroupName = resourceGroupName;
    }

    @Override
    public List<Deployment> list() throws CloudException, IOException {
        PagedListConverter<DeploymentExtendedInner, Deployment> converter = new PagedListConverter<DeploymentExtendedInner, Deployment>() {
            @Override
            public Deployment typeConvert(DeploymentExtendedInner deploymentInner) {
                return new DeploymentImpl(resourceGroupName, deploymentInner.name(), deploymentInner, deployments);
            }
        };
        return converter.convert(deployments.list(resourceGroupName).getBody());
    }

    @Override
    public boolean checkExistence(String resourceGroupName, String deploymentName) throws IOException, CloudException {
        return deployments.checkExistence(resourceGroupName, deploymentName).getBody();
    }

    @Override
    public Deployment define(String name) throws Exception {
        return null;
    }

    @Override
    public Deployment get(String name) throws IOException, CloudException {
        return new DeploymentImpl(resourceGroupName, name, deployments.get(resourceGroupName, name).getBody(), deployments);
    }
}
