// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.models.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.ManagerBase;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;

/**
 * The implementation for {@link GroupableResource}.
 * (Internal use only)
 *
 * @param <FluentModelT> The fluent model type
 * @param <InnerModelT> Azure inner resource class type
 * @param <FluentModelImplT> the implementation type of the fluent model type
 * @param <ManagerT> the service manager type
 */
public abstract class GroupableResourceImpl<
        FluentModelT extends Resource,
        InnerModelT extends com.azure.core.management.Resource,
        FluentModelImplT extends GroupableResourceImpl<FluentModelT, InnerModelT, FluentModelImplT, ManagerT>,
        ManagerT extends ManagerBase>
        extends
        ResourceImpl<FluentModelT, InnerModelT, FluentModelImplT>
        implements
        GroupableResource<ManagerT, InnerModelT> {

    protected final ManagerT myManager;
    protected Creatable<ResourceGroup> creatableGroup;
    private String groupName;

    protected GroupableResourceImpl(
            String name,
            InnerModelT innerObject,
            ManagerT manager) {
        super(name, innerObject);
        this.myManager = manager;
    }

    // Helpers

    protected String resourceIdBase() {
        return new StringBuilder()
                .append("/subscriptions/").append(this.myManager.subscriptionId())
                .append("/resourceGroups/").append(this.resourceGroupName())
                .toString();
    }

    /*******************************************
     * Getters.
     *******************************************/

    @Override
    public ManagerT manager() {
        return this.myManager;
    }

    @Override
    public String resourceGroupName() {
        if (this.groupName == null) {
            return ResourceUtils.groupFromResourceId(this.id());
        } else {
            return this.groupName;
        }
    }

    protected Creatable<ResourceGroup> creatableGroup() {
        return this.creatableGroup;
    }

    /****************************************
     * withGroup implementations.
     ****************************************/

    /**
     * Creates a new resource group to put the resource in.
     * <p>
     * The group will be created in the same location as the resource.
     *
     * @param groupName the name of the new group
     * @return the next stage of the definition
     */
    public final FluentModelImplT withNewResourceGroup(String groupName) {
        return this.withNewResourceGroup(
                this.myManager.resourceManager().resourceGroups().define(groupName).withRegion(this.regionName()));
    }

    /**
     * Creates a new resource group to put the resource in.
     * <p>
     * The group will be created in the same location as the resource.
     *
     * @param groupName the name of the new group
     * @param region the region where resource group needs to be created
     * @return the next stage of the definition
     */
    public final FluentModelImplT withNewResourceGroup(String groupName, Region region) {
        return this.withNewResourceGroup(
                this.myManager.resourceManager().resourceGroups().define(groupName).withRegion(region));
    }

    /**
     * Creates a new resource group to put the resource in.
     * <p>
     * The group will be created in the same location as the resource.
     * The group's name is automatically derived from the resource's name.
     *
     * @return the next stage of the definition
     */
    public final FluentModelImplT withNewResourceGroup() {
        return this.withNewResourceGroup(this.name() + "group");
    }

    /**
     * Creates a new resource group to put the resource in.
     * <p>
     * The group will be created in the same location as the resource.
     * The group's name is automatically derived from the resource's name.
     *
     * @param region the region where resource group needs to be created
     * @return the next stage of the definition
     */
    public final FluentModelImplT withNewResourceGroup(Region region) {
        return this.withNewResourceGroup(this.name() + "group", region);
    }

    /**
     * Creates a new resource group to put the resource in, based on the definition specified.
     *
     * @param creatable a creatable definition for a new resource group
     * @return the next stage of the definition
     */
    @SuppressWarnings("unchecked")
    public final FluentModelImplT withNewResourceGroup(Creatable<ResourceGroup> creatable) {
        this.groupName = creatable.name();
        this.creatableGroup = creatable;
        this.addDependency(creatable);
        return (FluentModelImplT) this;
    }

    /**
     * Associates the resources with an existing resource group.
     *
     * @param groupName the name of an existing resource group to put this resource in.
     * @return the next stage of the definition
     */
    @SuppressWarnings("unchecked")
    public final FluentModelImplT withExistingResourceGroup(String groupName) {
        this.groupName = groupName;
        return (FluentModelImplT) this;
    }

    /**
     * Associates the resources with an existing resource group.
     *
     * @param group an existing resource group to put the resource in
     * @return the next stage of the definition
     */
    public final FluentModelImplT withExistingResourceGroup(ResourceGroup group) {
        return this.withExistingResourceGroup(group.name());
    }
}
