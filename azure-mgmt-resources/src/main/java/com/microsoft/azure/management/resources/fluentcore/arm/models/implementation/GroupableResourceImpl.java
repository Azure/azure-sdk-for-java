/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.models.implementation;

import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.implementation.ResourceManager;

/**
 * The implementation for {@link GroupableResource}.
 * (Internal use only)
 *
 * @param <FluentModelT> The fluent model type
 * @param <InnerModelT> Azure inner resource class type
 * @param <FluentModelImplT> the implementation type of the fluent model type
 */
public abstract class GroupableResourceImpl<
        FluentModelT,
        InnerModelT extends com.microsoft.azure.Resource,
        FluentModelImplT extends GroupableResourceImpl<FluentModelT, InnerModelT, FluentModelImplT>>
        extends
            ResourceImpl<FluentModelT, InnerModelT, FluentModelImplT>
        implements
            GroupableResource {

    private final ResourceManager resourceManager;
    protected ResourceGroup.DefinitionCreatable newGroup;
    private String groupName;

    protected GroupableResourceImpl(String key, InnerModelT innerObject, ResourceManager resourceManager) {
        super(key, innerObject);
        this.resourceManager = resourceManager;
    }

    /*******************************************
     * Getters.
     *******************************************/

    @Override
    public String resourceGroupName() {
        if (this.groupName == null) {
            return ResourceUtils.groupFromResourceId(this.id());
        } else {
            return this.groupName;
        }
    }

    /****************************************
     * withGroup implementations.
     ****************************************/

    /**
     * Creates a new resource group to put the resource in.
     * <p>
     * The group will be created in the same location as the resource.
     * @param groupName the name of the new group
     * @return the next stage of the resource definition
     */
    public final FluentModelImplT withNewGroup(String groupName) {
        return this.withNewGroup(
                this.resourceManager.resourceGroups().define(groupName).withRegion(this.region()));
    }

    /**
     * Creates a new resource group to put the resource in.
     * <p>
     * The group will be created in the same location as the resource.
     * The group's name is automatically derived from the resource's name.
     * @return the next stage of the resource definition
     */
    public final FluentModelImplT withNewGroup() {
        return this.withNewGroup(this.name() + "group");
    }

    /**
     * Creates a new resource group to put the resource in, based on the definition specified.
     * @param creatable a creatable definition for a new resource group
     * @return the next stage of the resource definition
     */
    @SuppressWarnings("unchecked")
    public final FluentModelImplT withNewGroup(ResourceGroup.DefinitionCreatable creatable) {
        this.groupName = creatable.key();
        this.newGroup = creatable;
        addCreatableDependency(creatable);
        return (FluentModelImplT) this;
    }

    /**
     * Associates the resources with an existing resource group.
     * @param groupName the name of an existing resource group to put this resource in.
     * @return the next stage of the resource definition
     */
    @SuppressWarnings("unchecked")
    public final FluentModelImplT withExistingGroup(String groupName) {
        this.groupName = groupName;
        return (FluentModelImplT) this;
    }

    /**
     * Associates the resources with an existing resource group.
     * @param group an existing resource group to put the resource in
     * @return the next stage of the resource definition
     */
    public final FluentModelImplT withExistingGroup(ResourceGroup group) {
        return this.withExistingGroup(group.name());
    }
}