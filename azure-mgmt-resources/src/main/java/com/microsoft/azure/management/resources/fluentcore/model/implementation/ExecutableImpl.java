/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.dag.TaskGroup;
import com.microsoft.azure.management.resources.fluentcore.dag.TaskGroupTerminateOnErrorStrategy;
import com.microsoft.azure.management.resources.fluentcore.dag.TaskItem;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Executable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;

/**
 * The base class for all executable model.
 *
 * @param <FluentModelT> the fluent model type
 */
public abstract class ExecutableImpl<FluentModelT extends Indexable>
        extends IndexableImpl
        implements
        Executable<FluentModelT>,
        TaskGroup.HasTaskGroup<FluentModelT, ExecuteTask<FluentModelT>>,
        ExecuteTask.Executor<FluentModelT> {
    /**
     * The group of tasks to the produces this result and it's dependencies results.
     */
    private final TaskGroup<FluentModelT, ExecuteTask<FluentModelT>> taskGroup;

    /**
     * Creates ExecutableImpl.
     */
    protected ExecutableImpl() {
        taskGroup = new TaskGroup<>(this.key(),
                new ExecuteTask<>(this),
                TaskGroupTerminateOnErrorStrategy.TERMINATE_ON_INPROGRESS_TASKS_COMPLETION);
    }

    @Override
    public TaskGroup<FluentModelT, ExecuteTask<FluentModelT>> taskGroup() {
        return this.taskGroup;
    }

    /**
     * Add a creatable dependency for the executable model.
     *
     * @param creatable the creatable dependency.
     */
    @SuppressWarnings("unchecked")
    protected void addCreatableDependency(Creatable<? extends Indexable> creatable) {
        TaskGroup.HasTaskGroup<FluentModelT, TaskItem<FluentModelT>> dependency =
                (TaskGroup.HasTaskGroup<FluentModelT, TaskItem<FluentModelT>>) creatable;

        Executable<FluentModelT> that = this;
        TaskGroup.HasTaskGroup<FluentModelT, TaskItem<FluentModelT>> thisDependent =
                (TaskGroup.HasTaskGroup<FluentModelT, TaskItem<FluentModelT>>) that;

        dependency.taskGroup().addDependentTaskGroup(thisDependent.taskGroup());
    }

    /**
     * Add an executable dependency for this executable model.
     *
     * @param executable the executable dependency
     */
    @SuppressWarnings("unchecked")
    protected void addExecutableDependency(Executable<? extends Indexable> executable) {
        TaskGroup.HasTaskGroup<FluentModelT, ExecuteTask<FluentModelT>> dependency =
                (TaskGroup.HasTaskGroup<FluentModelT, ExecuteTask<FluentModelT>>) executable;
        dependency.taskGroup().addDependentTaskGroup(this.taskGroup);
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean isHot() {
        return false;
    }

    @Override
    public Observable<FluentModelT> executeAsync() {
        return taskGroup.invokeAsync(taskGroup.newInvocationContext()).last();
    }

    @Override
    public FluentModelT execute() {
        return executeAsync().toBlocking().last();
    }

    @Override
    public ServiceFuture<FluentModelT> executeAsync(ServiceCallback<FluentModelT> callback) {
        return ServiceFuture.fromBody(executeAsync(), callback);
    }
}