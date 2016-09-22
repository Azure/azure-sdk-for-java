package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.TaskItem;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Represents a task that creates or updates a resource when executed.
 *
 * @param <ResourceT> the type of the resource that this task creates or update
 */
public class CreateUpdateTask<ResourceT> implements TaskItem<ResourceT> {
    private CreateUpdateTaskGroup.ResourceCreatorUpdator<ResourceT> resourceCreatorUpdator;
    private ResourceT resource;

    /**
     * Creates CreateUpdateTask.
     *
     * @param resourceCreatorUpdator the resource creator and updator
     */
    public CreateUpdateTask(CreateUpdateTaskGroup.ResourceCreatorUpdator<ResourceT> resourceCreatorUpdator) {
        this.resourceCreatorUpdator = resourceCreatorUpdator;
    }

    @Override
    public ResourceT result() {
        return resource;
    }

    @Override
    public Observable<ResourceT> executeAsync() {
        if (this.resourceCreatorUpdator.isInCreateMode()) {
            return this.resourceCreatorUpdator.createResourceAsync()
                    .subscribeOn(Schedulers.io())
                    .doOnNext(new Action1<ResourceT>() {
                        @Override
                        public void call(ResourceT resourceT) {
                            resource = resourceT;
                        }
                    });
        } else {
            return this.resourceCreatorUpdator.updateResourceAsync()
                    .subscribeOn(Schedulers.io())
                    .doOnNext(new Action1<ResourceT>() {
                        @Override
                        public void call(ResourceT resourceT) {
                            resource = resourceT;
                        }
                    });
        }
    }
}
