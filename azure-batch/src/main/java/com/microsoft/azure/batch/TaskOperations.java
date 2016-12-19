/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.interceptor.BatchClientParallelOptions;
import com.microsoft.azure.batch.protocol.models.*;
import com.microsoft.rest.ServiceResponseWithHeaders;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Performs task related operations on an Azure Batch account.
 */
public class TaskOperations implements IInheritedBehaviors {
    TaskOperations(BatchClient batchClient, Collection<BatchClientBehavior> customBehaviors) {
        _parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.InheritClientBehaviorsAndSetPublicProperty(this, customBehaviors);
    }

    private Collection<BatchClientBehavior> _customBehaviors;

    private BatchClient _parentBatchClient;

    /**
     * Gets a list of behaviors that modify or customize requests to the Batch service.
     *
     * @return A list of BatchClientBehavior
     */
    @Override
    public Collection<BatchClientBehavior> customBehaviors() {
        return _customBehaviors;
    }

    /**
     * Sets a list of behaviors that modify or customize requests to the Batch service.
     *
     * @param behaviors The collection of BatchClientBehavior classes
     * @return The current instance
     */
    @Override
    public IInheritedBehaviors withCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        _customBehaviors = behaviors;
        return this;
    }

    /**
     * Adds a single task to a job.
     *
     * @param jobId The ID of the job to which to add the task.
     * @param taskToAdd The {@link CloudTask} to add.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createTask(String jobId, TaskAddParameter taskToAdd) throws BatchErrorException, IOException {
        createTask(jobId, taskToAdd, null);
    }

    /**
     * Adds a single task to a job.
     *
     * @param jobId The ID of the job to which to add the task.
     * @param taskToAdd The {@link CloudTask} to add.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createTask(String jobId, TaskAddParameter taskToAdd, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskAddOptions options = new TaskAddOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().tasks().add(jobId, taskToAdd, options);
    }

    /**
     * Adds multiple tasks to a job.
     *
     * @param jobId The ID of the job to which to add the task.
     * @param taskList A collection of {@link CloudTask tasks} to add.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown if any thread has interrupted the current thread.
     */
    public void createTasks(String jobId, List<TaskAddParameter> taskList) throws BatchErrorException, IOException, InterruptedException {
        createTasks(jobId, taskList, null);
    }

    private static class WorkingThread implements Runnable {

        final int MAX_TASKS_PER_REQUEST = 100;

        private BatchClient client;
        private BehaviorManager bhMgr;
        private String jobId;
        private Queue<TaskAddParameter> pendingList;
        private List<TaskAddResult> failures;
        private volatile Exception exception;
        private final Object lock;

        WorkingThread(BatchClient client, BehaviorManager bhMgr, String jobId, Queue<TaskAddParameter> pendingList, List<TaskAddResult> failures, Object lock) {
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

            List<TaskAddParameter> taskList = new LinkedList<>();

            // Take the task from the queue up to MAX_TASKS_PER_REQUEST
            int count = 0;
            while (count < MAX_TASKS_PER_REQUEST) {
                TaskAddParameter param = pendingList.poll();
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
                TaskAddCollectionOptions options = new TaskAddCollectionOptions();
                this.bhMgr.applyRequestBehaviors(options);

                try {
                    ServiceResponseWithHeaders<TaskAddCollectionResult, TaskAddCollectionHeaders> response = this.client.protocolLayer().tasks().addCollection(this.jobId, taskList, options);

                    if (response.getBody() != null && response.getBody().value() != null) {
                        for (TaskAddResult result : response.getBody().value()) {
                            if (result.error() != null) {
                                if (result.status() == TaskAddStatus.SERVERERROR) {
                                    // Server error will be retried
                                    for (TaskAddParameter addParameter : taskList) {
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

    /**
     * Adds multiple tasks to a job.
     *
     * @param jobId The ID of the job to which to add the task.
     * @param taskList A collection of {@link CloudTask tasks} to add.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown if any thread has interrupted the current thread.
     */
    public void createTasks(String jobId, List<TaskAddParameter> taskList, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException, InterruptedException {

        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);

        // Default thread number is 1
        int threadNumber = 1;

        // Get user defined thread number
        for (BatchClientBehavior op : bhMgr.getMasterListOfBehaviors()) {
            if (op instanceof BatchClientParallelOptions) {
                threadNumber = ((BatchClientParallelOptions)op).maxDegreeOfParallelism();
                break;
            }
        }

        final Object lock = new Object();
        ConcurrentLinkedQueue<TaskAddParameter> pendingList = new ConcurrentLinkedQueue<>(taskList);
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
            List<TaskAddParameter> notFinished = new ArrayList<>();
            for (TaskAddParameter param : pendingList) {
                notFinished.add(param);
            }
            throw new CreateTasksTerminatedException("At least one task failed to be added.", failures, notFinished);
        }

        // We succeed here
    }

    /**
     * Enumerates the {@link CloudTask tasks} of the specified job.
     *
     * @param jobId The ID of the job.
     * @return A collection of {@link CloudTask tasks}.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<CloudTask> listTasks(String jobId) throws BatchErrorException, IOException {
        return listTasks(jobId, null, null);
    }

    /**
     * Enumerates the {@link CloudTask tasks} of the specified job.
     *
     * @param jobId The ID of the job.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A collection of {@link CloudTask tasks}.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<CloudTask> listTasks(String jobId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listTasks(jobId, detailLevel, null);
    }

    /**
     * Enumerates the {@link CloudTask tasks} of the specified job.
     *
     * @param jobId The ID of the job.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A collection of {@link CloudTask tasks}.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<CloudTask> listTasks(String jobId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskListOptions options = new TaskListOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<CloudTask>, TaskListHeaders> response = this._parentBatchClient.protocolLayer().tasks().list(jobId, options);

        return response.getBody();
    }

    /**
     * Enumerates the {@link SubtaskInformation subtask information} of the specified task.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @return A collection of {@link SubtaskInformation subtask information}.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<SubtaskInformation> listSubtasks(String jobId, String taskId) throws BatchErrorException, IOException {
        return listSubtasks(jobId, taskId, null, null);
    }

    /**
     * Enumerates the {@link SubtaskInformation subtask information} of the specified task.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A collection of {@link SubtaskInformation subtask information}.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<SubtaskInformation> listSubtasks(String jobId, String taskId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listSubtasks(jobId, taskId, detailLevel, null);
    }

    /**
     * Enumerates the {@link SubtaskInformation subtask information} of the specified task.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A collection of {@link SubtaskInformation subtask information}.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<SubtaskInformation> listSubtasks(String jobId, String taskId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskListSubtasksOptions options = new TaskListSubtasksOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<CloudTaskListSubtasksResult, TaskListSubtasksHeaders> response = this._parentBatchClient.protocolLayer().tasks().listSubtasks(jobId, taskId, options);

        if (response.getBody() != null) {
            return response.getBody().value();
        }
        else {
            return null;
        }
    }

    /**
     * Deletes the specified task.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void deleteTask(String jobId, String taskId) throws BatchErrorException, IOException {
        deleteTask(jobId, taskId, null);
    }

    /**
     * Deletes the specified task.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void deleteTask(String jobId, String taskId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskDeleteOptions options = new TaskDeleteOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().tasks().delete(jobId, taskId, options);
    }

    /**
     * Gets the specified {@link CloudTask}.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @return A {@link CloudTask} containing information about the specified Azure Batch task.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public CloudTask getTask(String jobId, String taskId) throws BatchErrorException, IOException {
        return getTask(jobId, taskId, null, null);
    }

    /**
     * Gets the specified {@link CloudTask}.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A {@link CloudTask} containing information about the specified Azure Batch task.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public CloudTask getTask(String jobId, String taskId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getTask(jobId, taskId, detailLevel, null);
    }

    /**
     * Gets the specified {@link CloudTask}.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A {@link CloudTask} containing information about the specified Azure Batch task.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public CloudTask getTask(String jobId, String taskId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskGetOptions options = new TaskGetOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<CloudTask, TaskGetHeaders> response = this._parentBatchClient.protocolLayer().tasks().get(jobId, taskId, options);

        return response.getBody();
    }

    /**
     * Updates the specified task.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param constraints Constraints that apply to this task. If omitted, the task is given the default constraints.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void updateTask(String jobId, String taskId, TaskConstraints constraints) throws BatchErrorException, IOException {
        updateTask(jobId, taskId, constraints, null);
    }

    /**
     * Updates the specified task.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param constraints Constraints that apply to this task. If omitted, the task is given the default constraints.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void updateTask(String jobId, String taskId, TaskConstraints constraints, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskUpdateOptions options = new TaskUpdateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().tasks().update(jobId, taskId, constraints, options);
    }

    /**
     * Terminates the specified task.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void terminateTask(String jobId, String taskId) throws BatchErrorException, IOException {
        terminateTask(jobId, taskId, null);
    }

    /**
     * Terminates the specified task.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void terminateTask(String jobId, String taskId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskTerminateOptions options = new TaskTerminateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().tasks().terminate(jobId, taskId, options);
    }

    /**
     * Reactivates a task, allowing it to run again even if its retry count has been exhausted.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void  reactivateTask(String jobId, String taskId) throws BatchErrorException, IOException {
        reactivateTask(jobId, taskId, null);
    }

    /**
     * Reactivates a task, allowing it to run again even if its retry count has been exhausted.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void reactivateTask(String jobId, String taskId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskReactivateOptions options = new TaskReactivateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().tasks().reactivate(jobId, taskId, options);
    }

}
