// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import reactor.core.publisher.Mono;

/**
 * Type representing an entry in {@link TaskGroup} that holds one {@link TaskItem} and associated
 * information which includes:
 * 1. references to other {@link TaskGroupEntry} dependencies
 * 2. references to the other {@link TaskGroupEntry} dependents
 *
 * @param <TaskT> the task type that can return a value
 */
public final class TaskGroupEntry<TaskT extends TaskItem>
        extends DAGNode<TaskT, TaskGroupEntry<TaskT>> {
    /**
     * The proxy entry for this entry if exists.
     */
    private TaskGroupEntry<TaskT> proxy;

    /**
     * indicates that one or more decedent dependency tasks are faulted.
     */
    private boolean hasFaultedDescentDependencyTasks;

    /**
     * Creates TaskGroupEntry.
     *
     * @param taskId id that uniquely identifies the task from other tasks in the group
     * @param taskItem the task this entry holds
     */
    TaskGroupEntry(String taskId, TaskT taskItem) {
        super(taskId, taskItem);
    }

    @Override
    public void initialize() {
        super.initialize();
        this.hasFaultedDescentDependencyTasks = false;
    }

    /**
     * Set the proxy entry for this entry.
     *
     * @param proxy the proxy entry
     */
    public void setProxy(TaskGroupEntry<TaskT> proxy) {
        this.proxy = proxy;
    }

    /**
     * @return the proxy entry if it is set, null if not set.
     */
    public TaskGroupEntry<TaskT> proxy() {
        return this.proxy;
    }

    /**
     * @return the result produced by the task.
     */
    public Indexable taskResult() {
        return taskItem().result();
    }

    /**
     * @return true if one or more decedent dependency tasks are in faulted
     * state, false otherwise.
     */
    public boolean hasFaultedDescentDependencyTasks() {
        return this.hasFaultedDescentDependencyTasks;
    }

    /**
     * Invokes the task this entry holds.
     * if the task cannot be invoked due to faulted dependencies then an observable that emit
     * {@link ErroredDependencyTaskException} will be returned.
     *
     * @param ignoreCachedResult if the task is already invoked and has result cached then a value false for this
     *                           parameter indicates the cached result can be returned without invoking task again,
     *                           if true then cached result will be ignored and task will be invoked
     * @param context the context object shared across all the entries in the group that this entry belongs to,
     *                           this will be passed to {@link TaskItem#invokeAsync(TaskGroup.InvocationContext)}
     *                           method of the task item
     * @return a cold Observable upon subscription invokes the task this entry hold, which produces a result of
     * type {@link Indexable}.
     */
    public Mono<Indexable> invokeTaskAsync(boolean ignoreCachedResult, final TaskGroup.InvocationContext context) {
        if (hasFaultedDescentDependencyTasks) {
            return Mono.error(new ErroredDependencyTaskException());
        }
        final TaskT taskItem = this.taskItem();
        if (!ignoreCachedResult && hasCachedResult()) {
            return Mono.just(taskItem.result());
        }
        if (taskItem.isHot()) {
            // Convert hot task to cold to delay it's execution until subscription.
            return Mono.defer(() -> taskItem.invokeAsync(context));
        } else {
            return taskItem.invokeAsync(context);
        }
    }

    @Override
    protected void onFaultedResolution(String dependencyKey, Throwable throwable) {
        super.onFaultedResolution(dependencyKey, throwable);
        this.hasFaultedDescentDependencyTasks = true;
    }

    /**
     * @return the {@link TaskItem} this entry holds.
     */
    private TaskT taskItem() {
        return super.data();
    }

    /**
     * @return true, if the result of the task is cached.
     */
    private boolean hasCachedResult() {
        return taskItem().result() != null;
    }
}
