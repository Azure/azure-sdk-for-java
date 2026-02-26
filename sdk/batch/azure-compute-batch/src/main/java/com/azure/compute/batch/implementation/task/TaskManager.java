// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.implementation.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.azure.compute.batch.BatchClient;
import com.azure.compute.batch.models.BatchCreateTaskCollectionResult;
import com.azure.compute.batch.models.BatchError;
import com.azure.compute.batch.models.BatchErrorException;
import com.azure.compute.batch.models.BatchTaskAddStatus;
import com.azure.compute.batch.models.BatchTaskBulkCreateOptions;
import com.azure.compute.batch.models.BatchTaskCreateResult;
import com.azure.compute.batch.models.BatchTaskCreateParameters;
import com.azure.compute.batch.models.BatchTaskGroup;
import com.azure.compute.batch.models.CreateTasksErrorException;
import com.azure.core.util.logging.ClientLogger;

/**
 * The TaskManager class is responsible for managing the task submission process for a Batch job.
 * It handles the complexities of submitting tasks in parallel, managing retries for failed submissions,
 * and tracking any failures.
 */
public class TaskManager {

    /**
     * Wrapper class to pair Thread and WorkingThread to check for thread states
     */
    private static class ThreadPairInfo {
        private final Thread thread;
        private final WorkingThread workingThread;

        ThreadPairInfo(Thread thread, WorkingThread workingThread) {
            this.thread = thread;
            this.workingThread = workingThread;
        }

        public WorkingThread getWorkingThread() {
            return workingThread;
        }

        public boolean isTerminated() {
            return thread.getState() == Thread.State.TERMINATED;
        }
    }

    /**
     * Runnable implementation for handling task submissions in a separate thread.
     */
    private static class WorkingThread implements Runnable {
        static final int MAX_TASKS_PER_REQUEST = 100;
        private static final AtomicInteger CURRENT_MAX_TASKS = new AtomicInteger(MAX_TASKS_PER_REQUEST);

        private final TaskSubmitter taskSubmitter;
        private final String jobId;
        private final Queue<BatchTaskCreateParameters> pendingList;
        private final List<BatchTaskCreateResult> failures;
        private volatile Exception exception;
        private final Semaphore taskSemaphore;

        WorkingThread(TaskSubmitter taskSubmitter, String jobId, Queue<BatchTaskCreateParameters> pendingList,
            List<BatchTaskCreateResult> failures, Semaphore taskSemaphore) {
            this.taskSubmitter = taskSubmitter;
            this.jobId = jobId;
            this.pendingList = pendingList;
            this.failures = failures;
            this.exception = null;
            this.taskSemaphore = taskSemaphore;
        }

        /**
         * Gets the exception encountered during task processing, if any.
         *
         * @return The exception encountered, or null if no exception occurred.
         */
        public Exception getException() {
            return this.exception;
        }

        /**
         * Submits a chunk of tasks for processing. If any errors occur during submission,
         * they are handled and, where appropriate, tasks are retried or recorded as failures.
         *
         * @param taskList The list of tasks to be submitted.
         */
        private void submitChunk(List<BatchTaskCreateParameters> taskList) {
            try {
                // Build a Task ID to Task map
                Map<String, BatchTaskCreateParameters> taskIdMap = new HashMap<>(taskList.size());
                for (BatchTaskCreateParameters p : taskList) {
                    taskIdMap.putIfAbsent(p.getId(), p);
                }

                BatchCreateTaskCollectionResult response
                    = taskSubmitter.submitTasks(jobId, new BatchTaskGroup(taskList));

                if (response != null && response.getValues() != null) {
                    for (BatchTaskCreateResult result : response.getValues()) {
                        if (result.getError() == null) {
                            continue; // success
                        }

                        if (result.getStatus() == BatchTaskAddStatus.SERVER_ERROR) {
                            // Server error will be retried
                            String id = result.getTaskId();
                            if (id != null) {
                                BatchTaskCreateParameters p = taskIdMap.get(id);
                                if (p != null) {
                                    pendingList.add(p);
                                }
                            }
                        } else if (result.getStatus() == BatchTaskAddStatus.CLIENT_ERROR) {
                            BatchError err = result.getError();
                            String code = (err != null) ? err.getCode() : null;
                            if (!"TaskExists".equalsIgnoreCase(code)) {
                                // Client error will be recorded
                                failures.add(result);
                            }
                        }
                    }
                }
            } catch (BatchErrorException e) {
                handleBatchException(e, taskList);
            } catch (RuntimeException e) {
                exception = e;
                pendingList.addAll(taskList);
            } catch (Exception e) {
                exception = e;
                pendingList.addAll(taskList);
            }
        }

        /**
         * Handles BatchErrorException that may occur during task submission. This includes
         * reducing task chunk size and reattempting submission if the error is due to request body size.
         *
         * @param e The BatchErrorException encountered.
         * @param taskList The list of tasks that were being submitted when the exception occurred.
         */
        private void handleBatchException(BatchErrorException e, List<BatchTaskCreateParameters> taskList) {
            // Split on payload too large (413) if chunk > 1
            if (e.getResponse() != null && e.getResponse().getStatusCode() == 413 && taskList.size() > 1) {
                int midpoint = taskList.size() / 2;
                int max = CURRENT_MAX_TASKS.get();
                while (midpoint < max) {
                    CURRENT_MAX_TASKS.compareAndSet(max, midpoint);
                    max = CURRENT_MAX_TASKS.get();
                }
                // Resubmit chunk as a smaller list and requeue remaining tasks
                pendingList.addAll(taskList.subList(midpoint, taskList.size()));
                submitChunk(taskList.subList(0, midpoint));
            } else {
                exception = e;
                pendingList.addAll(taskList);
            }
        }

