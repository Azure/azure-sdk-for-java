package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.TaskItem;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Represents a task that creates a resource when executed.
 *
 * @param <ResourceT> the type of the resource that this task creates
 */
public class CreatorTaskItem<ResourceT> implements TaskItem<ResourceT> {
    private CreatorTaskGroup.ResourceCreator<ResourceT> resourceCreator;
    private ResourceT created;

    /**
     * Creates CreatorTaskItem.
     *
     * @param resourceCreator the resource creator
     */
    public CreatorTaskItem(CreatorTaskGroup.ResourceCreator<ResourceT> resourceCreator) {
        this.resourceCreator = resourceCreator;
    }

    @Override
    public ResourceT result() {
        return created;
    }

    @Override
    public Observable<ResourceT> executeAsync() {
        return this.resourceCreator.createResourceAsync()
            .subscribeOn(Schedulers.io())
            .doOnNext(new Action1<ResourceT>() {
                        @Override
                        public void call(ResourceT resourceT) {
                            created = resourceT;
                        }
                    });
    }
}
