package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.Deployments;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.DeploymentsInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.models.Deployment;
import com.microsoft.azure.management.resources.models.implementation.DeploymentImpl;
import com.microsoft.azure.management.resources.models.implementation.api.DeploymentExtendedInner;
import com.microsoft.rest.ServiceCallback;

import java.io.IOException;
import java.util.List;

public class DeploymentsInGroupImpl
        implements Deployments.InGroup {

    private ResourceManagementClientImpl serviceClient;
    private DeploymentsInner deployments;
    private ResourceGroups resourceGroups;
    private PagedListConverter<DeploymentExtendedInner, Deployment> converter;
    private String resourceGroupName;


    public DeploymentsInGroupImpl(ResourceManagementClientImpl serviceClient, String resourceGroupName) {
        this.serviceClient = serviceClient;
        this.deployments = serviceClient.deployments();
        this.resourceGroups = new ResourceGroupsImpl(serviceClient);
        converter = new PagedListConverter<DeploymentExtendedInner, Deployment>() {
            @Override
            public Deployment typeConvert(DeploymentExtendedInner deploymentInner) {
                return new DeploymentImpl(deploymentInner, deployments, resourceGroups);
            }
        };
        this.resourceGroupName = resourceGroupName;
    }

    @Override
    public List<Deployment> list() throws CloudException, IOException {
        return converter.convert(deployments.list(resourceGroupName).getBody());
    }

    @Override
    public boolean checkExistence(String deploymentName) throws IOException, CloudException {
        return deployments.checkExistence(resourceGroupName, deploymentName).getBody();
    }

    @Override
    public Deployment get(String name) throws IOException, CloudException {
        return new DeploymentImpl(deployments.get(resourceGroupName, name).getBody(), deployments, resourceGroups);
    }

    @Override
    public Deployment.DefinitionWithGroup define(String name) throws Exception {
        DeploymentExtendedInner deployment = new DeploymentExtendedInner();
        deployment.setName(name);
        return new DeploymentImpl(deployment, deployments, new ResourceGroupsImpl(serviceClient))
                .withExistingResourceGroup(resourceGroupName);
    }

    @Override
    public void delete(String id) throws Exception {

    }

    @Override
    public void deleteAsync(String id, ServiceCallback<Void> callback) throws Exception {

    }
}

