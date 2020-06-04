// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskGroup;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskItem;
import reactor.core.publisher.Mono;

/**
 * A {@link TaskItem} type, when invoked it create or update a resource using
 * the {@link ResourceCreatorUpdater} it composes.
 *
 * @param <ResourceT> the type of the resource that this task creates or update
 */
public class CreateUpdateTask<ResourceT extends Indexable> implements TaskItem {
    /**
     * the underlying instance that can create and update the resource.
     */
    private ResourceCreatorUpdater<ResourceT> resourceCreatorUpdater;
    /**
     * created or updated resource.
     */
    private ResourceT resource;

    /**
     * Creates CreateUpdateTask.
     *
     * @param resourceCreatorUpdater the resource creator and updater used by this TaskItem
     *                               to create or update the resource when invoked.
     */
    public CreateUpdateTask(ResourceCreatorUpdater<ResourceT> resourceCreatorUpdater) {
        this.resourceCreatorUpdater = resourceCreatorUpdater;
    }

    @Override
    public ResourceT result() {
        return resource;
    }

    @Override
    public void beforeGroupInvoke() {
        this.resourceCreatorUpdater.beforeGroupCreateOrUpdate();
    }

    @Override
    public Mono<Indexable> invokeAsync(TaskGroup.InvocationContext context) {
        if (this.resourceCreatorUpdater.isInCreateMode()) {
            return this.resourceCreatorUpdater.createResourceAsync()
                    .subscribeOn(SdkContext.getReactorScheduler())
                    .doOnNext(resourceT -> resource = resourceT)
                    .map(resourceT -> resourceT);
        } else {
            return this.resourceCreatorUpdater.updateResourceAsync()
                    .subscribeOn(SdkContext.getReactorScheduler())
                    .doOnNext(resourceT -> resource = resourceT)
                    .map(resourceT -> resourceT);
        }
    }

    @Override
    public Mono<Void> invokeAfterPostRunAsync(boolean isGroupFaulted) {
        return this.resourceCreatorUpdater.afterPostRunAsync(isGroupFaulted);
    }

    @Override
    public boolean isHot() {
        return this.resourceCreatorUpdater.isHot();
    }


    /**
     * Represents a type that know how to create or update a resource of type {@link T}.
     * <p>
     * An instance of {@link CreateUpdateTask} wraps this type and invokes appropriate
     * methods when CreateUpdateTask methods get called during TaskGroup invocation.
     *
     * @param <T> the resource type
     */
    public interface ResourceCreatorUpdater<T extends Indexable> {
        /**
         * @return true if this creatorUpdater is in create mode.
         */
        boolean isInCreateMode();

        /**
         * The method that gets called before invoking all the tasks in the {@link TaskGroup}
         * that the parent {@link CreateUpdateTask} belongs to.
         */
        void beforeGroupCreateOrUpdate();

        /**
         * Creates the resource asynchronously.
         *
         * @return an observable that create the resource when subscribed
         */
        Mono<T> createResourceAsync();

        /**
         * Update the resource asynchronously.
         *
         * @return an observable that update the resource when subscribed
         */
        Mono<T> updateResourceAsync();

        /**
         * @return true if the observable returned by {@link this#createResourceAsync()} and
         * {@link this#updateResourceAsync()} are hot observables, false if they are cold
         * observables.
         */
        boolean isHot();

        /**
         * Perform any action followed by the processing of work scheduled to be invoked
         * (i.e. "post run") after {@link this#createResourceAsync()} or
         * {@link this#updateResourceAsync()}.
         *
         * @param isGroupFaulted true if one or more tasks in the group this creatorUpdater
         *                       belongs to are in faulted state.
         * @return a completable represents the asynchronous action
         */
        Mono<Void> afterPostRunAsync(boolean isGroupFaulted);
    }
}
