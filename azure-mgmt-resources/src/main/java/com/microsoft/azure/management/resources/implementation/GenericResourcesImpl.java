package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupsInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourcesInner;
import com.microsoft.azure.management.resources.models.GenericResource;
import com.microsoft.azure.management.resources.models.implementation.GenericResourceImpl;
import com.microsoft.azure.management.resources.models.implementation.api.GenericResourceInner;
import com.microsoft.azure.management.resources.models.implementation.api.PageImpl;
import com.microsoft.rest.RestException;
import com.microsoft.rest.ServiceCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class GenericResourcesImpl extends PagedList<GenericResource>
    implements GenericResources {

    ResourceManagementClientImpl serviceClient;
    ResourcesInner resources;
    ResourceGroupsInner resourceGroups;
    private PagedList<GenericResourceInner> innerList;
    private Map<String, GenericResource> indexable;

    public GenericResourcesImpl(ResourceManagementClientImpl serviceClient) throws CloudException, IOException {
        this.serviceClient = serviceClient;
        this.resources = serviceClient.resources();
        this.resourceGroups = serviceClient.resourceGroups();
        this.innerList = resources.list().getBody();
        this.loadNextPage();
    }

    public GenericResourcesImpl(ResourceManagementClientImpl serviceClient, String resourceGroupName) throws CloudException, IOException {
        this.serviceClient = serviceClient;
        this.resources = serviceClient.resources();
        this.resourceGroups = serviceClient.resourceGroups();
        this.innerList = resourceGroups.listResources(resourceGroupName).getBody();
        this.loadNextPage();
    }

    @Override
    public Page<GenericResource> nextPage(String nextPageLink) throws RestException, IOException {
        PageImpl<GenericResource> page = new PageImpl<>();
        List<GenericResource> items = new ArrayList<>();
        if (currentPage() == null) {
            for (GenericResourceInner inner : innerList) {
                items.add((new GenericResourceImpl(inner, client)));
            }
            page.setNextPageLink(innerList.nextpageLink());
        } else {
            Page<GenericResourceInner> innerPage = innerList.nextPage(nextPageLink);
            page.setNextPageLink(innerPage.getNextPageLink());
            for (GenericResourceInner inner : innerList) {
                items.add((new GenericResourceImpl(inner, client)));
            }
        }
        page.setItems(items);
        return page;
    }

    @Override
    public boolean checkExistence(String resourceGroupName, String resourceProviderNamespace, String parentResourcePath, String resourceType, String resourceName, String apiVersion) {
        return false;
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

    @Override
    public Map asMap() throws Exception {
        return null;
    }
}
