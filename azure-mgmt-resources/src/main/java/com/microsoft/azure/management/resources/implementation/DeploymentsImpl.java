package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Deployments;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupPagedList;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.DeploymentOperationsInner;
import com.microsoft.azure.management.resources.implementation.api.DeploymentsInner;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.api.DeploymentExtendedInner;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.List;

public class DeploymentsImpl
    implements Deployments {

    private final DeploymentsInner client;
    private final DeploymentOperationsInner deploymentOperationsClient;
    private final ResourceGroups resourceGroups;
    private PagedListConverter<DeploymentExtendedInner, Deployment> converter;

    public DeploymentsImpl(final DeploymentsInner client,
                           final DeploymentOperationsInner deploymentOperationsClient,
                           final ResourceGroups resourceGroups) {
        this.client = client;
        this.deploymentOperationsClient = deploymentOperationsClient;
        this.resourceGroups = resourceGroups;
        converter = new PagedListConverter<DeploymentExtendedInner, Deployment>() {
            @Override
            public Deployment typeConvert(DeploymentExtendedInner deploymentInner) {
                return createFluentModel(deploymentInner);
            }
        };
    }

    @Override
    public InGroup resourceGroup(ResourceGroup resourceGroup) {
        return new DeploymentsInGroupImpl(this, resourceGroup);
    }

    @Override
    public PagedList<Deployment> list() throws CloudException, IOException {
        return new GroupPagedList<Deployment>(resourceGroups.list()) {
            @Override
            public List<Deployment> listNextGroup(String resourceGroupName) throws RestException, IOException {
                return converter.convert(client.list(resourceGroupName).getBody());
            }
        };
    }

    @Override
    public PagedList<Deployment> list(String groupName) throws CloudException, IOException {
        return converter.convert(client.list(groupName).getBody());
    }

    @Override
    public Deployment get(String name) throws IOException, CloudException {
        for (ResourceGroup group : resourceGroups.list()) {
            try {
                DeploymentExtendedInner inner = client.get(group.name(), name).getBody();
                if (inner != null) {
                    return createFluentModel(inner);
                }
            } catch (CloudException ex) {
            }
        }
        return null;
    }

    @Override
    public Deployment get(String groupName, String name) throws IOException, CloudException {
        DeploymentExtendedInner inner = client.get(groupName, name).getBody();
        return createFluentModel(inner);
    }

    @Override
    public void delete(String id) throws Exception {
        this.delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        client.delete(groupName, name);
    }

    @Override
    public Deployment.DefinitionBlank define(String name) {
        return createFluentModel(name);
    }

    @Override
    public boolean checkExistence(String deploymentName) throws IOException, CloudException {
        for (ResourceGroup group : resourceGroups.list()) {
            if (client.checkExistence(group.name(), deploymentName).getBody()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkExistence(String groupName, String deploymentName) throws IOException, CloudException {
        if (client.checkExistence(groupName, deploymentName).getBody()) {
            return true;
        }
        return false;
    }

    /** Fluent model create helpers **/

    private DeploymentImpl createFluentModel(String name) {
        DeploymentExtendedInner deploymentExtendedInner = new DeploymentExtendedInner();
        deploymentExtendedInner.setName(name);
        return new DeploymentImpl(deploymentExtendedInner, client, deploymentOperationsClient, resourceGroups);
    }

    private DeploymentImpl createFluentModel(DeploymentExtendedInner deploymentExtendedInner) {
        return new DeploymentImpl(deploymentExtendedInner, client, deploymentOperationsClient, resourceGroups);
    }
}
