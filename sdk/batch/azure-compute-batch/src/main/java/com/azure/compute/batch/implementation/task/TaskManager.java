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
        private final Object lock;
        private final AtomicInteger activeThreadCounter;

        WorkingThread(TaskSubmitter taskSubmitter, String jobId, Queue<BatchTaskCreateParameters> pendingList,
            List<BatchTaskCreateResult> failures, Object lock, AtomicInteger activeThreadCounter) {
            this.taskSubmitter = taskSubmitter;
            this.jobId = jobId;
            this.pendingList = pendingList;
            this.failures = failures;
            this.exception = null;
            this.lock = lock;
            this.activeThreadCounter = activeThreadCounter;
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
                activeThreadCounter.decrementAndGet();
                synchronized (lock) {
                    lock.notifyAll();
                }
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

        final Object lock = new Object();
        ConcurrentLinkedQueue<BatchTaskCreateParameters> pendingList = new ConcurrentLinkedQueue<>(taskList);
        CopyOnWriteArrayList<BatchTaskCreateResult> failures = new CopyOnWriteArrayList<>();
        Map<Thread, WorkingThread> threads = new HashMap<>();
        Exception innerException = null;

        // Tracks the number of active threads currently running. Prevents the coordinator from waiting indefinitely if no threads are alive.
        AtomicInteger activeThreadCounter = new AtomicInteger(0);

        synchronized (lock) {
            // Continue looping while there are still tasks left to submit OR while any active threads are still processing tasks.
            while (!pendingList.isEmpty() || activeThreadCounter.get() > 0) {

                if (threads.size() < threadNumber && !pendingList.isEmpty()) {
                    // Kick as many as possible add tasks requests by max allowed threads
                    WorkingThread worker
                        = new WorkingThread(taskSubmitter, jobId, pendingList, failures, lock, activeThreadCounter);
                    Thread thread = new Thread(worker);
                    activeThreadCounter.incrementAndGet(); // Increment before starting the thread to ensure activeThreadCounter accurately reflects the number of active threads
                    thread.start();
                    threads.put(thread, worker);
                } else {

                    // Only wait if there are active threads that can notify us
                    if (activeThreadCounter.get() > 0) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw logger.logExceptionAsError(new RuntimeException(e));
                        }
                    } else {
                        // No active threads: nothing to wait for.
                        // Loop will exit if the queue is empty, or retry starting threads otherwise.
                        continue;
                    }

                    // If no capacity for new workers or no tasks remain, clean up any finished threads
                    List<Thread> finishedThreads = new ArrayList<>();
                    for (Map.Entry<Thread, WorkingThread> entry : threads.entrySet()) {
                        if (entry.getKey().getState() == Thread.State.TERMINATED) {
                            finishedThreads.add(entry.getKey());
                            // If any exception is encountered, then stop immediately without waiting for
                            // remaining active threads.
                            innerException = entry.getValue().getException();
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

        for (Thread t : threads.keySet()) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw logger.logExceptionAsError(new RuntimeException(e));
            }
        }

        if (innerException == null) {
            // Check for errors in any of the threads.
            for (Map.Entry<Thread, WorkingThread> entry : threads.entrySet()) {
                innerException = entry.getValue().getException();
                if (innerException != null) {
                    break;
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
