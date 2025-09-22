// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch.models;

import com.azure.core.exception.AzureException;

import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * The exception that is thrown when the {@link com.azure.compute.batch.BatchClient#createTasks(String, Collection, BatchClientParallelOptions)} operation is terminated.
 */
public class CreateTasksErrorException extends AzureException {

    /**
     * Initializes a new instance of the CreateTasksErrorException class.
     *
     * @param message The exception message.
     * @param failureTaskList The list of {@link BatchTaskCreateResult} instances containing failure details for tasks that were not successfully created.
     * @param pendingTaskList The list of {@link BatchTask} instances containing the tasks that were not added, but for which the operation can be retried.
     */
    public CreateTasksErrorException(final String message, List<BatchTaskCreateResult> failureTaskList,
        List<BatchTaskCreateParameters> pendingTaskList) {
        super(message, null);
        this.failureTaskList = unmodifiableList(failureTaskList);
        this.pendingTaskList = unmodifiableList(pendingTaskList);
    }

    /**
     * The list of {@link BatchTaskCreateResult} instances containing failure details for tasks that were not successfully created.
     */
    private final List<BatchTaskCreateResult> failureTaskList;

    /**
     * The list of {@link BatchTask} instances containing the tasks that were not added, but for which the operation can be retried.
     */
    private final List<BatchTaskCreateParameters> pendingTaskList;

    /**
     * Gets the list of {@link BatchTaskCreateResult} instances containing failure details for tasks that were not successfully created.
     *
     * @return The list of {@link BatchTaskCreateResult} instances containing failure details for tasks that were not successfully created.
     */
    public List<BatchTaskCreateResult> failureTaskList() {
        return failureTaskList;
    }

    /**
     * Gets the list of {@link BatchTask} instances containing the tasks that were not added, but for which the operation can be retried.
     *
     * @return The list of {@link BatchTask} instances containing the tasks that were not added, but for which the operation can be retried.
     */
    public List<BatchTaskCreateParameters> pendingTaskList() {
        return pendingTaskList;
    }
}
