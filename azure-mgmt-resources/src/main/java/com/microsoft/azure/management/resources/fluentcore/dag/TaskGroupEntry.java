/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

import rx.Observable;
import rx.functions.Func0;

/**
 * Type representing an entry in {@link TaskGroup} that holds one {@link TaskItem} and associated
 * information which includes:
 * 1. references to other {@link TaskGroupEntry} dependencies
 * 2. references to the other {@link TaskGroupEntry} dependents
 *
 * @param <ResultT> the type of the result produced by the task
 * @param <TaskT> the task type that can return a value
 */
final class TaskGroupEntry<ResultT, TaskT extends TaskItem<ResultT>>
        extends DAGNode<TaskT, TaskGroupEntry<ResultT, TaskT>> {
    /**
     * indicates that one or more decedent dependency tasks are faulted.
     */
    private boolean hasFaultedDescentDependencyTask;

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
        this.hasFaultedDescentDependencyTask = false;
    }

    /**
     * @return the result produced by the task.
     */
    public ResultT taskResult() {
        return taskItem().result();
    }

    /**
     * Executes the task this entry holds.
     * if the task cannot be executed due to faulted dependencies then an observable that emit
     * {@link ErroredDependencyTaskException} will be returned.
     *
     * @param ignoreCachedResult indicate that whether the cached result can be returned without
     *                           re-running the task
     * @return the handle to the asynchronous execution of the task this entry holds.
     */
    public Observable<ResultT> executeTaskAsync(boolean ignoreCachedResult) {
        if (hasFaultedDescentDependencyTask) {
            return Observable.error(new ErroredDependencyTaskException());
        }
        final TaskT taskItem = this.taskItem();
        if (!ignoreCachedResult && hasCachedResult()) {
            return Observable.just(taskItem.result());
        }
        if (taskItem.isHot()) {
            // Convert hot task to cold to delay it's execution until subscription.
            return Observable.defer(new Func0<Observable<ResultT>>() {
                @Override
                public Observable<ResultT> call() {
                    return taskItem.executeAsync();
                }
            });
        } else {
            return taskItem.executeAsync();
        }
    }

    @Override
    protected void onFaultedResolution(String dependencyKey, Throwable throwable) {
        super.onFaultedResolution(dependencyKey, throwable);
        this.hasFaultedDescentDependencyTask = true;
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