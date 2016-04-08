package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupsInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.resources.models.implementation.ResourceGroupImpl;
import com.microsoft.azure.management.resources.models.implementation.api.PageImpl;
import com.microsoft.azure.management.resources.models.implementation.api.ResourceGroupInner;
import com.microsoft.rest.RestException;
import com.microsoft.rest.ServiceCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceGroupsImpl
        extends PagedList<ResourceGroup>
        implements ResourceGroups {
    private ResourceGroupsInner client;
    private ResourceManagementClientImpl serviceClient;
    private PagedList<ResourceGroupInner> innerList;
    private Map<String, ResourceGroup> indexable;

    public ResourceGroupsImpl(ResourceManagementClientImpl serviceClient) throws IOException, CloudException {
        this.serviceClient = serviceClient;
        this.client = serviceClient.resourceGroups();
        this.innerList = client.list().getBody();
    }

    @Override
    public Map<String, ResourceGroup> asMap() throws Exception {
        if (indexable == null) {
            indexable = new HashMap<>();
            for(ResourceGroup item : this) {
                indexable.put(item.name(), item);
            }
        }
        return Collections.unmodifiableMap(indexable);
    }

    @Override
    // Gets a specific resource group
    public ResourceGroupImpl get(String name) throws Exception {
        ResourceGroupInner group = client.get(name).getBody();
        return new ResourceGroupImpl(group, client);
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
        return new ResourceGroupImpl(azureGroup, client);
    }

    @Override
    public Page<ResourceGroup> nextPage(String nextPageLink) throws RestException, IOException {
        PageImpl<ResourceGroup> page = new PageImpl<>();
        List<ResourceGroup> items = new ArrayList<>();
        if (currentPage() == null) {
            for (ResourceGroupInner inner : innerList) {
                items.add((new ResourceGroupImpl(inner, client)));
            }
            page.setNextPageLink(innerList.nextpageLink());
        } else {
            Page<ResourceGroupInner> innerPage = innerList.nextPage(nextPageLink);
            page.setNextPageLink(innerPage.getNextPageLink());
            for (ResourceGroupInner inner : innerList) {
                items.add((new ResourceGroupImpl(inner, client)));
            }
        }
        page.setItems(items);
        return page;
    }
}
