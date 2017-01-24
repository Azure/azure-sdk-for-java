/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.models.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import rx.Observable;

/**
 * Implementation for the child resource which can be CRUDed independently from the parent resource.
 * (internal use only)
 * @param <FluentModelT> The fluent model type
 * @param <FluentParentModelT> the fluent model for parent resource
 * @param <InnerModelT> Azure inner resource class type
 * @param <FluentModelImplT> the implementation type of the fluent model type
 * @param <ManagerT> the client manager type representing the service
 */
@LangDefinition
public abstract class IndependentChildImpl<
            FluentModelT extends IndependentChild<ManagerT>,
            FluentParentModelT extends GroupableResource<ManagerT>,
            InnerModelT,
            FluentModelImplT extends IndependentChildImpl<FluentModelT, FluentParentModelT, InnerModelT, FluentModelImplT, ManagerT>,
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
     * @param name        the name of the resource
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
            this.creatableParentResourceKey = parentResourceCreatable.key();
            this.addCreatableDependency(parentResourceCreatable);
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
    public Observable<FluentModelT> createResourceAsync() {
        if (this.creatableParentResourceKey != null) {
            FluentParentModelT parentResource = (FluentParentModelT) this.createdResource(this.creatableParentResourceKey);
            withExistingParentResource(parentResource);
        }
        return this.createChildResourceAsync();
    }

    protected void setParentName(InnerModelT inner) {
        if (this.id() != null) {
            this.parentName = ResourceId.fromString(this.id()).parent().name();
        }
    }

    protected Indexable createdResource(String key) {
        return super.createdModel(key);
    }

    protected abstract Observable<FluentModelT> createChildResourceAsync();
}
