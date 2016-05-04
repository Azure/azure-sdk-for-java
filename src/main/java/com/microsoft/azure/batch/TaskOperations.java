/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.interceptor.BatchClientParallelOptions;
import com.microsoft.azure.batch.protocol.implementation.api.BatchErrorException;
import com.microsoft.azure.batch.protocol.implementation.api.CloudTaskInner;
import com.microsoft.azure.batch.protocol.implementation.api.CloudTaskListSubtasksResultInner;
import com.microsoft.azure.batch.protocol.implementation.api.SubtaskInformation;
import com.microsoft.azure.batch.protocol.implementation.api.TaskAddCollectionHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.TaskAddCollectionOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.TaskAddCollectionResultInner;
import com.microsoft.azure.batch.protocol.implementation.api.TaskAddOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.TaskAddParameterInner;
import com.microsoft.azure.batch.protocol.implementation.api.TaskAddResult;
import com.microsoft.azure.batch.protocol.implementation.api.TaskAddStatus;
import com.microsoft.azure.batch.protocol.implementation.api.TaskConstraintsInner;
import com.microsoft.azure.batch.protocol.implementation.api.TaskDeleteOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.TaskGetHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.TaskGetOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.TaskListHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.TaskListOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.TaskListSubtasksHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.TaskListSubtasksOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.TaskTerminateOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.TaskUpdateOptionsInner;
import com.microsoft.rest.ServiceResponseWithHeaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class TaskOperations implements IInheritedBehaviors {
    TaskOperations(BatchClient batchClient, Collection<BatchClientBehavior> customBehaviors) {
        _parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.InheritClientBehaviorsAndSetPublicProperty(this, customBehaviors);
    }

    private Collection<BatchClientBehavior> _customBehaviors;

    private BatchClient _parentBatchClient;

    @Override
    public Collection<BatchClientBehavior> getCustomBehaviors() {
        return _customBehaviors;
    }

    @Override
    public void setCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        _customBehaviors = behaviors;
    }

    public void createTask(String jobId, TaskAddParameterInner taskToAdd) throws BatchErrorException, IOException {
        createTask(jobId, taskToAdd, null);
    }

    public void createTask(String jobId, TaskAddParameterInner taskToAdd, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskAddOptionsInner options = new TaskAddOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().tasks().add(jobId, taskToAdd, options);
    }

    public void createTasks(String jobId, List<TaskAddParameterInner> taskList) throws BatchErrorException, IOException, InterruptedException {
        createTasks(jobId, taskList, null);
    }

    private static class WorkingThread implements Runnable {

        final int MAX_TASKS_PER_REQUEST = 100;

        private BatchClient client;
        private BehaviorManager bhMgr;
        private String jobId;
        private Queue<TaskAddParameterInner> pendingList;
        private List<TaskAddResult> failures;
        private volatile Exception exception;
        private final Object lock;

        WorkingThread(BatchClient client, BehaviorManager bhMgr, String jobId, Queue<TaskAddParameterInner> pendingList, List<TaskAddResult> failures, Object lock) {
            this.client = client;
            this.bhMgr = bhMgr;
            this.jobId = jobId;
            this.pendingList = pendingList;
            this.failures = failures;
            this.exception = null;
            this.lock = lock;
        }

        public Exception getException() {
            return this.exception;
        }

        @Override
        public void run() {

            List<TaskAddParameterInner> taskList = new LinkedList<>();

            // Take the task from the queue up to MAX_TASKS_PER_REQUEST
            int count = 0;
            while (count < MAX_TASKS_PER_REQUEST) {
                TaskAddParameterInner param = pendingList.poll();
                if (param != null) {
                    taskList.add(param);
                    count++;
                }
                else {
                    break;
                }
            }

            if (taskList.size() > 0) {
                // The option should be different to every server calls (for example, client-request-id)
                TaskAddCollectionOptionsInner options = new TaskAddCollectionOptionsInner();
                this.bhMgr.applyRequestBehaviors(options);

                try {
                    ServiceResponseWithHeaders<TaskAddCollectionResultInner, TaskAddCollectionHeadersInner> response = this.client.getProtocolLayer().tasks().addCollection(this.jobId, taskList, options);

                    if (response.getBody() != null && response.getBody().value() != null) {
                        for (TaskAddResult result : response.getBody().value()) {
                            if (result.error() != null) {
                                if (result.status() == TaskAddStatus.SERVERERROR) {
                                    // Server error will be retried
                                    for (TaskAddParameterInner addParameter : taskList) {
                                        if (addParameter.id().equals(result.taskId())) {
                                            pendingList.add(addParameter);
                                            break;
                                        }
                                    }
                                } else if (result.status() == TaskAddStatus.CLIENTERROR && !result.error().code().equals(BatchErrorCodeStrings.TaskExists)) {
                                    // Client error will be recorded
                                    failures.add(result);
                                }
                            }
                        }
                    }
                } catch (BatchErrorException | IOException e) {
                    // Any exception will stop further call
                    exception = e;
                    pendingList.addAll(taskList);
                }
            }

            synchronized (lock) {
                // Notify main thread that sub thread finished
                lock.notify();
            }
        }
    }

    public void createTasks(String jobId, List<TaskAddParameterInner> taskList, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException, InterruptedException {

        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);

        // Default thread number is 1
        int threadNumber = 1;

        // Get user defined thread number
        for (BatchClientBehavior op : bhMgr.getMasterListOfBehaviors()) {
            if (op instanceof BatchClientParallelOptions) {
                threadNumber = ((BatchClientParallelOptions) op).getMaxDegreeOfParallelism();
                break;
            }
        }

        final Object lock = new Object();
        ConcurrentLinkedQueue<TaskAddParameterInner> pendingList = new ConcurrentLinkedQueue<>(taskList);
        CopyOnWriteArrayList<TaskAddResult> failures = new CopyOnWriteArrayList<>();

        Map<Thread, WorkingThread> threads = new HashMap<>();
        Exception innerException = null;

        while (!pendingList.isEmpty()) {

            if (threads.size() < threadNumber) {
                // Kick as many as possible add tasks requests by max allowed threads
                WorkingThread worker = new WorkingThread(this._parentBatchClient, bhMgr, jobId, pendingList, failures, lock);
                Thread thread = new Thread(worker);
                thread.start();
                threads.put(thread, worker);
            }
            else {
                // Wait any thread finished
                synchronized (lock) {
                    lock.wait();
                }

                List<Thread> finishedThreads = new ArrayList<>();
                for (Thread t : threads.keySet()) {
                    if (t.getState() == Thread.State.TERMINATED) {
                        finishedThreads.add(t);
                        // If any exception happened, do not care the left requests
                        innerException = threads.get(t).getException();
                        if (innerException != null) {
                            break;
                        }
                    }
                }

                // Free thread pool, so we can start more threads to send the request
                threads.keySet().removeAll(finishedThreads);

                // Any errors happened, we stop
                if (innerException != null || !failures.isEmpty()) {
                    break;
                }
            }
        }

        // May sure all the left threads finished
        for (Thread t : threads.keySet()) {
            t.join();
        }

        if (innerException == null) {
            // Anything bad happened at the left threads?
            for (Thread t : threads.keySet()) {
                innerException = threads.get(t).getException();
                if (innerException != null) {
                    break;
                }
            }
        }

        if (innerException != null) {
            // We throw any exception happened in sub thread
            if (innerException instanceof BatchErrorException) {
                throw (BatchErrorException) innerException;
            } else {
                throw (IOException) innerException;
            }
        }

        if (!failures.isEmpty()) {
            // Report any client error with leftover request
            List<TaskAddParameterInner> notFinished = new ArrayList<>();
            for (TaskAddParameterInner param : pendingList) {
                notFinished.add(param);
            }
            throw new CreateTasksTerminatedException("At least one task failed to be added.", failures, notFinished);
        }

        // We succeed here
    }

    public List<CloudTaskInner> listTasks(String jobId) throws BatchErrorException, IOException {
        return listTasks(jobId, null, null);
    }

    public List<CloudTaskInner> listTasks(String jobId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listTasks(jobId, detailLevel, null);
    }

    public List<CloudTaskInner> listTasks(String jobId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskListOptionsInner options = new TaskListOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<CloudTaskInner>, TaskListHeadersInner> response = this._parentBatchClient.getProtocolLayer().tasks().list(jobId, options);

        return response.getBody();
    }

    public List<SubtaskInformation> listSubtasks(String jobId, String taskId) throws BatchErrorException, IOException {
        return listSubtasks(jobId, taskId, null, null);
    }

    public List<SubtaskInformation> listSubtasks(String jobId, String taskId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listSubtasks(jobId, taskId, detailLevel, null);
    }

    public List<SubtaskInformation> listSubtasks(String jobId, String taskId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskListSubtasksOptionsInner options = new TaskListSubtasksOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<CloudTaskListSubtasksResultInner, TaskListSubtasksHeadersInner> response = this._parentBatchClient.getProtocolLayer().tasks().listSubtasks(jobId, taskId, options);

        if (response.getBody() != null) {
            return response.getBody().value();
        }
        else {
            return null;
        }
    }

    public void deleteTask(String jobId, String taskId) throws BatchErrorException, IOException {
        deleteTask(jobId, taskId, null);
    }

    public void deleteTask(String jobId, String taskId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskDeleteOptionsInner options = new TaskDeleteOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().tasks().delete(jobId, taskId, options);
    }

    public CloudTaskInner getTask(String jobId, String taskId) throws BatchErrorException, IOException {
        return getTask(jobId, taskId, null, null);
    }

    public CloudTaskInner getTask(String jobId, String taskId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getTask(jobId, taskId, detailLevel, null);
    }

    public CloudTaskInner getTask(String jobId, String taskId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskGetOptionsInner options = new TaskGetOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<CloudTaskInner, TaskGetHeadersInner> response = this._parentBatchClient.getProtocolLayer().tasks().get(jobId, taskId, options);

        return response.getBody();
    }

    public void updateTask(String jobId, String taskId, TaskConstraintsInner constraints) throws BatchErrorException, IOException {
        updateTask(jobId, taskId, constraints, null);
    }

    public void updateTask(String jobId, String taskId, TaskConstraintsInner constraints, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskUpdateOptionsInner options = new TaskUpdateOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().tasks().update(jobId, taskId, constraints, options);
    }

    public void terminateTask(String jobId, String taskId) throws BatchErrorException, IOException {
        terminateTask(jobId, taskId, null);
    }

    public void terminateTask(String jobId, String taskId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskTerminateOptionsInner options = new TaskTerminateOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().tasks().terminate(jobId, taskId, options);
    }
}
