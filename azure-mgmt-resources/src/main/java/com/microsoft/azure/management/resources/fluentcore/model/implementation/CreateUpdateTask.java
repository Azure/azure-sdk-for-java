/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.dag.TaskItem;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import rx.Observable;
import rx.functions.Action1;

/**
 * Represents a task that creates or updates a resource when executed.
 *
 * @param <ResourceT> the type of the resource that this task creates or update
 */
public class CreateUpdateTask<ResourceT> implements TaskItem<ResourceT> {
    /**
     * the underlying instance that can create and update the resource.
     */
    private ResourceCreatorUpdator<ResourceT> resourceCreatorUpdator;
    /**
     * created or updated resource.
     */
    private ResourceT resource;

    /**
     * Creates CreateUpdateTask.
     *
     * @param resourceCreatorUpdator the resource creator and updator
     */
    public CreateUpdateTask(ResourceCreatorUpdator<ResourceT> resourceCreatorUpdator) {
        this.resourceCreatorUpdator = resourceCreatorUpdator;
    }

    @Override
    public ResourceT result() {
        return resource;
    }

    @Override
    public void prepare() {
        this.resourceCreatorUpdator.prepare();
    }

    @Override
    public boolean isHot() {
        return this.resourceCreatorUpdator.isHot();
    }

    @Override
    public Observable<ResourceT> executeAsync() {
        if (this.resourceCreatorUpdator.isInCreateMode()) {
            return this.resourceCreatorUpdator.createResourceAsync()
                    .subscribeOn(SdkContext.getRxScheduler())
                    .doOnNext(new Action1<ResourceT>() {
                        @Override
                        public void call(ResourceT resourceT) {
                            resource = resourceT;
                        }
                    });
        } else {
            return this.resourceCreatorUpdator.updateResourceAsync()
                    .subscribeOn(SdkContext.getRxScheduler())
                    .doOnNext(new Action1<ResourceT>() {
                        @Override
                        public void call(ResourceT resourceT) {
                            resource = resourceT;
                        }
                    });
        }
    }

    /**
     * Represents a type that know how to create or update a resource of type {@link ResultT}.
     *
     * @param <ResultT> the resource type
     */
    public interface ResourceCreatorUpdator<ResultT> {
        /**
         * @return true if this creatorUpdator is in create mode.
         */
        boolean isInCreateMode();

        /**
         * prepare for create or update.
         */
        void prepare();

        /**
         * @return true if the observable returned by {@link this#createResourceAsync()} and
         * {@link this#updateResourceAsync()} are hot observables, false if they are cold observables.
         */
        boolean isHot();

        /**
         * Creates the resource asynchronously.
         *
         * @return the observable reference
         */
        Observable<ResultT> createResourceAsync();

        /**
         * Update the resource asynchronously.
         *
         * @return the observable reference
         */
        Observable<ResultT> updateResourceAsync();
    }
}
