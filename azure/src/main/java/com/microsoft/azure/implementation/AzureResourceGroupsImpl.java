package com.microsoft.azure.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.ResourceGroupsImpl;
import com.microsoft.azure.management.resources.implementation.api.PageImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.rest.RestException;

import java.io.IOException;


final class AzureResourceGroupsImpl implements Azure.ResourceGroups {
    private ResourceManagementClientImpl client;
    private ResourceGroups resourceGroupsCore;

    AzureResourceGroupsImpl(ResourceManagementClientImpl client) {
        this.client = client;
        this.resourceGroupsCore = new ResourceGroupsImpl(client);
    }

    @Override
    public PagedList<Azure.ResourceGroup> list() throws CloudException, IOException {
        PageImpl<ResourceGroup> page = new PageImpl<>();
        page.setNextPageLink(null);
        page.setItems(resourceGroupsCore.list());

        return (new PagedListConverter<ResourceGroup, Azure.ResourceGroup>() {
            @Override
            public Azure.ResourceGroup typeConvert(ResourceGroup resourceGroup) {
                return new AzureResourceGroupImpl(resourceGroup, client);
            }
        }).convert(new PagedList<ResourceGroup>(page) {
            @Override
            public Page<ResourceGroup> nextPage(String nextPageLink) throws RestException, IOException {
                return null;
            }
        });
    }

    @Override
    public AzureResourceGroupImpl get(String name) throws CloudException, IOException {
        return new AzureResourceGroupImpl(resourceGroupsCore.get(name), client);
    }

    @Override
    public void delete(String name) throws Exception {
        resourceGroupsCore.delete(name);
    }

    @Override
    public Azure.ResourceGroup.Update update(String name) {
        return resourceGroupsCore.update(name);
    }

    @Override
    public Azure.ResourceGroup.DefinitionBlank define(String name) {
        return resourceGroupsCore.define(name);
    }

}
