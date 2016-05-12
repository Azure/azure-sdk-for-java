package com.microsoft.azure.management.resources.fluentcore.arm.models.implementation;

import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Provisionable;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;

import java.util.ArrayList;
import java.util.List;

public abstract class GroupableResourceImpl<
        FluentModelT,
        InnerModelT extends com.microsoft.azure.Resource,
        FluentModelImplT extends GroupableResourceImpl<FluentModelT, InnerModelT, FluentModelImplT>>
        extends
        ResourceImpl<FluentModelT, InnerModelT, FluentModelImplT>
        implements
        GroupableResource {

    ResourceGroups resourceGroups;
    ResourceGroup.DefinitionProvisionable newGroup;
    protected String groupName;

    protected GroupableResourceImpl(String id, InnerModelT innerObject, ResourceGroups resourceGroups) {
        super(id, innerObject);
        this.resourceGroups = resourceGroups;
    }

    protected GroupableResourceImpl(String id, InnerModelT innerObject, ResourceGroup resourceGroup) {
        super(id, innerObject);
        this.withRegion(resourceGroup.location());
        this.withExistingGroup(resourceGroup);
    }

    /*******************************************
     * Getters
     *******************************************/

    @Override
    final public String resourceGroupName() {
        return this.groupName;
    }

    public List<Provisionable<?>> prerequisites() {
        List<Provisionable<?>> provisionables = new ArrayList<>();
        if (this.newGroup != null) {
            provisionables.add(newGroup);
        }
        return provisionables;
    }

    /****************************************
     * withGroup implementations
     ****************************************/

    @SuppressWarnings("unchecked")
    public final FluentModelImplT withExistingGroup(String groupName) {
        this.groupName = groupName;
        return (FluentModelImplT) this;
    }

    public final FluentModelImplT withNewGroup(String groupName) {
        this.groupName = groupName;
        return this.withNewGroup(groupName, resourceGroups.define(groupName).withLocation(location()));
    }

    public final FluentModelImplT withNewGroup() {
        this.groupName = this.name() + "Group";
        return this.withNewGroup(groupName);
    }

    @SuppressWarnings("unchecked")
    public final FluentModelImplT withNewGroup(String groupName, ResourceGroup.DefinitionProvisionable groupDefinition) {
        this.groupName = groupName;
        this.newGroup = groupDefinition;
        return (FluentModelImplT) this;
    }

    public final FluentModelImplT withExistingGroup(ResourceGroup group) {
        return this.withExistingGroup(group.name());
    }

    public final FluentModelImplT withExistingGroup(ResourceGroupInner group) {
        return this.withExistingGroup(group.name());
    }
}