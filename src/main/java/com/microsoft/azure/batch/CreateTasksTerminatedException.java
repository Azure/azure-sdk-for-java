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

public class CreateTasksTerminatedException extends BatchErrorException {

    /**
     * Initializes a new instance of the CreateTasksTerminatedException class.
     *
     * @param message The exception message.
     */
    public CreateTasksTerminatedException(final String message, List<TaskAddResult> failureTasks, List<TaskAddParameter> pendingList) {
        super(message);
        this.failureTasks = failureTasks;
    }

    /**
     * The actual response Result.
     */
    private List<TaskAddResult> failureTasks;

    private List<TaskAddParameter> pendingTaskList;

    public List<TaskAddResult> getFailureTasks() {
        return failureTasks;
    }

    public List<TaskAddParameter> getPendingTaskList() {
        return pendingTaskList;
    }
}
