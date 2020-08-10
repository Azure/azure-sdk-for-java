// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.models.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The implementation for {@link Resource} and base class for all resource
 * model implementations.
 * (Internal use only)
 *
 * @param <FluentModelT> The fluent model type
 * @param <InnerModelT> Azure inner resource class type
 * @param <FluentModelImplT> the implementation type of the fluent model type
 */
public abstract class ResourceImpl<
        FluentModelT extends Resource,
        InnerModelT extends com.azure.core.management.Resource,
        FluentModelImplT extends ResourceImpl<FluentModelT, InnerModelT, FluentModelImplT>>
        extends
        CreatableUpdatableImpl<FluentModelT, InnerModelT, FluentModelImplT>
        implements
        Resource {
    protected ResourceImpl(String name, InnerModelT innerObject) {
        super(name, innerObject);
        if (innerObject.tags() == null) {
            innerObject.withTags(new TreeMap<String, String>());
        }
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
        Map<String, String> tags = this.inner().tags();
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
        this.inner().withTags(new HashMap<>(tags));
        return (FluentModelImplT) this;
    }

    /**
     * Adds a tag to the resource.
     *
     * @param key the key for the tag
     * @param value the value for the tag
     * @return the next stage of the definition/update
     */
    @SuppressWarnings("unchecked")
    public final FluentModelImplT withTag(String key, String value) {
        if (this.inner().tags() == null) {
            this.inner().withTags(new HashMap<String, String>());
        }
        this.inner().tags().put(key, value);
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
        if (this.inner().tags() != null) {
            this.inner().tags().remove(key);
        }
        return (FluentModelImplT) this;
    }

    /**********************************************
     * Region setters
     **********************************************/

    /**
     * Specifies the region for the resource by name.
     *
     * @param regionName The name of the region for the resource
     * @return the next stage of the definition/update
     */
    @SuppressWarnings("unchecked")
    public final FluentModelImplT withRegion(String regionName) {
        this.inner().withLocation(regionName);
        return (FluentModelImplT) this;
    }

    /**
     * Specifies the region for the resource.
     *
     * @param region The location for the resource
     * @return the next stage of the definition
     */
    public final FluentModelImplT withRegion(Region region) {
        return this.withRegion(region.toString());
    }

    /**
     * @return <tt>true</tt> if currently in define..create mode
     */
    @Override
    public boolean isInCreateMode() {
        return this.inner().id() == null;
    }

    protected <InnerT> List<InnerT> innersFromWrappers(Collection<? extends HasInner<InnerT>> wrappers) {
        return innersFromWrappers(wrappers, null);
    }

    protected <InnerT> List<InnerT> innersFromWrappers(Collection<? extends HasInner<InnerT>> wrappers,
                                                       List<InnerT> inners) {
        if (wrappers == null || wrappers.size() == 0) {
            return inners;
        } else {
            if (inners == null) {
                inners = new ArrayList<>();
            }
            for (HasInner<InnerT> wrapper : wrappers) {
                inners.add(wrapper.inner());
            }
            return inners;
        }
    }
}
