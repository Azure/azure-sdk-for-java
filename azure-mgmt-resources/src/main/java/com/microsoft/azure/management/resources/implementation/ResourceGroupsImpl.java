package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.Azure;
import com.microsoft.azure.management.resources.fluentcore.collection.implementation.EntitiesImpl;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.resources.models.implementation.ResourceGroupImpl;
import com.microsoft.azure.management.resources.models.implementation.api.ResourceGroupInner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ResourceGroupsImpl extends EntitiesImpl<Azure>
        implements ResourceGroups {

    ResourceGroupsImpl(Azure azure) {
        super(azure);
    }

    @Override
    public Map<String, ResourceGroup> list() throws Exception {
        HashMap<String, ResourceGroup> wrappers = new HashMap<>();
        for(ResourceGroupInner nativeItem : getNativeEntities()) {
            ResourceGroupImpl wrapper = new ResourceGroupImpl(nativeItem, this);
            wrappers.put(nativeItem.name(), wrapper);
        }

        return Collections.unmodifiableMap(wrappers);
    }

    @Override
    // Gets a specific resource group
    public ResourceGroupImpl get(String name) throws Exception {
        ResourceGroupInner azureGroup = azure.resourceManagementClient().resourceGroups().get(name).getBody();
        return new ResourceGroupImpl(azureGroup, this);
    }

    @Override
    public void delete(String name) throws Exception {
        azure.resourceManagementClient().resourceGroups().delete(name);
        //TODO: Apparently the effect of the deletion is not immediate - Azure SDK misleadingly returns from this synch call even though listing resource groups will still include this
    }

    @Override
    public ResourceGroupImpl update(String name) {
        return createWrapper(name);
    }

    @Override
    public ResourceGroupImpl define(String name) {
        return createWrapper(name);
    }

    /***************************************************
     * Helpers
     ***************************************************/

    // Wraps native Azure resource group
    private ResourceGroupImpl createWrapper(String name) {
        ResourceGroupInner azureGroup = new ResourceGroupInner();
        azureGroup.setName(name);
        return new ResourceGroupImpl(azureGroup, this);
    }

    // Helper to get the resource groups from Azure
    private ArrayList<ResourceGroupInner> getNativeEntities() throws Exception {
        // return this.azure.resourceManagementClient().resourceGroups().list().getBody();
        // TODO
        return null;
    }
}
