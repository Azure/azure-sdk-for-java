// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.models.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.IndependentChild;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import reactor.core.publisher.Mono;

/**
 * Implementation for the child resource which can be CRUDed independently from the parent resource.
 * (internal use only)
 *
 * @param <FluentModelT> The fluent model type
 * @param <FluentParentModelT> the fluent model for parent resource
 * @param <InnerModelT> Azure inner resource class type
 * @param <FluentModelImplT> the implementation type of the fluent model type
 * @param <ManagerT> the client manager type representing the service
 */
public abstract class IndependentChildImpl<
        FluentModelT extends IndependentChild<ManagerT>,
        FluentParentModelT extends Resource & HasResourceGroup,
        InnerModelT,
        FluentModelImplT extends IndependentChildImpl<FluentModelT, FluentParentModelT,
                                                      InnerModelT, FluentModelImplT, ManagerT>,
        ManagerT>
        extends
        CreatableUpdatableImpl<FluentModelT, InnerModelT, FluentModelImplT>
        implements
        IndependentChild<ManagerT>,
        IndependentChild.DefinitionStages.WithParentResource<FluentModelT, FluentParentModelT> {
    private String groupName;
    protected String parentName;
    private String creatableParentResourceKey;
    private final ManagerT manager;

    /**
     * Creates a new instance of IndependentChildResourceImpl.
     *
     * @param name the name of the resource
     * @param innerObject the inner object
     */
    protected IndependentChildImpl(String name, InnerModelT innerObject, ManagerT manager) {
        super(name, innerObject);
        this.manager = manager;
    }

    /*******************************************
     * Getters.
     *******************************************/

    @Override
    public ManagerT manager() {
        return this.manager;
    }

    @Override
    public String resourceGroupName() {
        if (this.groupName == null) {
            return ResourceUtils.groupFromResourceId(this.id());
        } else {
            return this.groupName;
        }
    }

    protected String parentName() {
        return this.parentName;
    }

    /**
     * @return <tt>true</tt> if currently in define..create mode
     */
    @Override
    public boolean isInCreateMode() {
        return this.id() == null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public FluentModelImplT withExistingParentResource(String groupName, String parentName) {
        this.groupName = groupName;
        this.parentName = parentName;

        return (FluentModelImplT) this;
    }

    @Override
    public FluentModelImplT withExistingParentResource(FluentParentModelT existingParentResource) {
        return withExistingParentResource(existingParentResource.resourceGroupName(), existingParentResource.name());
    }

    @SuppressWarnings("unchecked")
    @Override
    public FluentModelImplT withNewParentResource(Creatable<FluentParentModelT> parentResourceCreatable) {
        if (this.creatableParentResourceKey == null) {
            this.creatableParentResourceKey = this.addDependency(parentResourceCreatable);
        }
        return (FluentModelImplT) this;
    }

    @Override
    public void setInner(InnerModelT inner) {
        super.setInner(inner);
        this.setParentName(inner);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<FluentModelT> createResourceAsync() {
        if (this.creatableParentResourceKey != null) {
            FluentParentModelT parentResource = this.<FluentParentModelT>taskResult(this.creatableParentResourceKey);
            withExistingParentResource(parentResource);
        }
        return this.createChildResourceAsync();
    }

    protected void setParentName(InnerModelT inner) {
        if (this.id() != null) {
            this.parentName = ResourceId.fromString(this.id()).parent().name();
        }
    }

    protected abstract Mono<FluentModelT> createChildResourceAsync();
}
