package com.microsoft.azure.management.resources.collection.fluent.implementation;

import com.microsoft.azure.management.resources.collection.fluent.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.Azure;
import com.microsoft.azure.management.resources.fluentcore.collection.implementation.EntitiesImpl;
import com.microsoft.azure.management.resources.models.fluent.ResourceGroup;
import com.microsoft.azure.management.resources.models.fluent.implementation.ResourceGroupImpl;

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
        for(com.microsoft.azure.management.resources.models.dto.toplevel.ResourceGroup nativeItem : getNativeEntities()) {
            ResourceGroupImpl wrapper = new ResourceGroupImpl(nativeItem, this);
            wrappers.put(nativeItem.getName(), wrapper);
        }

        return Collections.unmodifiableMap(wrappers);
    }

    @Override
    // Gets a specific resource group
    public ResourceGroupImpl get(String name) throws Exception {
        com.microsoft.azure.management.resources.models.dto.toplevel.ResourceGroup azureGroup =
                azure.resourceManagementClient().resourceGroups().get(name).getBody();
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
        com.microsoft.azure.management.resources.models.dto.toplevel.ResourceGroup azureGroup =
                new com.microsoft.azure.management.resources.models.dto.toplevel.ResourceGroup();
        azureGroup.setName(name);
        return new ResourceGroupImpl(azureGroup, this);
    }

    // Helper to get the resource groups from Azure
    private ArrayList<com.microsoft.azure.management.resources.models.dto.toplevel.ResourceGroup> getNativeEntities() throws Exception {
        // return this.azure.resourceManagementClient().resourceGroups().list().getBody();
        // TODO
        return null;
    }
}
