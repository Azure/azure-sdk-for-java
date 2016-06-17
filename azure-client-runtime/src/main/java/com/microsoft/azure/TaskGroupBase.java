/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure;

import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;

/**
 * The base implementation of TaskGroup interface.
 *
 * @param <T> the result type of the tasks in the group
 */
public abstract class TaskGroupBase<T>
    implements TaskGroup<T, TaskItem<T>> {
    private DAGraph<TaskItem<T>, DAGNode<TaskItem<T>>> dag;

    /**
     * Creates TaskGroupBase.
     *
     * @param rootTaskItemId the id of the root task in this task group
     * @param rootTaskItem the root task
     */
    public TaskGroupBase(String rootTaskItemId, TaskItem<T> rootTaskItem) {
        this.dag = new DAGraph<>(new DAGNode<>(rootTaskItemId, rootTaskItem));
    }

    @Override
    public DAGraph<TaskItem<T>, DAGNode<TaskItem<T>>> dag() {
        return dag;
    }

    @Override
    public boolean isRoot() {
        return !dag.hasParent();
    }

    @Override
    public void merge(TaskGroup<T, TaskItem<T>> parentTaskGroup) {
        dag.merge(parentTaskGroup.dag());
    }

    @Override
    public void prepare() {
        if (isRoot()) {
            dag.prepare();
        }
    }

    @Override
    public void execute() throws Exception {
        DAGNode<TaskItem<T>> nextNode = dag.getNext();
        if (nextNode == null) {
            return;
        }

        if (dag.isRootNode(nextNode)) {
            executeRootTask(nextNode.data());
        } else {
            // TaskGroupBase::execute will be called both in update and create
            // scenarios, so run the task only if it not not executed already.
            if (nextNode.data().result() == null) {
                nextNode.data().execute(this, nextNode);
            }
        }
    }

    @Override
    public ServiceCall executeAsync(final ServiceCallback<Void> callback) {
        final DAGNode<TaskItem<T>> nextNode = dag.getNext();
        if (nextNode == null) {
            return null;
        }

        if (dag.isRootNode(nextNode)) {
            return executeRootTaskAsync(nextNode.data(), callback);
        } else {
            // TaskGroupBase::execute will be called both in update and create
            // scenarios, so run the task only if it not not executed already.
            if (nextNode.data().result() == null) {
                return nextNode.data().executeAsync(this, nextNode, callback);
            } else {
                return null;
            }
        }
    }

    @Override
    public T taskResult(String taskId) {
        return dag.getNodeData(taskId).result();
    }

    /**
     * executes the root task in this group.
     * <p>
     * this method will be invoked when all the task dependencies of the root task are finished
     * executing, at this point root task can be executed by consuming the result of tasks it
     * depends on.
     *
     * @param task the root task in this group
     * @throws Exception the exception
     */
    public abstract void executeRootTask(TaskItem<T> task) throws Exception;

    /**
     * executes the root task in this group asynchronously.
     * <p>
     * this method will be invoked when all the task dependencies of the root task are finished
     * executing, at this point root task can be executed by consuming the result of tasks it
     * depends on.
     *
     * @param task the root task in this group
     * @param callback the callback when the task fails or succeeds
     * @return the handle to the REST call
     */
    public abstract ServiceCall executeRootTaskAsync(TaskItem<T> task, ServiceCallback<Void> callback);
}