        /**
         * Runs the task processing logic in the current thread. Tasks are taken from the pending list
         * and submitted in chunks. The method keeps running until there are no more tasks to process.
         */
        @Override
        public void run() {
            try {
                List<BatchTaskCreateParameters> taskList = new LinkedList<>();
                int count = 0;
                int maxAmount = CURRENT_MAX_TASKS.get();
                while (count < maxAmount) {
                    BatchTaskCreateParameters param = pendingList.poll();
                    if (param != null) {
                        taskList.add(param);
                        count++;
                    } else {
                        break;
                    }
                }
                if (!taskList.isEmpty()) {
                    submitChunk(taskList);
                }
            } finally {
                taskSemaphore.release();
            }
        }
    }

    /**
     * Creates and submits tasks to a Batch job.
     * This method manages the parallel submission of tasks, handling retries and errors as needed.
     *
     * @param taskSubmitter The TaskSubmitter instance used for submitting tasks.
     * @param jobId The ID of the job to which the tasks will be added.
     * @param taskList The list of tasks to be submitted.
     * @param taskCreateOptions Options for configuring the task creation.
     */
    public static void createTasks(TaskSubmitter taskSubmitter, String jobId,
        Collection<BatchTaskCreateParameters> taskList, BatchTaskBulkCreateOptions taskCreateOptions) {

        final ClientLogger logger = new ClientLogger(BatchClient.class);

        WorkingThread.CURRENT_MAX_TASKS.set(WorkingThread.MAX_TASKS_PER_REQUEST);

        int threadNumber = 1;
        if (taskCreateOptions != null && taskCreateOptions.getMaxConcurrency() != null) {
            threadNumber = taskCreateOptions.getMaxConcurrency();
        }

        Semaphore taskSemaphore = new Semaphore(threadNumber);
        ConcurrentLinkedQueue<BatchTaskCreateParameters> pendingList = new ConcurrentLinkedQueue<>(taskList);
        CopyOnWriteArrayList<BatchTaskCreateResult> failures = new CopyOnWriteArrayList<>();
        List<ThreadPairInfo> semaphoreThreads = new ArrayList<>();
        Exception innerException = null;

        // Continue looping while there are still tasks left to submit OR while any active threads are still processing tasks.
        while (!pendingList.isEmpty() || taskSemaphore.availablePermits() < threadNumber) {
            try {
                if (taskSemaphore.tryAcquire(30, TimeUnit.SECONDS)) {
                    if (!pendingList.isEmpty()) {
                        WorkingThread taskThread
                            = new WorkingThread(taskSubmitter, jobId, pendingList, failures, taskSemaphore);
                        Thread thread = new Thread(taskThread);
                        thread.start();
                        semaphoreThreads.add(new ThreadPairInfo(thread, taskThread));
                    } else {
                        taskSemaphore.release();
                    }
                }

                // Check for thread errors only in terminated threads
                for (ThreadPairInfo threadPair : semaphoreThreads) {
                    if (threadPair.isTerminated()) {
                        Exception ex = threadPair.getWorkingThread().getException();
                        if (ex != null) {
                            innerException = ex;
                            break;
                        }
                    }
                }

                if (innerException != null || !failures.isEmpty()) {
                    break;
                }
            } catch (Exception e) {
                // Semaphore, code, thread, etc. exception, not from Task submission
                throw logger.logExceptionAsError(new RuntimeException("Error in task submission semaphore loop", e));
            }
        }

        try {
            taskSemaphore.acquire(threadNumber);
            taskSemaphore.release(threadNumber);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw logger
                .logExceptionAsError(new RuntimeException("Interrupt when checking for all threads to be done", e));
        }

        if (innerException == null) {
            for (ThreadPairInfo threadPair : semaphoreThreads) {
                if (threadPair.isTerminated()) {
                    innerException = threadPair.getWorkingThread().getException();
                    if (innerException != null) {
                        break;
                    }
                }
            }
        }

        // Handle exceptions and failures
        if (innerException != null) {
            // If an exception happened in any of the threads, throw it.
            if (innerException instanceof BatchErrorException) {
                throw logger.logExceptionAsError((BatchErrorException) innerException);
            } else if (innerException instanceof RuntimeException) {
                // WorkingThread will only catch and store a BatchErrorException or a
                // RuntimeException in its run() method.
                // WorkingThread.getException() should therefore only return one of these two
                // types, making the cast safe.
                throw logger.logExceptionAsError((RuntimeException) innerException);
            } else {
                throw logger.logExceptionAsError(new RuntimeException(innerException));
            }
        }

        // Throw aggregated client errors (plus any leftover pending)
        if (!failures.isEmpty()) {
            List<BatchTaskCreateParameters> notFinished = new ArrayList<>();
            for (BatchTaskCreateParameters param : pendingList) {
                notFinished.add(param);
            }
            throw new CreateTasksErrorException("At least one task failed to be added.", failures, notFinished);
        }
        // We succeed here
    }
}
