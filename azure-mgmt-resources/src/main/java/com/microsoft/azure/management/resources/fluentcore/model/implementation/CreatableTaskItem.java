package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.TaskItem;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

/**
 * Represents a task that creates a resource when executed.
 */
public class CreatableTaskItem implements TaskItem<Resource> {
    private Creatable<?> creatable;
    private Resource created;

    /**
     * Creates CreatableTaskItem
     *
     * @param creatable the creatable
     */
    public CreatableTaskItem(Creatable<?> creatable) {
        this.creatable = creatable;
    }

    @Override
    public Resource result() {
        return created;
    }

    @Override
    public void execute() throws Exception {
        this.created = (Resource) this.creatable.create();
    }
}
