package com.microsoft.azure.management.resources.fluentcore.arm.models.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.Azure;
import com.microsoft.azure.management.resources.fluentcore.collection.implementation.EntitiesImpl;
import com.microsoft.azure.management.resources.models.fluent.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;

public abstract class GroupableResourceImpl<
        WRAPPER,
        INNER extends com.microsoft.azure.Resource,
        WRAPPERIMPL extends GroupableResourceImpl<WRAPPER, INNER, WRAPPERIMPL>>
        extends
        ResourceImpl<WRAPPER, INNER, WRAPPERIMPL>
        implements
        GroupableResource {

    protected GroupableResourceImpl(String id, INNER innerObject, EntitiesImpl<Azure> collection) {
        super(id, innerObject, collection);
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

            group = this.collection.azure().resourceGroups().define(this.groupName)
                    .withRegion(this.region())
                    .provision();
            this.isExistingGroup = true;
            return group;
        } else {
            return this.collection.azure().resourceGroups(this.groupName);
        }
    }


    /****************************************
     * withGroup implementations
     ****************************************/

    @SuppressWarnings("unchecked")
    public final WRAPPERIMPL withExistingGroup(String groupName) {
        this.groupName = groupName;
        this.isExistingGroup = true;
        return (WRAPPERIMPL)this;
    }


    @SuppressWarnings("unchecked")
    public final WRAPPERIMPL withNewGroup(String groupName) {
        this.groupName = groupName;
        this.isExistingGroup = false;
        return (WRAPPERIMPL) this;
    }

    public final WRAPPERIMPL withNewGroup() {
        return this.withNewGroup((String)null);
    }

    public final WRAPPERIMPL withNewGroup(ResourceGroup.DefinitionProvisionable groupDefinition) throws Exception {
        return withExistingGroup(groupDefinition.provision());
    }

    public final WRAPPERIMPL withExistingGroup(ResourceGroup group) {
        return this.withExistingGroup(group.name());
    }

    public final WRAPPERIMPL withExistingGroup(com.microsoft.azure.management.resources.models.dto.toplevel.ResourceGroup group) {
        return this.withExistingGroup(group.getName());
    }
}