/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.arm.models.implementation;

import com.azure.management.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.management.resources.fluentcore.arm.models.IndependentChildResource;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.arm.models.Resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation for the child resource which can be CRUDed independently from the parent resource.
 * (internal use only)
 *
 * @param <FluentModelT>       The fluent model type
 * @param <FluentParentModelT> the fluent model for parent resource
 * @param <InnerModelT>        Azure inner resource class type
 * @param <FluentModelImplT>   the implementation type of the fluent model type
 * @param <ManagerT>           the client manager type representing the service
 */
public abstract class IndependentChildResourceImpl<
        FluentModelT extends IndependentChildResource<ManagerT, InnerModelT>,
        FluentParentModelT extends Resource & HasResourceGroup,
        InnerModelT extends com.azure.core.management.Resource,
        FluentModelImplT extends IndependentChildResourceImpl<FluentModelT, FluentParentModelT, InnerModelT, FluentModelImplT, ManagerT>,
        ManagerT>
        extends
        IndependentChildImpl<FluentModelT, FluentParentModelT, InnerModelT, FluentModelImplT, ManagerT>
        implements
        IndependentChildResource<ManagerT, InnerModelT> {
    /**
     * Creates a new instance of CreatableUpdatableImpl.
     *
     * @param name        the name of the resource
     * @param innerObject the inner object
     */
    protected IndependentChildResourceImpl(String name, InnerModelT innerObject, ManagerT manager) {
        super(name, innerObject, manager);
    }

    /*******************************************
     * Getters.
     *******************************************/

    @Override
    public String regionName() {
        return this.inner().getLocation();
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
        if (this.inner() != null) {
            return this.inner().getId();
        }

        return null;
    }

    @Override
    public String type() {
        return this.inner().getType();
    }

    @Override
    public String name() {
        if (this.inner().getName() == null) {
            return super.name();
        } else {
            return this.inner().getName();
        }
    }

    /**************************************************
     * Tag setters.
     **************************************************/

    /**
     * Specifies tags for the resource as a {@link Map}.
     *
     * @param tags a {@link Map} of tags
     * @return the next stage of the definition/update
     */
    @SuppressWarnings("unchecked")
    public final FluentModelImplT withTags(Map<String, String> tags) {
        this.inner().setTags(new HashMap<>(tags));
        return (FluentModelImplT) this;
    }

    /**
     * Adds a tag to the resource.
     *
     * @param key   the key for the tag
     * @param value the value for the tag
     * @return the next stage of the definition/update
     */
    @SuppressWarnings("unchecked")
    public final FluentModelImplT withTag(String key, String value) {
        if (this.inner().getTags() == null) {
            this.inner().setTags(new HashMap<String, String>());
        }
        this.inner().getTags().put(key, value);
        return (FluentModelImplT) this;
    }

    /**
     * Removes a tag from the resource.
     *
     * @param key the key of the tag to remove
     * @return the next stage of the definition/update
     */
    @SuppressWarnings("unchecked")
    public final FluentModelImplT withoutTag(String key) {
        if (this.inner().getTags() != null) {
            this.inner().getTags().remove(key);
        }
        return (FluentModelImplT) this;
    }

    /**
     * @return <tt>true</tt> if currently in define..create mode
     */
    @Override
    public boolean isInCreateMode() {
        return this.inner().getId() == null;
    }

    @Override
    public FluentModelImplT withExistingParentResource(FluentParentModelT existingParentResource) {
        this.inner().setLocation(existingParentResource.regionName());
        return super.withExistingParentResource(existingParentResource);
    }
}
