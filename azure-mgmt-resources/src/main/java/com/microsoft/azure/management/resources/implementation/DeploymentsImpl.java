package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.Deployments;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupPagedList;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.DeploymentsInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.models.Deployment;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.resources.models.implementation.DeploymentImpl;
import com.microsoft.azure.management.resources.models.implementation.api.DeploymentExtendedInner;
import com.microsoft.rest.RestException;
import com.microsoft.rest.ServiceCallback;

import java.io.IOException;
import java.util.List;

public class DeploymentsImpl
    implements Deployments {

    protected ResourceManagementClientImpl serviceClient;
    protected DeploymentsInner deployments;
    protected ResourceGroups resourceGroups;
    protected PagedListConverter<DeploymentExtendedInner, Deployment> converter;

    public DeploymentsImpl(ResourceManagementClientImpl serviceClient) {
        this.serviceClient = serviceClient;
        this.deployments = serviceClient.deployments();
        this.resourceGroups = new ResourceGroupsImpl(serviceClient);
        converter = new PagedListConverter<DeploymentExtendedInner, Deployment>() {
            @Override
            public Deployment typeConvert(DeploymentExtendedInner deploymentInner) {
                return new DeploymentImpl(deploymentInner, deployments, resourceGroups);
            }
        };
    }

    @Override
    public InGroup resourceGroup(String resourceGroupName) {
        return new DeploymentsInGroupImpl(serviceClient, resourceGroupName);
    }

    @Override
    public List<Deployment> list() throws CloudException, IOException {
        return new GroupPagedList<Deployment>(resourceGroups.list()) {
            @Override
            public List<Deployment> listNextGroup(String resourceGroupName) throws RestException, IOException {
                return converter.convert(deployments.list(resourceGroupName).getBody());
            }
        };
    }

    @Override
    public boolean checkExistence(String deploymentName) throws IOException, CloudException {
        for (ResourceGroup group : resourceGroups.list()) {
            if (deployments.checkExistence(group.name(), deploymentName).getBody()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Deployment.DefinitionBlank define(String name) throws Exception {
        DeploymentExtendedInner deployment = new DeploymentExtendedInner();
        deployment.setName(name);
        return new DeploymentImpl(deployment, deployments, new ResourceGroupsImpl(serviceClient));
    }

    @Override
    public Deployment get(String name) throws IOException, CloudException {
        for (ResourceGroup group : resourceGroups.list()) {
            try {
                DeploymentExtendedInner inner = deployments.get(group.name(), name).getBody();
                if (inner != null) {
                    return new DeploymentImpl(inner, deployments, resourceGroups);
                }
            } catch (CloudException ex) {
            }
        }
        return null;
    }

    @Override
    public void delete(String id) throws Exception {

    }
}
