package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.TaskGroupBase;
import rx.Observable;

/**
 * Type representing a group of tasks that can create resources that are dependents on each other.
 *
 * @param <ResourceT> the type of the resource this group creates
 */
public class CreatorTaskGroup<ResourceT> extends TaskGroupBase<ResourceT, CreatorTaskItem<ResourceT>> {
    /**
     * Represents a type that know how to create resource.
     *
     * @param <T> the type of the resource that this creator creates
     */
    interface ResourceCreator<T> {
        /**
         * Creates the resource asynchronously.
         *
         * @return the observable reference
         */
        Observable<T> createResourceAsync();

        /**
         * Creates the resource synchronously.
         *
         * @return the created resource
         * @throws Exception
         */
        T createResource() throws Exception;

        /**
         * @return Gets the task group.
         */
        CreatorTaskGroup<T> creatorTaskGroup();
    }

    /**
     * Creates CreatorTaskGroup.
     *
     * @param key the key of the root task
     * @param resourceCreator represents the resource creator that this group want to create ultimately
     */
    public CreatorTaskGroup(String key, ResourceCreator<ResourceT> resourceCreator) {
        this(key, new CreatorTaskItem<>(resourceCreator));
    }

    /**
     * Creates CreatorTaskGroup.
     *
     * @param key the key of the root task
     * @param rootTask represents the root task that this group want to executes ultimately
     */
    public CreatorTaskGroup(String key, CreatorTaskItem<ResourceT> rootTask) {
        super(key, rootTask);
    }

    /**
     * Gets a resource created by a creator task in this group.
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
