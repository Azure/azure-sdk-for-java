package com.microsoft.azure.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.utils.WrappedItemTransformer;
import com.microsoft.azure.management.resources.fluentcore.utils.WrappedList;
import com.microsoft.azure.management.resources.implementation.ResourceGroupsImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.ResourceGroup;
import java.io.IOException;
import java.util.List;


public class AzureResourceGroupsImpl implements Azure.ResourceGroups {
    private ResourceManagementClientImpl client;
    private ResourceGroups resourceGroupsCore;

    public AzureResourceGroupsImpl(ResourceManagementClientImpl client) {
        this.client = client;
        this.resourceGroupsCore = new ResourceGroupsImpl(client);
    }

    @Override
    public List<Azure.ResourceGroup> list() throws CloudException, IOException {
        final ResourceManagementClientImpl client = this.client;
        WrappedList<ResourceGroup, Azure.ResourceGroup> wrappedList = new WrappedList<>(this.resourceGroupsCore.list(),
                new WrappedItemTransformer<ResourceGroup, Azure.ResourceGroup>() {
                    @Override
                    public Azure.ResourceGroup toWrapped(ResourceGroup source) {
                        return new AzureResourceGroupImpl(source, client);
                    }

                    @Override
                    public ResourceGroup toSource(Azure.ResourceGroup wrapped) {
                        return ((AzureResourceGroupImpl)wrapped).resourceGroupCore();
                    }
                });

        return wrappedList;
    }

    @Override
    public AzureResourceGroupImpl get(String name) throws CloudException, IOException {
        return new AzureResourceGroupImpl(this.resourceGroupsCore.get(name), this.client);
    }

    @Override
    public void delete(String name) throws Exception {
        this.resourceGroupsCore.delete(name);
    }

    @Override
    public Azure.ResourceGroup.UpdateBlank update(String name) {
        return this.resourceGroupsCore.update(name);
    }

    @Override
    public Azure.ResourceGroup.DefinitionBlank define(String name) throws Exception {
        return this.resourceGroupsCore.define(name);
    }

}
