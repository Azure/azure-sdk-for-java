package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.DAGNode;
import com.microsoft.azure.TaskGroup;
import com.microsoft.azure.TaskItem;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

/**
 * Represents a task that creates a resource when executed.
 */
public class CreatableTaskItem<ResourceT> implements TaskItem<ResourceT> {
    private CreatableTaskGroup.ResourceCreator<ResourceT> resourceCreator;
    private ResourceT created;

    /**
     * Creates CreatableTaskItem.
     *
     * @param resourceCreator the resource creator
     */
    public CreatableTaskItem(CreatableTaskGroup.ResourceCreator<ResourceT> resourceCreator) {
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
    public ServiceCall executeAsync(final TaskGroup<ResourceT, TaskItem<ResourceT>> taskGroup, final DAGNode<TaskItem<ResourceT>> node, final ServiceCallback<Void> callback) {
        final CreatableTaskItem<ResourceT> self = this;
        return (this.resourceCreator).createResourceAsync(new ServiceCallback<ResourceT>() {
            @Override
            public void failure(Throwable t) {
                callback.failure(t);
            }

            @Override
            public void success(ServiceResponse<ResourceT> result) {
                self.created = result.getBody();
                taskGroup.dag().reportedCompleted(node);
                taskGroup.executeAsync(callback);
            }
        });
    }
}
