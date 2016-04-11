package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupsInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourcesInner;
import com.microsoft.azure.management.resources.models.GenericResource;
import com.microsoft.azure.management.resources.models.implementation.GenericResourceImpl;
import com.microsoft.azure.management.resources.models.implementation.api.GenericResourceInner;
import com.microsoft.rest.ServiceCallback;

import java.io.IOException;
import java.util.List;

final class GenericResourcesImpl
    implements GenericResources {

    ResourceManagementClientImpl serviceClient;
    ResourcesInner resources;
    ResourceGroupsInner resourceGroups;

    public GenericResourcesImpl(ResourceManagementClientImpl serviceClient) throws CloudException, IOException {
        this.serviceClient = serviceClient;
        this.resources = serviceClient.resources();
        this.resourceGroups = serviceClient.resourceGroups();
    }

    public GenericResourcesImpl(ResourceManagementClientImpl serviceClient, String resourceGroupName) throws CloudException, IOException {
        this.serviceClient = serviceClient;
        this.resources = serviceClient.resources();
        this.resourceGroups = serviceClient.resourceGroups();
    }

    @Override
    public List<GenericResource> list() throws CloudException, IOException {
        PagedListConverter<GenericResourceInner, GenericResource> converter = new PagedListConverter<GenericResourceInner, GenericResource>() {
            @Override
            public GenericResource typeConvert(GenericResourceInner genericResourceInner) {
                return new GenericResourceImpl(genericResourceInner.id(), genericResourceInner, serviceClient);
            }
        };
        return converter.convert(resources.list().getBody());
    }

    @Override
    public boolean checkExistence(String resourceGroupName, String resourceProviderNamespace, String parentResourcePath, String resourceType, String resourceName, String apiVersion) throws IOException, CloudException {
        return resources.checkExistence(resourceGroupName, resourceProviderNamespace, parentResourcePath, resourceType, resourceName, apiVersion).getBody();
    }

    @Override
    public GenericResource define(String name) throws Exception {
        return null;
    }

    @Override
    public void delete(String id) throws Exception {

    }

    @Override
    public void deleteAsync(String id, ServiceCallback<Void> callback) throws Exception {

    }

    @Override
    public GenericResource get(String name) throws Exception {
        return null;
    }
}
