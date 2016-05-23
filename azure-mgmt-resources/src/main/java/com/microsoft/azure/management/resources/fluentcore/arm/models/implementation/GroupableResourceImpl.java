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

    protected GroupableResourceImpl(String key, InnerModelT innerObject, ResourceGroups resourceGroups) {
        super(key, innerObject);
        this.resourceGroups = resourceGroups;
    }

    protected GroupableResourceImpl(String key, InnerModelT innerObject, ResourceGroup resourceGroup) {
        super(key, innerObject);
        this.withRegion(resourceGroup.location());
        this.withExistingGroup(resourceGroup);
    }

    /*******************************************
     * Getters
     *******************************************/

    @Override
    public String resourceGroupName() {
        if(this.groupName == null) {
        	return ResourceUtils.groupFromResourceId(this.id());
        } else {
        	return this.groupName;
        }
    }

    @Override
    public FluentModelT create() throws Exception {
        for (String id : prerequisites().keySet()) {
            if (!created().containsKey(id)) {
                prerequisites().get(id).create();
                created().put(id, prerequisites().get(id));
            }
        }
        return null;
    }

    /****************************************
     * withGroup implementations
     ****************************************/

    public final FluentModelImplT withNewGroup(String groupName) {
        return this.withNewGroup(resourceGroups.define(groupName).withLocation(this.region()));
    }

    public final FluentModelImplT withNewGroup() {
        return this.withNewGroup(this.key() + "group");
    }

    @SuppressWarnings("unchecked")
    public final FluentModelImplT withNewGroup(ResourceGroup.DefinitionCreatable groupDefinition) {
        this.groupName = groupDefinition.key();
        this.newGroup = groupDefinition;
        this.prerequisites().put(groupDefinition.key(), this.newGroup);
        return (FluentModelImplT) this;
    }

    @SuppressWarnings("unchecked")
    public final FluentModelImplT withExistingGroup(String groupName) {
        this.groupName = groupName;
        return (FluentModelImplT) this;
    }

    public final FluentModelImplT withExistingGroup(ResourceGroup group) {
        return this.withExistingGroup(group.name());
    }

    public final FluentModelImplT withExistingGroup(ResourceGroupInner group) {
        return this.withExistingGroup(group.name());
    }
}