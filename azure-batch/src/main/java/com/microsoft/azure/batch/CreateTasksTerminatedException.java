/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.batch.protocol.implementation.api.BatchErrorException;
import com.microsoft.azure.batch.protocol.implementation.api.TaskAddParameterInner;
import com.microsoft.azure.batch.protocol.implementation.api.TaskAddResult;

import java.util.List;

public class CreateTasksTerminatedException extends BatchErrorException {

    /**
     * Initializes a new instance of the CreateTasksTerminatedException class.
     *
     * @param message The exception message.
     */
    public CreateTasksTerminatedException(final String message, List<TaskAddResult> failureTasks, List<TaskAddParameterInner> pendingList) {
        super(message);
        this.failureTasks = failureTasks;
    }

    /**
     * The actual response Result.
     */
    private List<TaskAddResult> failureTasks;

    private List<TaskAddParameterInner> pendingTaskList;

    public List<TaskAddResult> getFailureTasks() {
        return failureTasks;
    }

    public List<TaskAddParameterInner> getPendingTaskList() {
        return pendingTaskList;
    }
}
