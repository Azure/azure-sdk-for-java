// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.interceptor.BatchClientParallelOptions;
import com.microsoft.azure.batch.protocol.models.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Performs task-related operations on an Azure Batch account.
 */
public class TaskOperations implements IInheritedBehaviors {
    TaskOperations(BatchClient batchClient, Collection<BatchClientBehavior> customBehaviors) {
        _parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.InheritClientBehaviorsAndSetPublicProperty(this, customBehaviors);
    }

    private Collection<BatchClientBehavior> _customBehaviors;

    private final BatchClient _parentBatchClient;

    /**
     * Gets a collection of behaviors that modify or customize requests to the Batch
     * service.
     *
     * @return A collection of {@link BatchClientBehavior} instances.
     */
    @Override
    public Collection<BatchClientBehavior> customBehaviors() {
        return _customBehaviors;
    }

    /**
     * Sets a collection of behaviors that modify or customize requests to the Batch
     * service.
     *
     * @param behaviors
     *            The collection of {@link BatchClientBehavior} instances.
     * @return The current instance.
     */
    @Override
    public IInheritedBehaviors withCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        _customBehaviors = behaviors;
        return this;
    }

    /**
     * Adds a single task to a job.
     *
     * @param jobId
     *            The ID of the job to which to add the task.
     * @param taskToAdd
     *            The {@link TaskAddParameter task} to add.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void createTask(String jobId, TaskAddParameter taskToAdd) throws BatchErrorException, IOException {
        createTask(jobId, taskToAdd, null);
    }

    /**
     * Adds a single task to a job.
     *
     * @param jobId
     *            The ID of the job to which to add the task.
     * @param taskToAdd
     *            The {@link TaskAddParameter task} to add.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void createTask(String jobId, TaskAddParameter taskToAdd, Iterable<BatchClientBehavior> additionalBehaviors)
            throws BatchErrorException, IOException {
        TaskAddOptions options = new TaskAddOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().tasks().add(jobId, taskToAdd, options);
    }

    /**
     * Adds multiple tasks to a job.
     *
     * @param jobId
     *            The ID of the job to which to add the task.
     * @param taskList
     *            A list of {@link TaskAddParameter tasks} to add.
     * @throws RuntimeException
     *             Exception thrown when an error response is received from the
     *             Batch service or any network exception.
     * @throws InterruptedException
     *             Exception thrown if any thread has interrupted the current
     *             thread.
     */
    public void createTasks(String jobId, List<TaskAddParameter> taskList)
            throws RuntimeException, InterruptedException {
        createTasks(jobId, taskList, null);
    }

    private static class WorkingThread implements Runnable {
        final static int MAX_TASKS_PER_REQUEST = 100;
        private static final AtomicInteger currentMaxTasks = new AtomicInteger(MAX_TASKS_PER_REQUEST);

        BatchClient client;
        BehaviorManager bhMgr;
        String jobId;
        Queue<TaskAddParameter> pendingList;
        List<TaskAddResult> failures;
        volatile Exception exception;
        final Object lock;

        WorkingThread(BatchClient client, BehaviorManager bhMgr, String jobId, Queue<TaskAddParameter> pendingList,
                List<TaskAddResult> failures, Object lock) {
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

        /**
         * Submits one chunk of tasks to a job.
         * 
         * @param taskList
         *            A list of {@link TaskAddParameter tasks} to add.
         */
        private void submit_chunk(List<TaskAddParameter> taskList) {
            // The option should be different to every server calls (for example,
            // client-request-id)
            TaskAddCollectionOptions options = new TaskAddCollectionOptions();
            this.bhMgr.applyRequestBehaviors(options);
            try {
                TaskAddCollectionResult response = this.client.protocolLayer().tasks().addCollection(this.jobId,
                        taskList, options);

                if (response != null && response.value() != null) {
                    for (TaskAddResult result : response.value()) {
                        if (result.error() != null) {
                            if (result.status() == TaskAddStatus.SERVER_ERROR) {
                                // Server error will be retried
                                for (TaskAddParameter addParameter : taskList) {
                                    if (addParameter.id().equals(result.taskId())) {
                                        pendingList.add(addParameter);
                                        break;
                                    }
                                }
                            } else if (result.status() == TaskAddStatus.CLIENT_ERROR
                                    && !result.error().code().equals(BatchErrorCodeStrings.TaskExists)) {
                                // Client error will be recorded
                                failures.add(result);
                            }
                        }
                    }
                }
            } catch (BatchErrorException e) {
                // If we get RequestBodyTooLarge could be that we chunked the tasks too large.
                // Try decreasing the size unless caused by 1 task.
                if (e.body().code().equals(BatchErrorCodeStrings.RequestBodyTooLarge) && taskList.size() > 1) {
                    // Use binary reduction to decrease size of submitted chunks
                    int midpoint = taskList.size() / 2;
                    // If the midpoint is less than the currentMaxTasks used to create new chunks,
                    // attempt to atomically reduce currentMaxTasks.
                    // In the case where compareAndSet fails, that means that currentMaxTasks which
                    // was the goal
                    int max = currentMaxTasks.get();
                    while (midpoint < max) {
                        currentMaxTasks.compareAndSet(max, midpoint);
                        max = currentMaxTasks.get();
                    }
                    // Resubmit chunk as a smaller list and requeue remaining tasks.
                    pendingList.addAll(taskList.subList(midpoint, taskList.size()));
                    submit_chunk(taskList.subList(0, midpoint));
                } else {
                    // Any exception will stop further call
                    exception = e;
                    pendingList.addAll(taskList);
                }
            } catch (RuntimeException e) {
                // Any exception will stop further call
                exception = e;
                pendingList.addAll(taskList);
            }
        }

        @Override
        public void run() {
            try {
                List<TaskAddParameter> taskList = new LinkedList<>();

                // Take the task from the queue up to MAX_TASKS_PER_REQUEST
                int count = 0;
                int maxAmount = currentMaxTasks.get();
                while (count < maxAmount) {
                    TaskAddParameter param = pendingList.poll();
                    if (param != null) {
                        taskList.add(param);
                        count++;
                    } else {
                        break;
                    }
                }

                if (taskList.size() > 0) {
                    submit_chunk(taskList);
                }
            } finally {
                synchronized (lock) {
                    // Notify main thread that sub thread finished
                    lock.notify();
                }
            }
        }
    }

    /**
     * Adds multiple tasks to a job.
     *
     * @param jobId
     *            The ID of the job to which to add the task.
     * @param taskList
     *            A list of {@link TaskAddParameter tasks} to add.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws RuntimeException
     *             Exception thrown when an error response is received from the
     *             Batch service or any network exception.
     * @throws InterruptedException
     *             Exception thrown if any thread has interrupted the current
     *             thread.
     */
    public void createTasks(String jobId, List<TaskAddParameter> taskList,
            Iterable<BatchClientBehavior> additionalBehaviors) throws RuntimeException, InterruptedException {

        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);

        // Default thread number is 1
        int threadNumber = 1;

        // Get user defined thread number
        for (BatchClientBehavior op : bhMgr.getMasterListOfBehaviors()) {
            if (op instanceof BatchClientParallelOptions) {
                threadNumber = ((BatchClientParallelOptions) op).maxDegreeOfParallelism();
            }
        }

        final Object lock = new Object();
        ConcurrentLinkedQueue<TaskAddParameter> pendingList = new ConcurrentLinkedQueue<>(taskList);
        CopyOnWriteArrayList<TaskAddResult> failures = new CopyOnWriteArrayList<>();

        Map<Thread, WorkingThread> threads = new HashMap<>();
        Exception innerException = null;

        synchronized (lock) {
            while (!pendingList.isEmpty()) {

                if (threads.size() < threadNumber) {
                    // Kick as many as possible add tasks requests by max allowed threads
                    WorkingThread worker = new WorkingThread(this._parentBatchClient, bhMgr, jobId, pendingList,
                            failures, lock);
                    Thread thread = new Thread(worker);
                    thread.start();
                    threads.put(thread, worker);
                } else {
                    // Wait for any thread to finish
                    lock.wait();

                    List<Thread> finishedThreads = new ArrayList<>();
                    for (Thread t : threads.keySet()) {
                        if (t.getState() == Thread.State.TERMINATED) {
                            finishedThreads.add(t);
                            // If any exception is encountered, then stop immediately without waiting for
                            // remaining active threads.
                            innerException = threads.get(t).getException();
                            if (innerException != null) {
                                break;
                            }
                        }
                    }

                    // Free the thread pool so we can start more threads to send the remaining add
                    // tasks requests.
                    threads.keySet().removeAll(finishedThreads);

                    // Any errors happened, we stop.
                    if (innerException != null || !failures.isEmpty()) {
                        break;
                    }
                }
            }
        }

        // Wait for all remaining threads to finish.
        for (Thread t : threads.keySet()) {
            t.join();
        }

        if (innerException == null) {
            // Check for errors in any of the threads.
            for (Thread t : threads.keySet()) {
                innerException = threads.get(t).getException();
                if (innerException != null) {
                    break;
                }
            }
        }

        if (innerException != null) {
            // If an exception happened in any of the threads, throw it.
            if (innerException instanceof BatchErrorException) {
                throw (BatchErrorException) innerException;
            } else {
                // WorkingThread will only catch and store a BatchErrorException or a
                // RuntimeException in its run() method.
                // WorkingThread.getException() should therefore only return one of these two
                // types, making the cast safe.
                throw (RuntimeException) innerException;
            }
        }

        if (!failures.isEmpty()) {
            // Report any client error with leftover request
            List<TaskAddParameter> notFinished = new ArrayList<>();
            for (TaskAddParameter param : pendingList) {
                notFinished.add(param);
            }
            throw new CreateTasksErrorException("At least one task failed to be added.", failures, notFinished);
        }

        // We succeed here
    }

    /**
     * Lists the {@link CloudTask tasks} of the specified job.
     *
     * @param jobId
     *            The ID of the job.
     * @return A list of {@link CloudTask} objects.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public PagedList<CloudTask> listTasks(String jobId) throws BatchErrorException, IOException {
        return listTasks(jobId, null, null);
    }

    /**
     * Lists the {@link CloudTask tasks} of the specified job.
     *
     * @param jobId
     *            The ID of the job.
     * @param detailLevel
     *            A {@link DetailLevel} used for filtering the list and for
     *            controlling which properties are retrieved from the service.
     * @return A list of {@link CloudTask} objects.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public PagedList<CloudTask> listTasks(String jobId, DetailLevel detailLevel)
            throws BatchErrorException, IOException {
        return listTasks(jobId, detailLevel, null);
    }

    /**
     * Lists the {@link CloudTask tasks} of the specified job.
     *
     * @param jobId
     *            The ID of the job.
     * @param detailLevel
     *            A {@link DetailLevel} used for filtering the list and for
     *            controlling which properties are retrieved from the service.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @return A list of {@link CloudTask} objects.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public PagedList<CloudTask> listTasks(String jobId, DetailLevel detailLevel,
            Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskListOptions options = new TaskListOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        return this._parentBatchClient.protocolLayer().tasks().list(jobId, options);
    }

    /**
     * Lists the {@link SubtaskInformation subtasks} of the specified task.
     *
     * @param jobId
     *            The ID of the job containing the task.
     * @param taskId
     *            The ID of the task.
     * @return A list of {@link SubtaskInformation} objects.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public List<SubtaskInformation> listSubtasks(String jobId, String taskId) throws BatchErrorException, IOException {
        return listSubtasks(jobId, taskId, null, null);
    }

    /**
     * Lists the {@link SubtaskInformation subtasks} of the specified task.
     *
     * @param jobId
     *            The ID of the job containing the task.
     * @param taskId
     *            The ID of the task.
     * @param detailLevel
     *            A {@link DetailLevel} used for controlling which properties are
     *            retrieved from the service.
     * @return A list of {@link SubtaskInformation} objects.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public List<SubtaskInformation> listSubtasks(String jobId, String taskId, DetailLevel detailLevel)
            throws BatchErrorException, IOException {
        return listSubtasks(jobId, taskId, detailLevel, null);
    }

    /**
     * Lists the {@link SubtaskInformation subtasks} of the specified task.
     *
     * @param jobId
     *            The ID of the job containing the task.
     * @param taskId
     *            The ID of the task.
     * @param detailLevel
     *            A {@link DetailLevel} used for controlling which properties are
     *            retrieved from the service.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @return A list of {@link SubtaskInformation} objects.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public List<SubtaskInformation> listSubtasks(String jobId, String taskId, DetailLevel detailLevel,
            Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskListSubtasksOptions options = new TaskListSubtasksOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        CloudTaskListSubtasksResult response = this._parentBatchClient.protocolLayer().tasks().listSubtasks(jobId,
                taskId, options);

        if (response != null) {
            return response.value();
        } else {
            return null;
        }
    }

    /**
     * Deletes the specified task.
     *
     * @param jobId
     *            The ID of the job containing the task.
     * @param taskId
     *            The ID of the task.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void deleteTask(String jobId, String taskId) throws BatchErrorException, IOException {
        deleteTask(jobId, taskId, null);
    }

    /**
     * Deletes the specified task.
     *
     * @param jobId
     *            The ID of the job containing the task.
     * @param taskId
     *            The ID of the task.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void deleteTask(String jobId, String taskId, Iterable<BatchClientBehavior> additionalBehaviors)
            throws BatchErrorException, IOException {
        TaskDeleteOptions options = new TaskDeleteOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().tasks().delete(jobId, taskId, options);
    }

    /**
     * Gets the specified {@link CloudTask}.
     *
     * @param jobId
     *            The ID of the job containing the task.
     * @param taskId
     *            The ID of the task.
     * @return A {@link CloudTask} containing information about the specified Azure
     *         Batch task.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public CloudTask getTask(String jobId, String taskId) throws BatchErrorException, IOException {
        return getTask(jobId, taskId, null, null);
    }

    /**
     * Gets the specified {@link CloudTask}.
     *
     * @param jobId
     *            The ID of the job containing the task.
     * @param taskId
     *            The ID of the task.
     * @param detailLevel
     *            A {@link DetailLevel} used for controlling which properties are
     *            retrieved from the service.
     * @return A {@link CloudTask} containing information about the specified Azure
     *         Batch task.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public CloudTask getTask(String jobId, String taskId, DetailLevel detailLevel)
            throws BatchErrorException, IOException {
        return getTask(jobId, taskId, detailLevel, null);
    }

    /**
     * Gets the specified {@link CloudTask}.
     *
     * @param jobId
     *            The ID of the job containing the task.
     * @param taskId
     *            The ID of the task.
     * @param detailLevel
     *            A {@link DetailLevel} used for controlling which properties are
     *            retrieved from the service.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @return A {@link CloudTask} containing information about the specified Azure
     *         Batch task.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public CloudTask getTask(String jobId, String taskId, DetailLevel detailLevel,
            Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskGetOptions options = new TaskGetOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        return this._parentBatchClient.protocolLayer().tasks().get(jobId, taskId, options);
    }

    /**
     * Updates the specified task.
     *
     * @param jobId
     *            The ID of the job containing the task.
     * @param taskId
     *            The ID of the task.
     * @param constraints
     *            Constraints that apply to this task. If null, the task is given
     *            the default constraints.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void updateTask(String jobId, String taskId, TaskConstraints constraints)
            throws BatchErrorException, IOException {
        updateTask(jobId, taskId, constraints, null);
    }

    /**
     * Updates the specified task.
     *
     * @param jobId
     *            The ID of the job containing the task.
     * @param taskId
     *            The ID of the task.
     * @param constraints
     *            Constraints that apply to this task. If null, the task is given
     *            the default constraints.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void updateTask(String jobId, String taskId, TaskConstraints constraints,
            Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskUpdateOptions options = new TaskUpdateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().tasks().update(jobId, taskId, constraints, options);
    }

    /**
     * Terminates the specified task.
     *
     * @param jobId
     *            The ID of the job containing the task.
     * @param taskId
     *            The ID of the task.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void terminateTask(String jobId, String taskId) throws BatchErrorException, IOException {
        terminateTask(jobId, taskId, null);
    }

    /**
     * Terminates the specified task.
     *
     * @param jobId
     *            The ID of the job containing the task.
     * @param taskId
     *            The ID of the task.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void terminateTask(String jobId, String taskId, Iterable<BatchClientBehavior> additionalBehaviors)
            throws BatchErrorException, IOException {
        TaskTerminateOptions options = new TaskTerminateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().tasks().terminate(jobId, taskId, options);
    }

    /**
     * Reactivates a task, allowing it to run again even if its retry count has been
     * exhausted.
     *
     * @param jobId
     *            The ID of the job containing the task.
     * @param taskId
     *            The ID of the task.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void reactivateTask(String jobId, String taskId) throws BatchErrorException, IOException {
        reactivateTask(jobId, taskId, null);
    }

    /**
     * Reactivates a task, allowing it to run again even if its retry count has been
     * exhausted.
     *
     * @param jobId
     *            The ID of the job containing the task.
     * @param taskId
     *            The ID of the task.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void reactivateTask(String jobId, String taskId, Iterable<BatchClientBehavior> additionalBehaviors)
            throws BatchErrorException, IOException {
        TaskReactivateOptions options = new TaskReactivateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().tasks().reactivate(jobId, taskId, options);
    }

}
