package com.microsoft.azure.management.resources.fluentcore.arm.models.implementation;

import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.resources.models.implementation.api.ResourceGroupInner;

public abstract class GroupableResourceImpl<
        FluentModelT,
        InnerModelT extends com.microsoft.azure.Resource,
        FluentModelImplT extends GroupableResourceImpl<FluentModelT, InnerModelT, FluentModelImplT>>
        extends
        ResourceImpl<FluentModelT, InnerModelT, FluentModelImplT>
        implements
        GroupableResource {

    ResourceGroups resourceGroups;

    protected GroupableResourceImpl(String id, InnerModelT innerObject, ResourceGroups resourceGroups) {
        super(id, innerObject);
        this.resourceGroups = resourceGroups;
    }

    protected String groupName;
    protected boolean isExistingGroup;

    /*******************************************
     * Getters
     *******************************************/

    @Override
    final public String group() {
        String groupNameTemp = groupFromResourceId(this.id());
        return (groupNameTemp == null) ? this.groupName : groupNameTemp;
    }

    private static String groupFromResourceId(String id) {
        // TODO
        return null;
    }

    /**************************************************
     * Helpers
     * @throws Exception
     **************************************************/
    final protected ResourceGroup ensureGroup() throws Exception {
        ResourceGroup group;
        if(!this.isExistingGroup) {
            if(this.groupName == null) {
                this.groupName = this.name() + "group";
            }

            group = this.resourceGroups.define(this.groupName)
                    .withLocation(this.region())
                    .provision();
            this.isExistingGroup = true;
            return group;
        } else {
            return resourceGroups.get(groupName);
        }
    }


    /****************************************
     * withGroup implementations
     ****************************************/

    @SuppressWarnings("unchecked")
    public final FluentModelImplT withExistingGroup(String groupName) {
        this.groupName = groupName;
        this.isExistingGroup = true;
        return (FluentModelImplT)this;
    }


    @SuppressWarnings("unchecked")
    public final FluentModelImplT withNewGroup(String groupName) {
        this.groupName = groupName;
        this.isExistingGroup = false;
        return (FluentModelImplT) this;
    }

    public final FluentModelImplT withNewGroup() {
        return this.withNewGroup((String)null);
    }

    public final FluentModelImplT withNewGroup(ResourceGroup.DefinitionProvisionable groupDefinition) throws Exception {
        return withExistingGroup(groupDefinition.provision());
    }

    public final FluentModelImplT withExistingGroup(ResourceGroup group) {
        return this.withExistingGroup(group.name());
    }

    public final FluentModelImplT withExistingGroup(ResourceGroupInner group) {
        return this.withExistingGroup(group.name());
    }
}