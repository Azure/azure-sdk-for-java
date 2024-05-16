// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.implementation.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import com.azure.compute.batch.BatchClient;
import com.azure.compute.batch.models.BatchClientParallelOptions;
import com.azure.compute.batch.models.BatchTaskAddResult;
import com.azure.compute.batch.models.BatchTaskAddStatus;
import com.azure.compute.batch.models.BatchTaskCreateContent;
import com.azure.compute.batch.models.BatchTaskGroup;
import com.azure.compute.batch.models.CreateTasksErrorException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

/**
 * The TaskManager class is responsible for managing the task submission process for a Batch job.
 * It handles the complexities of submitting tasks in parallel, managing retries for failed submissions,
 * and tracking any failures.
 */
public class TaskManager {

    /**
     * Runnable implementation for handling task submissions in a separate thread.
     */
    public static class WorkingThread implements Runnable {

        static final int MAX_TASKS_PER_REQUEST = 100;
        private static final AtomicInteger CURRENT_MAX_TASKS = new AtomicInteger(MAX_TASKS_PER_REQUEST);

        private final TaskSubmitter taskSubmitter;
        private final String jobId;
        private final Queue<BatchTaskCreateContent> pendingList;
        private final List<BatchTaskAddResult> failures;
        private volatile Exception exception;
        private final Object lock;

        public WorkingThread(
            TaskSubmitter taskSubmitter,
            String jobId,
            Queue<BatchTaskCreateContent> pendingList,
            List<BatchTaskAddResult> failures,
            Object lock) {
            this.taskSubmitter = taskSubmitter;
            this.jobId = jobId;
            this.pendingList = pendingList;
            this.failures = failures;
            this.exception = null;
            this.lock = lock;
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
        private void submitChunk(List<BatchTaskCreateContent> taskList) {
            try {
                taskSubmitter.submitTasks(jobId, new BatchTaskGroup(taskList))
                    .doOnError(e -> {
                        if (e instanceof HttpResponseException) {
                            // Handle HttpResponseException
                            handleException((HttpResponseException) e, taskList);
                        } else {
                            // Handle generic exceptions
                            exception = (Exception) e;
                            pendingList.addAll(taskList);
                        }
                    })
                    .subscribe(response -> {
                        if (response != null && response.getValue() != null) {
                            for (BatchTaskAddResult result : response.getValue()) {
                                if (result.getError() != null) {
                                    if (result.getStatus() == BatchTaskAddStatus.SERVER_ERROR) {
                                        // Server error will be retried
                                        for (BatchTaskCreateContent batchTaskToCreate : taskList) {
                                            if (batchTaskToCreate.getId().equals(result.getTaskId())) {
                                                pendingList.add(batchTaskToCreate);
                                                break;
                                            }
                                        }
                                    } else if (result.getStatus() == BatchTaskAddStatus.CLIENT_ERROR
                                        && !result.getError().getMessage().getValue().contains("Status code 409")) {
                                        // Client error will be recorded
                                        failures.add(result);
                                    }
                                }
                            }
                        }
                    });
            } catch (Exception e) {
                // TODO (catch): Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * Handles HttpResponseException that may occur during task submission. This includes
         * reducing task chunk size and reattempting submission if the error is due to request body size.
         *
         * @param e The HttpResponseException encountered.
         * @param taskList The list of tasks that were being submitted when the exception occurred.
         */
        private void handleException(HttpResponseException e, List<BatchTaskCreateContent> taskList) {
            if (e.getResponse().getStatusCode() == 413 && taskList.size() > 1) {
                // Use binary reduction to decrease size of submitted chunks
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
                List<BatchTaskCreateContent> taskList = new LinkedList<>();
                int count = 0;
                int maxAmount = CURRENT_MAX_TASKS.get();
                while (count < maxAmount) {
                    BatchTaskCreateContent param = pendingList.poll();
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
     * @param batchClientParallelOptions Options for configuring the parallelism of task submissions.
     */
    public static Mono<Void> createTasks(
        TaskSubmitter taskSubmitter,
        String jobId,
        List<BatchTaskCreateContent> taskList,
        BatchClientParallelOptions batchClientParallelOptions) {

        final ClientLogger logger = new ClientLogger(BatchClient.class);

        return Mono.create(sink -> {
            int threadNumber = 1;
            // Get user defined thread number
            if (batchClientParallelOptions != null) {
                threadNumber = batchClientParallelOptions.getMaxDegreeOfParallelism();
            }
            final Object lock = new Object();
            ConcurrentLinkedQueue<BatchTaskCreateContent> pendingList = new ConcurrentLinkedQueue<>(taskList);
            CopyOnWriteArrayList<BatchTaskAddResult> failures = new CopyOnWriteArrayList<>();
            Map<Thread, WorkingThread> threads = new HashMap<>();
            Exception innerException = null;

            synchronized (lock) {
                while (!pendingList.isEmpty()) {
                    if (threads.size() < threadNumber) {
                        // Kick as many as possible add tasks requests by max allowed threads
                        WorkingThread worker = new WorkingThread(taskSubmitter, jobId, pendingList, failures, lock);
                        Thread thread = new Thread(worker);
                        thread.start();
                        threads.put(thread, worker);
                    } else {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            sink.error(e);
                            return;
                        }
                        List<Thread> finishedThreads = new ArrayList<>();
                        for (Map.Entry<Thread, WorkingThread> entry : threads.entrySet()) {
                            if (entry.getKey().getState() == Thread.State.TERMINATED) {
                                finishedThreads.add(entry.getKey());
                                // If any exception is encountered, then stop immediately without waiting for
                                // remaining active threads.
                                innerException = entry.getValue().getException();
                                if (innerException != null) {
                                    sink.error(innerException);
                                    return;
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
                try {
                    t.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    sink.error(e);
                    return;
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
                if (innerException instanceof HttpResponseException) {
                    throw logger.logExceptionAsError((HttpResponseException) innerException);
                } else if (innerException instanceof RuntimeException) {
                    // WorkingThread will only catch and store a BatchErrorException or a
                    // RuntimeException in its run() method.
                    // WorkingThread.getException() should therefore only return one of these two
                    // types, making the cast safe.
                    throw logger.logExceptionAsError((RuntimeException) innerException);
                }
            }

            if (!failures.isEmpty()) {
                List<BatchTaskCreateContent> notFinished = new ArrayList<>(pendingList);
                for (BatchTaskCreateContent param : pendingList) {
                    notFinished.add(param);
                }
                sink.error(new CreateTasksErrorException("At least one task failed to be added.", failures, notFinished));
            } else {
                sink.success();
            }
        });
        // We succeed here
    }

}
