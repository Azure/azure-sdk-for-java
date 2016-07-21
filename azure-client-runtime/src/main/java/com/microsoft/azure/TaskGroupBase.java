/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure;

import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 /**
 * An instance of this class provides access to the underlying REST service call running
 * in parallel.
 *
 * @param <T>
 */
class ParallelServiceCall<T> extends ServiceCall {
    private TaskGroupBase<T> taskGroup;

    /**
     * Creates a ParallelServiceCall
     *
     * @param taskGroup the task group
     */
    public ParallelServiceCall(TaskGroupBase<T> taskGroup) {
        super(null);
        this.taskGroup = taskGroup;
    }

    /**
     * Cancels all the service calls currently executing.
     */
    public void cancel() {
        for (ServiceCall call : this.taskGroup.calls()) {
            call.cancel();
        }
    }

    /**
     * @return true if the call has been canceled; false otherwise.
     */
    public boolean isCancelled() {
        for (ServiceCall call : this.taskGroup.calls()) {
            if (!call.isCanceled()) {
                return false;
            }
        }
        return true;
    }
}

/**
 * The base implementation of TaskGroup interface.
 *
 * @param <T> the result type of the tasks in the group
 */
public abstract class TaskGroupBase<T>
    implements TaskGroup<T, TaskItem<T>> {
    private DAGraph<TaskItem<T>, DAGNode<TaskItem<T>>> dag;
    private ConcurrentLinkedQueue<ServiceCall> serviceCalls = new ConcurrentLinkedQueue<>();

    public ParallelServiceCall<T> parallelServiceCall;

    /**
     * Creates TaskGroupBase.
     *
     * @param rootTaskItemId the id of the root task in this task group
     * @param rootTaskItem the root task
     */
    public TaskGroupBase(String rootTaskItemId, TaskItem<T> rootTaskItem) {
        this.dag = new DAGraph<>(new DAGNode<>(rootTaskItemId, rootTaskItem));
        this.parallelServiceCall = new ParallelServiceCall<>(this);
    }

    List<ServiceCall> calls() {
        return Collections.unmodifiableList(Arrays.asList(serviceCalls.toArray(new ServiceCall[0])));
    }

    @Override
    public DAGraph<TaskItem<T>, DAGNode<TaskItem<T>>> dag() {
        return dag;
    }

    @Override
    public boolean isPreparer() {
        return dag.isPreparer();
    }

    @Override
    public void merge(TaskGroup<T, TaskItem<T>> parentTaskGroup) {
        dag.merge(parentTaskGroup.dag());
    }

    @Override
    public void prepare() {
        if (isPreparer()) {
            dag.prepare();
        }
    }

    @Override
    public void execute() throws Exception {
        DAGNode<TaskItem<T>> nextNode = dag.getNext();
        if (nextNode == null) {
            return;
        }

        nextNode.data().execute(this, nextNode);
    }

    @Override
    public ServiceCall executeAsync(final ServiceCallback<Void> callback) {
        ServiceCall serviceCall = null;
        DAGNode<TaskItem<T>> nextNode = dag.getNext();
        while (nextNode != null) {
            if (dag.isRootNode(nextNode)) {
                serviceCall = nextNode.data().executeAsync(this, nextNode, new ServiceCallback<Void>() {
                    @Override
                    public void failure(Throwable t) {
                        callback.failure(t);
                    }

                    @Override
                    public void success(ServiceResponse<Void> result) {
                        callback.success(result);
                    }
                });
            } else {
                serviceCall = nextNode.data().executeAsync(this, nextNode, callback);
            }

            if (serviceCall != null) {
                // We need to filter out the null value returned by executeAsync. This can
                // happen when TaskItem::executeAsync invokes TaskGroupBase::executeAsync
                // but there is no task available in the queue at the moment.
                this.serviceCalls.add(serviceCall);
            }
            nextNode = dag.getNext();
        }
        return serviceCall;
    }

    @Override
    public T taskResult(String taskId) {
        return dag.getNodeData(taskId).result();
    }
}
