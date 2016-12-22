/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

import rx.Observable;

/**
 * Type representing an entry in {@link TaskGroup} that holds one {@link TaskItem} and associated
 * information which includes:
 * 1. references to other {@link TaskGroupEntry} dependencies
 * 2. references to the other {@link TaskGroupEntry} dependents
 *
 * @param <ResultT> the type of the result produced by the task
 * @param <TaskT> represents a task that can return a value
 */
public class TaskGroupEntry<ResultT, TaskT extends TaskItem<ResultT>>
        extends DAGNode<TaskT, TaskGroupEntry<ResultT, TaskT>> {
    /**
     * Creates TaskGroupEntry.
     *
     * @param taskId the unique id of the task
     * @param taskItem the task
     */
    public TaskGroupEntry(String taskId, TaskT taskItem) {
        super(taskId, taskItem);
    }

    /**
     * @return true, if the result of the task is cached.
     */
    boolean hasTaskResult() {
        return super.data().result() != null;
    }

    /**
     * @return the result produced by the task.
     */
    ResultT taskResult() {
        return super.data().result();
    }

    /**
     * @return the handle to the asynchronous execution of the task this entry holds.
     */
    Observable<ResultT> executeTaskAsync() {
        return super.data().executeAsync();
    }
}