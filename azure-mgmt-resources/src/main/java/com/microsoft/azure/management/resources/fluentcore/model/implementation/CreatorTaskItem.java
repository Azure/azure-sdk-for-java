package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.TaskItem;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

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
    public void execute() throws Exception {
        if (this.created == null) {
            // execute will be called both in update and create scenarios,
            // so run the task only if it not not executed already.
            this.created = this.resourceCreator.createResource();
        }
    }

    @Override
    public ServiceCall executeAsync(final ServiceCallback<ResourceT> callback) {
        final CreatorTaskItem<ResourceT> self = this;
        return (this.resourceCreator).createResourceAsync(new ServiceCallback<ResourceT>() {
            @Override
            public void failure(Throwable t) {
                callback.failure(t);
            }

            @Override
            public void success(ServiceResponse<ResourceT> result) {
                self.created = result.getBody();
                callback.success(result);
            }
        });
    }

    /**
     * @return the unique id of the creator
     */
    public String uuid() {
        return resourceCreator.uuid();
    }

    /**
     * @return the unique key assigned to the created resource.
     */
    public String resourceKey() {
        return resourceCreator.resourceKey();
    }
}
