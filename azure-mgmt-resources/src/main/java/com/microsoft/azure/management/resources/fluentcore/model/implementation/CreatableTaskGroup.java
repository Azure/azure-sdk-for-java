package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.TaskGroupBase;
import com.microsoft.azure.TaskItem;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;

/**
 * Type representing a group of creatable tasks and the dependency between them.
 */
public class CreatableTaskGroup extends TaskGroupBase<Resource> {

    /**
     * Represents a type that know how to create the root resource in a CreatableTaskGroup.
     */
    interface RootResourceCreator {
        /**
         * Creates the root resource.
         */
        void createRootResource() throws Exception;

        ServiceCall createRootResourceAsync(ServiceCallback<Void> serviceCallback);
    }

    private final RootResourceCreator rootCreate;

    /**
     * Creates CreatableTaskGroup.
     *
     * @param rootCreatableId the id of the root creatable
     * @param rootCreatable represents the root resource creatable that this group want to create ultimately
     * @param rootCreate {@link RootResourceCreator} that know how to create the rootCreatable once all the
     *                                              dependencies are available
     */
    public CreatableTaskGroup(String rootCreatableId, Creatable<? extends Resource> rootCreatable, RootResourceCreator rootCreate) {
        this(rootCreatableId, new CreatableTaskItem(rootCreatable), rootCreate);
    }

    /**
     * Creates CreatableTaskGroup.
     *
     * @param key the key of the root task
     * @param rootTask represents the root task that this group want to executes ultimately
     * @param rootCreate {@link RootResourceCreator} that know how to create the rootCreatable once all the
     *                                              dependencies are available
     */
    public CreatableTaskGroup(String key, CreatableTaskItem rootTask, RootResourceCreator rootCreate) {
        super(key, rootTask);
        this.rootCreate = rootCreate;
    }

    /**
     * Gets a resource created by a creatable task in this group.
     * <p>
     * this method can null if the resource has not yet created that happens if the responsible task is not
     * yet selected for execution or it's it progress
     *
     * @param key the resource id
     * @return the created resource
     */
    public Resource createdResource(String key) {
        return super.taskResult(key);
    }

    @Override
    public void executeRootTask(TaskItem<Resource> task) throws Exception {
        this.rootCreate.createRootResource();
    }

    @Override
    public ServiceCall executeRootTaskAsync(TaskItem<Resource> task, ServiceCallback<Void> callback) {
        return this.rootCreate.createRootResourceAsync(callback);
    }
}
