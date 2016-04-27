package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupsInner;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;

import java.io.IOException;
import java.util.List;

public class ResourceGroupsImpl
        implements ResourceGroups {
    private ResourceGroupsInner client;
    private ResourceManagementClientImpl serviceClient;

    public ResourceGroupsImpl(ResourceManagementClientImpl serviceClient) {
        this.serviceClient = serviceClient;
        this.client = serviceClient.resourceGroups();
    }

    @Override
    public PagedList<ResourceGroup> list() throws CloudException, IOException {
        PagedListConverter<ResourceGroupInner, ResourceGroup> converter = new PagedListConverter<ResourceGroupInner, ResourceGroup>() {
            @Override
            public ResourceGroup typeConvert(ResourceGroupInner resourceGroupInner) {
                return new ResourceGroupImpl(resourceGroupInner, serviceClient);
            }
        };
        return converter.convert(client.list().getBody());
    }

    @Override
    // Gets a specific resource group
    public ResourceGroupImpl get(String name) throws CloudException, IOException {
        ResourceGroupInner group = client.get(name).getBody();
        return new ResourceGroupImpl(group, serviceClient);
    }

    @Override
    public void delete(String name) throws Exception {
        client.delete(name);
    }

    @Override
    public ResourceGroupImpl update(String name) {
        return createFluentWrapper(name);
    }

    @Override
    public ResourceGroupImpl define(String name) {
        return createFluentWrapper(name);
    }

    @Override
    public boolean checkExistence(String name) throws CloudException, IOException {
        return client.checkExistence(name).getBody();
    }

    /***************************************************
     * Helpers
     ***************************************************/

    // Wraps native Azure resource group
    private ResourceGroupImpl createFluentWrapper(String name) {
        ResourceGroupInner azureGroup = new ResourceGroupInner();
        azureGroup.setName(name);
        return new ResourceGroupImpl(azureGroup, serviceClient);
    }
}
