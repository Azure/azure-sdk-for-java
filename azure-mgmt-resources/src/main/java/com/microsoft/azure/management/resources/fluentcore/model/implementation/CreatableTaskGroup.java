package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.TaskGroupBase;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;

/**
 * Type representing a group of tasks that can create resources that are dependents on each other.
 *
 * @param <ResourceT> the type of the resource this group creates
 */
public class CreatableTaskGroup<ResourceT> extends TaskGroupBase<ResourceT> {
    /**
     * Represents a type that know how to create resource.
     *
     * @param <T> the type of the resource that this creator creates
     */
    interface ResourceCreator<T> {
        /**
         * Creates the resource asynchronously.
         *
         * @param serviceCallback the callback to be invoked after the creation of resource
         * @return the service call reference
         */
        ServiceCall createResourceAsync(ServiceCallback<T> serviceCallback);

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
        CreatableTaskGroup creatableTaskGroup();
    }

    /**
     * Creates CreatableTaskGroup.
     *
     * @param rootCreatableId the id of the root creatable
     * @param resourceCreator represents the resource creator that this group want to create ultimately
     */
    public CreatableTaskGroup(String rootCreatableId, ResourceCreator<ResourceT> resourceCreator) {
        this(rootCreatableId, new CreatableTaskItem<>(resourceCreator));
    }

    /**
     * Creates CreatableTaskGroup.
     *
     * @param key the key of the root task
     * @param rootTask represents the root task that this group want to executes ultimately
     */
    public CreatableTaskGroup(String key, CreatableTaskItem<ResourceT> rootTask) {
        super(key, rootTask);
    }

    /**
     * Gets a resource created by a creator task in this group.
     * <p>
     * This method can return null if the resource has not yet created that happens if the responsible task
     * is not yet selected for execution or it's it progress
     *
     * @param key the resource id
     * @return the created resource
     */
    public ResourceT createdResource(String key) {
        return super.taskResult(key);
    }
}
