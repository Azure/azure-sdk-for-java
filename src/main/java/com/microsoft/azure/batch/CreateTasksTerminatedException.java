/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.TaskAddParameter;
import com.microsoft.azure.batch.protocol.models.TaskAddResult;

import java.util.List;

/**
 * The exception that is thrown when the {@link TaskOperations#createTasks(String, List)} operation is terminated.
 */
public class CreateTasksTerminatedException extends BatchErrorException {

    /**
     * Initializes a new instance of the CreateTasksTerminatedException class.
     *
     * @param message The exception message.
     * @param failureTasks The list of {@link TaskAddResult} instances containing failure details for tasks that were not successfully created.
     * @param pendingList The list of {@link TaskAddParameter} instances containing the tasks that were not added, but for which the operation can be retried.
     */
    public CreateTasksTerminatedException(final String message, List<TaskAddResult> failureTasks, List<TaskAddParameter> pendingList) {
        super(message);
        this.failureTasks = failureTasks;
    }

    private List<TaskAddResult> failureTasks;

    private List<TaskAddParameter> pendingTaskList;

    /**
     * @return The list of {@link TaskAddResult} instances containing failure details for tasks that were not successfully created.
     */
    public List<TaskAddResult> failureTasks() {
        return failureTasks;
    }

    /**
     * @return The list of {@link TaskAddParameter} instances containing the tasks that were not added, but for which the operation can be retried.
     */
    public List<TaskAddParameter> pendingTaskList() {
        return pendingTaskList;
    }
}
