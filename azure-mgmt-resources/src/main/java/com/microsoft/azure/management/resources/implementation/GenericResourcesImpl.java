package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupsInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourcesInner;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.implementation.api.GenericResourceInner;

import java.io.IOException;
import java.util.List;

public final class GenericResourcesImpl
    implements GenericResources {

    private final ResourceManagementClientImpl serviceClient;
    private final ResourcesInner resources;
    private final ResourceGroupsInner resourceGroups;

    public GenericResourcesImpl(ResourceManagementClientImpl serviceClient) {
        this.serviceClient = serviceClient;
        this.resources = serviceClient.resources();
        this.resourceGroups = serviceClient.resourceGroups();
    }

    @Override
    public PagedList<GenericResource> list() throws CloudException, IOException {
        return listIntern(null);
    }

    @Override
    public PagedList<GenericResource> list(String groupName) throws CloudException, IOException {
        return listIntern(groupName);
    }

    @Override
    public GenericResource get(String name) throws IOException, CloudException {
        return getIntern(null, name);
    }

    @Override
    public GenericResource get(String groupName, String name) throws IOException, CloudException {
        return getIntern(groupName, name);
    }

    @Override
    public void delete(String id) throws Exception {
        // TODO
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        // TODO
    }

    @Override
    public GenericResource.DefinitionBlank define(String name) {
        return null;
    }

    @Override
    public boolean checkExistence(String resourceGroupName, String resourceProviderNamespace, String parentResourcePath, String resourceType, String resourceName, String apiVersion) throws IOException, CloudException {
        return resources.checkExistence(resourceGroupName, resourceProviderNamespace, parentResourcePath, resourceType, resourceName, apiVersion).getBody();
    }

    private PagedList<GenericResource> listIntern(String groupName) throws IOException, CloudException {
        PagedListConverter<GenericResourceInner, GenericResource> converter = new PagedListConverter<GenericResourceInner, GenericResource>() {
            @Override
            public GenericResource typeConvert(GenericResourceInner genericResourceInner) {
                return new GenericResourceImpl(genericResourceInner.id(), genericResourceInner, serviceClient);
            }
        };
        return converter.convert(resourceGroups.listResources(groupName).getBody());
    }

    private GenericResource getIntern(String groupName, String name) throws IOException, CloudException {
        if (name == null) {
            return null;
        }
        List<GenericResourceInner> innerList = resourceGroups.listResources(groupName).getBody();
        for (GenericResourceInner inner : innerList) {
            if (name.equals(inner.name())) {
                return new GenericResourceImpl(inner.id(), inner, serviceClient);
            }
        }
        return null;
    }
}
