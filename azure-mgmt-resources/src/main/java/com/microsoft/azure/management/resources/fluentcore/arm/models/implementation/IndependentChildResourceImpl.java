/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.models.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import rx.Observable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation for the child resource which can be CRUDed independently from the parent resource.
 * (internal use only)
 * @param <FluentModelT> The fluent model type
 * @param <FluentParentModelT> the fluent model for parent resource
 * @param <InnerModelT> Azure inner resource class type
 * @param <FluentModelImplT> the implementation type of the fluent model type
 */
public abstract class IndependentChildResourceImpl<
            FluentModelT extends IndependentChildResource,
            FluentParentModelT extends GroupableResource,
            InnerModelT extends com.microsoft.azure.Resource,
            FluentModelImplT extends IndependentChildResourceImpl<FluentModelT, FluentParentModelT, InnerModelT, FluentModelImplT>>
        extends
            CreatableUpdatableImpl<FluentModelT, InnerModelT, FluentModelImplT>
        implements
            IndependentChildResource,
            IndependentChildResource.DefinitionStages.WithParentResource<FluentModelT, FluentParentModelT> {
    private String groupName;
    protected String parentName;
    private String creatableParentResourceKey;

    /**
     * Creates a new instance of CreatableUpdatableImpl.
     *
     * @param name        the name of the resource
     * @param innerObject the inner object
     */
    protected IndependentChildResourceImpl(String name, InnerModelT innerObject) {
        super(name, innerObject);
    }

    /*******************************************
     * Getters.
     *******************************************/

    @Override
    public String regionName() {
        return this.inner().location();
    }

    @Override
    public Region region() {
        return Region.fromName(this.regionName());
    }

    @Override
    public Map<String, String> tags() {
        Map<String, String> tags = this.inner().getTags();
        if (tags == null) {
            tags = new TreeMap<>();
        }
        return Collections.unmodifiableMap(tags);
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String type() {
        return this.inner().type();
    }

    @Override
    public String name() {
        if (this.inner().name() == null) {
            return super.name();
        } else {
            return this.inner().name();
        }
    }

    @Override
    public String resourceGroupName() {
        if (this.groupName == null) {
            return ResourceUtils.groupFromResourceId(this.id());
        } else {
            return this.groupName;
        }
    }

    /**************************************************
     * Tag setters.
     **************************************************/

    /**
     * Specifies tags for the resource as a {@link Map}.
     * @param tags a {@link Map} of tags
     * @return the next stage of the resource definition/update
     */
    @SuppressWarnings("unchecked")
    public final FluentModelImplT withTags(Map<String, String> tags) {
        this.inner().withTags(new HashMap<>(tags));
        return (FluentModelImplT) this;
    }

    /**
     * Adds a tag to the resource.
     * @param key the key for the tag
     * @param value the value for the tag
     * @return the next stage of the resource definition/update
     */
    @SuppressWarnings("unchecked")
    public final FluentModelImplT withTag(String key, String value) {
        this.inner().getTags().put(key, value);
        return (FluentModelImplT) this;
    }

    /**
     * Removes a tag from the resource.
     * @param key the key of the tag to remove
     * @return the next stage of the resource definition/update
     */
    @SuppressWarnings("unchecked")
    public final FluentModelImplT withoutTag(String key) {
        this.inner().getTags().remove(key);
        return (FluentModelImplT) this;
    }

    /**
     * @return <tt>true</tt> if currently in define..create mode
     */
    @Override
    public boolean isInCreateMode() {
        return this.inner().id() == null;
    }

    @Override
    public FluentModelImplT withExistingParentResource(String groupName, String parentName) {
        this.groupName = groupName;
        this.parentName = parentName;

        return (FluentModelImplT) this;
    }

    @Override
    public FluentModelImplT withExistingParentResource(FluentParentModelT existingParentResource) {
        this.inner().withLocation(existingParentResource.regionName());
        return withExistingParentResource(existingParentResource.resourceGroupName(), existingParentResource.name());
    }

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
        if (inner.id() != null) {
            this.parentName = ResourceId.parseResourceId(inner.id()).parent().name();
        }
        super.setInner(inner);
    }

    @Override
    public Observable<FluentModelT> createResourceAsync() {
        if (this.creatableParentResourceKey != null) {
            FluentParentModelT parentResource = (FluentParentModelT) this.createdResource(this.creatableParentResourceKey);
            withExistingParentResource(parentResource);
        }
        return this.createChildResourceAsync();
    }

    protected abstract Observable<FluentModelT> createChildResourceAsync();
}
