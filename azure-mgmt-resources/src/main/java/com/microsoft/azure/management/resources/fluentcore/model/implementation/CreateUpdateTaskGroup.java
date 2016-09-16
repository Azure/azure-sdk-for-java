package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.TaskGroupBase;
import rx.Observable;

/**
 * Type representing a group of tasks that can create resources that are dependents on each other.
 *
 * @param <ResourceT> the type of the resource this group creates
 */
public class CreateUpdateTaskGroup<ResourceT> extends TaskGroupBase<ResourceT, CreateUpdateTask<ResourceT>> {
    /**
     * Represents a type that know how to create or update resource.
     *
     * @param <T> the type of the resource that this creatorUpdator can create or update
     */
    interface ResourceCreatorUpdator<T> {
        /**
         * Creates the resource asynchronously.
         *
         * @return the observable reference
         */
        Observable<T> createResourceAsync();

        /**
         * Update the resource asynchronously.
         *
         * @return the observable reference
         */
        Observable<T> updateResourceAsync();
        /**
         * @return true if this creatorUpdator is in create mode.
         */
        boolean isInCreateMode();
        /**
         * @return Gets the task group.
         */
        CreateUpdateTaskGroup<T> creatorUpdatorTaskGroup();
    }

    /**
     * Creates CreateUpdateTaskGroup.
     *
     * @param key the key of the root task
     * @param resourceCreatorUpdator represents the resource creator that this group want to create or update ultimately
     */
    public CreateUpdateTaskGroup(String key, ResourceCreatorUpdator<ResourceT> resourceCreatorUpdator) {
        this(key, new CreateUpdateTask<>(resourceCreatorUpdator));
    }

    /**
     * Creates CreateUpdateTaskGroup.
     *
     * @param key the key of the root task
     * @param rootTask represents the root task that this group want to executes ultimately
     */
    public CreateUpdateTaskGroup(String key, CreateUpdateTask<ResourceT> rootTask) {
        super(key, rootTask);
    }

    /**
     * Gets a resource created or updated by a create-update task in this group.
     * <p>
     * This method can return null if the resource has not yet created that happens if the responsible task
     * is not yet selected for execution or it's it progress or provided key is invalid.
     *
     * @param key the resource key
     * @return the created resource
     */
    public ResourceT createdResource(String key) {
        return super.taskResult(key);
    }
}
