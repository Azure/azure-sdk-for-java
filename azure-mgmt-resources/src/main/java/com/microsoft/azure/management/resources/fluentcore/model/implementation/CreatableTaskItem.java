package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.DAGNode;
import com.microsoft.azure.TaskGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.TaskItem;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

/**
 * Represents a task that creates a resource when executed.
 */
public class CreatableTaskItem implements TaskItem<Resource> {
    private Creatable<? extends Resource> creatable;
    private DAGNode<TaskItem<Resource>> node;
    private Resource created;

    /**
     * Creates CreatableTaskItem.
     *
     * @param creatable the creatable
     */
    public CreatableTaskItem(Creatable<? extends Resource> creatable) {
        this.creatable = creatable;
    }

    @Override
    public Resource result() {
        return created;
    }

    @Override
    public void execute(TaskGroup<Resource, TaskItem<Resource>> taskGroup, DAGNode<TaskItem<Resource>> node) throws Exception {
        this.created = this.creatable.create();
        taskGroup.dag().reportedCompleted(node);
        taskGroup.execute();
    }

    @Override
    public ServiceCall executeAsync(final TaskGroup<Resource, TaskItem<Resource>> taskGroup, final DAGNode<TaskItem<Resource>> node, final ServiceCallback<Void> callback) {
        final CreatableTaskItem self = this;
        return ((Creatable<Resource>) this.creatable).createAsync(new ServiceCallback<Resource>() {
            @Override
            public void failure(Throwable t) {
                callback.failure(t);
            }

            @Override
            public void success(ServiceResponse<Resource> result) {
                self.created = result.getBody();
                taskGroup.dag().reportedCompleted(node);
                taskGroup.executeAsync(callback);
            }
        });
    }
}
