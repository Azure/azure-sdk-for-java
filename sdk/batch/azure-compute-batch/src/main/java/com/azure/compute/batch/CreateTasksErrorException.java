// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch;

import com.azure.compute.batch.models.BatchClientParallelOptions;
import com.azure.compute.batch.models.BatchTask;
import com.azure.compute.batch.models.BatchTaskCollection;
import com.azure.compute.batch.models.TaskAddResult;
import com.azure.core.exception.AzureException;


import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * The exception that is thrown when the {@link TaskClient#addTasks(String, BatchTaskCollection, BatchClientParallelOptions)} operation is terminated.
 */
public class CreateTasksErrorException extends AzureException {

    /**
     * Initializes a new instance of the CreateTasksErrorException class.
     *
     * @param message The exception message.
     * @param failureTaskList The list of {@link TaskAddResult} instances containing failure details for tasks that were not successfully created.
     * @param pendingTaskList The list of {@link BatchTask} instances containing the tasks that were not added, but for which the operation can be retried.
     */
    public CreateTasksErrorException(final String message, List<TaskAddResult> failureTaskList, List<BatchTask> pendingTaskList) {
        super(message, null);
        this.failureTaskList = unmodifiableList(failureTaskList);
        this.pendingTaskList = unmodifiableList(pendingTaskList);
    }

    private List<TaskAddResult> failureTaskList;

    private List<BatchTask> pendingTaskList;

    /**
     * @return The list of {@link TaskAddResult} instances containing failure details for tasks that were not successfully created.
     */
    public List<TaskAddResult> failureTaskList() {
        return failureTaskList;
    }

    /**
     * @return The list of {@link BatchTask} instances containing the tasks that were not added, but for which the operation can be retried.
     */
    public List<BatchTask> pendingTaskList() {
        return pendingTaskList;
    }
}
