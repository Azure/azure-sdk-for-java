package com.microsoft.azure.management.resources.fluentcore.arm.models.implementation;

import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;

public abstract class GroupableResourceImpl<
        FluentModelT,
        InnerModelT extends com.microsoft.azure.Resource,
        FluentModelImplT extends GroupableResourceImpl<FluentModelT, InnerModelT, FluentModelImplT>>
        extends
        	ResourceImpl<FluentModelT, InnerModelT, FluentModelImplT>
        implements
        	GroupableResource {

    private ResourceGroups resourceGroups;
    private ResourceGroup.DefinitionCreatable newGroup;
    private String groupName;

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
    public String resourceGroupName() {
        return this.groupName;
    }

    @Override
    public FluentModelT create() throws Exception {
        for (String id : prerequisites().keySet()) {
            if (!created().containsKey(id)) {
                created().put(id, prerequisites().get(id));
                prerequisites().get(id).create();
            }
        }
        return null;
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
        return this.withNewGroup(resourceGroups.define(groupName).withLocation(location()));
    }

    public final FluentModelImplT withNewGroup() {
        this.groupName = this.name() + "Group";
        return this.withNewGroup(groupName);
    }

    @SuppressWarnings("unchecked")
    public final FluentModelImplT withNewGroup(ResourceGroup.DefinitionCreatable groupDefinition) {
        this.groupName = groupDefinition.id();
        this.newGroup = groupDefinition;
        this.prerequisites().put(groupDefinition.id(), this.newGroup);
        return (FluentModelImplT) this;
    }

    public final FluentModelImplT withExistingGroup(ResourceGroup group) {
        return this.withExistingGroup(group.name());
    }

    public final FluentModelImplT withExistingGroup(ResourceGroupInner group) {
        return this.withExistingGroup(group.name());
    }
}