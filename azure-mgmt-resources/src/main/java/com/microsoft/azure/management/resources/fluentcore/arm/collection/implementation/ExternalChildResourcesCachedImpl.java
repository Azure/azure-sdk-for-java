/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import java.util.List;
import java.util.Map;

/**
 * Externalized cache-able child resource collection abstract implementation.
 * (Internal use only)
 * <p>
 * An external child resource collection is considered as cache-able when it is present as an inline
 * property of it's parent resource.
 * Consider using non-cached version {@link ExternalChildResourcesNonCachedImpl} if the child resources
 * are not present in the parent payload, using cached version in this case requires fetching the child resource
 * using separate GET call, that can be expensive if the child resources are pagable.
 *
 * @param <FluentModelTImpl> the implementation of {@param FluentModelT}
 * @param <FluentModelT> the fluent model type of the child resource
 * @param <InnerModelT> Azure inner resource class type representing the child resource
 * @param <ParentImplT> <ParentImplT> the parent Azure resource impl class type that implements {@link ParentT}
 * @param <ParentT> the parent interface
 */
public abstract class ExternalChildResourcesCachedImpl<
        FluentModelTImpl extends ExternalChildResourceImpl<FluentModelT, InnerModelT, ParentImplT, ParentT>,
        FluentModelT extends ExternalChildResource<FluentModelT, ParentT>,
        InnerModelT,
        ParentImplT extends ParentT,
        ParentT>
        extends ExternalChildResourceCollectionImpl<FluentModelTImpl, FluentModelT, InnerModelT, ParentImplT, ParentT> {
    /**
     * Creates a new ExternalChildResourcesImpl.
     *
     * @param parent the parent Azure resource
     * @param childResourceName the child resource name
     */
    protected ExternalChildResourcesCachedImpl(ParentImplT parent, String childResourceName) {
        super(parent, childResourceName);
    }

    /**
     * Refresh the child resource collection.
     */
    public void refresh() {
        cacheCollection();
    }

    /**
     * @return the childCollection of external child resources.
     */
    protected Map<String, FluentModelTImpl> collection() {
        return this.childCollection;
    }

    /**
     * Prepare for definition of a new external child resource.
     *
     * @param name the name for the new external child resource
     * @return the child resource
     */
    protected FluentModelTImpl prepareDefine(String name) {
        return prepareDefine(name, name);
    }

    /**
     * Prepare for definition of a new external child resource.
     *
     * @param name the name of the new external child resource
     * @param key the key
     * @return the child resource
     */
    protected final FluentModelTImpl prepareDefine(String name, String key) {
        if (find(key) != null) {
            throw new IllegalArgumentException("A child resource ('" + childResourceName + "') with name (key) '" + name + " (" + key + ")' already exists");
        }
        FluentModelTImpl childResource = newChildResource(name);
        childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
        return childResource;
    }

    /**
     * Prepare for an external child resource update.
     *
     * @param name the name of the external child resource
     * @return the external child resource to be updated
     */
    protected final FluentModelTImpl prepareUpdate(String name) {
        return prepareUpdate(name, name);
    }

    /**
     * Prepare for an external child resource update.
     *
     * @param name the name of the external child resource
     * @param key the key
     * @return the external child resource to be updated
     */
    protected final FluentModelTImpl prepareUpdate(String name, String key) {
        FluentModelTImpl childResource = find(key);
        if (childResource == null
                || childResource.pendingOperation() == ExternalChildResourceImpl.PendingOperation.ToBeCreated) {
            throw new IllegalArgumentException("A child resource ('" + childResourceName + "') with name (key) '" + name + " (" + key + ")' not found");
        }
        if (childResource.pendingOperation() == ExternalChildResourceImpl.PendingOperation.ToBeRemoved) {
            throw new IllegalArgumentException("A child resource ('" + childResourceName + "') with name (key) '" + name + " (" + key + ")' is marked for deletion");
        }
        childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeUpdated);
        return childResource;
    }

    /**
     * Mark an external child resource with given name as to be removed.
     *
     * @param name the name of the external child resource
     */
    protected final void prepareRemove(String name) {
        prepareRemove(name, name);
    }

    /**
     * Mark an external child resource with given key as to be removed.
     *
     * @param name the name of the external child resource
     * @param key the key
     */
    protected final void prepareRemove(String name, String key) {
        FluentModelTImpl childResource = find(key);
        if (childResource == null
                || childResource.pendingOperation() == ExternalChildResourceImpl.PendingOperation.ToBeCreated) {
            throw new IllegalArgumentException("A child resource ('" + childResourceName + "') with name (key) '" + name + " (" + key + ")' not found");
        }
        childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeRemoved);
    }

    /**
     * Adds an external child resource to the childCollection.
     *
     * @param childResource the external child resource
     */
    protected void addChildResource(FluentModelTImpl childResource) {
        this.addChildResource(childResource.name(), childResource);
    }

    /**
     * Adds an external child resource to the childCollection.
     *
     * @param key the key
     * @param childResource the external child resource
     */
    protected void addChildResource(String key, FluentModelTImpl childResource) {
        this.childCollection.put(key, childResource);
    }

    /**
     * Initializes the external child resource collection.
     */
    protected void cacheCollection() {
        this.childCollection.clear();
        for (FluentModelTImpl childResource : this.listChildResources()) {
            this.childCollection.put(childResource.childResourceKey(), childResource);
        }
    }

    @Override
    protected final boolean clearAfterCommit() {
        return false;
    }

    /**
     * Gets the list of external child resources.
     *
     * @return the list of external child resources
     */
    protected abstract List<FluentModelTImpl> listChildResources();

    /**
     * Gets a new external child resource model instance.
     *
     * @param name the name for the new child resource
     * @return the new child resource
     */
    protected abstract FluentModelTImpl newChildResource(String name);
}
