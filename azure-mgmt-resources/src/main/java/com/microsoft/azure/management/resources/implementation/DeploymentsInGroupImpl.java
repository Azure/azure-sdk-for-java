package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Deployments;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.Deployment;

import java.io.IOException;

public class DeploymentsInGroupImpl
        implements Deployments.InGroup {
    private final Deployments deployments;
    private final ResourceGroup resourceGroup;

    public DeploymentsInGroupImpl(Deployments deployments, ResourceGroup resourceGroup) {
        this.deployments = deployments;
        this.resourceGroup = resourceGroup;
    }

    @Override
    public PagedList<Deployment> list() throws CloudException, IOException {
        return this.deployments.list(this.resourceGroup.name());
    }

    @Override
    public boolean checkExistence(String deploymentName) throws IOException, CloudException {
        return this.deployments.checkExistence(this.resourceGroup.name(), deploymentName);
    }

    @Override
    public Deployment get(String name) throws IOException, CloudException {
        return this.deployments.get(this.resourceGroup.name(), name);
    }

    @Override
    public Deployment.DefinitionWithGroup define(String name) {
        return this.deployments.define(name).withExistingResourceGroup(resourceGroup.name());
    }

    @Override
    public void delete(String name) throws Exception {
        this.deployments.delete(this.resourceGroup.name(), name);
    }
}

