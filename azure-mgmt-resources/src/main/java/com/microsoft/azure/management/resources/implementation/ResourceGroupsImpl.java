package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupsInner;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.resources.models.implementation.ResourceGroupImpl;
import com.microsoft.azure.management.resources.models.implementation.api.ResourceGroupInner;
import com.microsoft.rest.ServiceCallback;

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
    public List<ResourceGroup> list() throws CloudException, IOException {
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
    public void deleteAsync(String name, ServiceCallback<Void> callback) {
        client.deleteAsync(name, callback);
    }

    @Override
    public ResourceGroupImpl update(String name) {
        return createWrapper(name);
    }

    @Override
    public ResourceGroupImpl define(String name) {
        return createWrapper(name);
    }

    @Override
    public boolean checkExistence(String name) throws CloudException, IOException {
        return client.checkExistence(name).getBody();
    }

    /***************************************************
     * Helpers
     ***************************************************/

    // Wraps native Azure resource group
    private ResourceGroupImpl createWrapper(String name) {
        ResourceGroupInner azureGroup = new ResourceGroupInner();
        azureGroup.setName(name);
        return new ResourceGroupImpl(azureGroup, serviceClient);
    }
}
