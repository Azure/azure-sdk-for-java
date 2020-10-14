// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskGroup;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
 * @param <ParentImplT> the parent Azure resource impl class type that implements {@link ParentT}
 * @param <ParentT> the parent interface
 */
public abstract class ExternalChildResourcesCachedImpl<
        FluentModelTImpl extends ExternalChildResourceImpl<FluentModelT, InnerModelT, ParentImplT, ParentT>,
        FluentModelT extends ExternalChildResource<FluentModelT, ParentT>,
        InnerModelT,
        ParentImplT extends ParentT,
        ParentT>
        extends ExternalChildResourceCollectionImpl<FluentModelTImpl, FluentModelT, InnerModelT, ParentImplT, ParentT> {
    private final ClientLogger logger = new ClientLogger(this.getClass());
    private static final String ERROR_MESSAGE_FORMAT = "A child resource ('%s') with name (key) '%s (%s)' %s";
    /**
     * Creates a new ExternalChildResourcesImpl.
     *
     * @param parent the parent Azure resource
     * @param parentTaskGroup the TaskGroup the parent Azure resource belongs to
     * @param childResourceName the child resource name
     */
    protected ExternalChildResourcesCachedImpl(ParentImplT parent,
                                               TaskGroup parentTaskGroup, String childResourceName) {
        super(parent, parentTaskGroup, childResourceName);
    }

    /**
     * Refresh the child resource collection.
     */
    public Mono<Void> refreshAsync() {
        return cacheCollectionAsync();
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
     * Prepare for independent definition of a new external child resource (without the parent context).
     *
     * @param name the name of the new external child resource
     * @return the external child resource prepared for create
     */
    protected final FluentModelTImpl prepareIndependentDefine(String name) {
        FluentModelTImpl childResource = newChildResource(name);
        childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
        return childResource;
    }

    /**
     * Prepare for inline definition of a new external child resource
     * (along with the definition or update of parent resource).
     *
     * @param name the name for the new external child resource
     * @return the child resource
     */
    protected FluentModelTImpl prepareInlineDefine(String name) {
        return prepareInlineDefine(name, name);
    }

    /**
     * Prepare for inline definition of a new external child resource
     * (along with the definition or update of parent resource).
     *
     * @param name the name of the new external child resource
     * @param key the key
     * @return the child resource
     */
    protected final FluentModelTImpl prepareInlineDefine(String name, String key) {
        if (find(key) != null) {
            String errorMessage = String.format(ERROR_MESSAGE_FORMAT, childResourceName, name, key, "already exists");
            throw logger.logExceptionAsError(new IllegalArgumentException(errorMessage));
        }
        FluentModelTImpl childResource = newChildResource(name);
        childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
        return super.prepareForFutureCommitOrPostRun(childResource);
    }

    /**
     * Prepare for inline update of an external child resource (along with the update of parent resource).
     *
     * @param name the name of the external child resource
     * @return the external child resource to be updated
     */
    protected final FluentModelTImpl prepareInlineUpdate(String name) {
        return prepareInlineUpdate(name, name);
    }

    /**
     * Prepare for inline update of an external child resource (along with the update of parent resource).
     *
     * @param name the name of the external child resource
     * @param key the key
     * @return the external child resource to be updated
     */
    protected final FluentModelTImpl prepareInlineUpdate(String name, String key) {
        FluentModelTImpl childResource = find(key);
        if (childResource == null
                || childResource.pendingOperation() == ExternalChildResourceImpl.PendingOperation.ToBeCreated) {
            String errorMessage = String.format(ERROR_MESSAGE_FORMAT, childResourceName, name, key, "not found");
            throw logger.logExceptionAsError(new IllegalArgumentException(errorMessage));
        }
        if (childResource.pendingOperation() == ExternalChildResourceImpl.PendingOperation.ToBeRemoved) {
            String errorMessage = String.format(ERROR_MESSAGE_FORMAT,
                childResourceName, name, key, "is marked for deletion");
            throw logger.logExceptionAsError(new IllegalArgumentException(errorMessage));
        }
        childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeUpdated);
        return super.prepareForFutureCommitOrPostRun(childResource);
    }

    /**
     * Prepare for inline removal of an external child resource (along with the update of parent resource).
     *
     * @param name the name of the external child resource
     */
    protected final void prepareInlineRemove(String name) {
        prepareInlineRemove(name, name);
    }

    /**
     * Prepare for inline removal of an external child resource (along with the update of parent resource).
     *
     * @param name the name of the external child resource
     * @param key the key
     */
    protected final void prepareInlineRemove(String name, String key) {
        FluentModelTImpl childResource = find(key);
        if (childResource == null
                || childResource.pendingOperation() == ExternalChildResourceImpl.PendingOperation.ToBeCreated) {
            String errorMessage = String.format(ERROR_MESSAGE_FORMAT, childResourceName, name, key, "not found");
            throw logger.logExceptionAsError(new IllegalArgumentException(errorMessage));
        }
        childResource.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeRemoved);
        super.prepareForFutureCommitOrPostRun(childResource);
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
        this.clear();
        this.listChildResources()
            .forEach(childResource -> this.childCollection.put(childResource.childResourceKey(), childResource));
    }

    /**
     * Initializes the external child resource collection.
     */
    protected Mono<Void> cacheCollectionAsync() {
        this.clear();
        return this.listChildResourcesAsync()
            .doOnNext(childResource -> this.childCollection.put(childResource.childResourceKey(), childResource))
            .then();
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
     * Gets the list of external child resources.
     *
     * @return the list of external child resources
     */
    protected abstract Flux<FluentModelTImpl> listChildResourcesAsync();

    /**
     * Gets a new external child resource model instance.
     *
     * @param name the name for the new child resource
     * @return the new child resource
     */
    protected abstract FluentModelTImpl newChildResource(String name);
}
