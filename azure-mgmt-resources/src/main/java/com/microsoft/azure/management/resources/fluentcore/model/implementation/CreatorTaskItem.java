package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.DAGNode;
import com.microsoft.azure.TaskGroup;
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
    public void execute(TaskGroup<ResourceT, TaskItem<ResourceT>> taskGroup, DAGNode<TaskItem<ResourceT>> node) throws Exception {
        if (this.created == null) {
            // execute will be called both in update and create scenarios,
            // so run the task only if it not not executed already.
            this.created = this.resourceCreator.createResource();
        }

        taskGroup.dag().reportedCompleted(node);
        taskGroup.execute();
    }

    @Override
    public ServiceCall executeAsync(final TaskGroup<ResourceT, TaskItem<ResourceT>> taskGroup, final DAGNode<TaskItem<ResourceT>> node, final boolean isRootNode, final ServiceCallback<ResourceT> callback) {
        final CreatorTaskItem<ResourceT> self = this;
        return (this.resourceCreator).createResourceAsync(new ServiceCallback<ResourceT>() {
            @Override
            public void failure(Throwable t) {
                callback.failure(t);
            }

            @Override
            public void success(ServiceResponse<ResourceT> result) {
                self.created = result.getBody();
                taskGroup.dag().reportedCompleted(node);
                if (isRootNode) {
                    callback.success(result);
                } else {
                    taskGroup.executeAsync(callback);
                }
            }
        });
    }
}
