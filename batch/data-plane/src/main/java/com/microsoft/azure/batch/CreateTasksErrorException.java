// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.TaskAddParameter;
import com.microsoft.azure.batch.protocol.models.TaskAddResult;

import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * The exception that is thrown when the {@link TaskOperations#createTasks(String, List)} operation is terminated.
 */
public class CreateTasksErrorException extends BatchErrorException {

    /**
     * Initializes a new instance of the CreateTasksErrorException class.
     *
     * @param message The exception message.
     * @param failureTaskList The list of {@link TaskAddResult} instances containing failure details for tasks that were not successfully created.
     * @param pendingTaskList The list of {@link TaskAddParameter} instances containing the tasks that were not added, but for which the operation can be retried.
     */
    public CreateTasksErrorException(final String message, List<TaskAddResult> failureTaskList, List<TaskAddParameter> pendingTaskList) {
        super(message, null);
        this.failureTaskList = unmodifiableList(failureTaskList);
        this.pendingTaskList = unmodifiableList(pendingTaskList);
    }

    private List<TaskAddResult> failureTaskList;

    private List<TaskAddParameter> pendingTaskList;

    /**
     * @return The list of {@link TaskAddResult} instances containing failure details for tasks that were not successfully created.
     */
    public List<TaskAddResult> failureTaskList() {
        return failureTaskList;
    }

    /**
     * @return The list of {@link TaskAddParameter} instances containing the tasks that were not added, but for which the operation can be retried.
     */
    public List<TaskAddParameter> pendingTaskList() {
        return pendingTaskList;
    }
}
