/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;

/**
 * Response to a CloudTaskOperations.ListSubtasks request.
 */
public class CloudTaskListSubtasksResult {
    /**
     * The list of information of subtasks.
     */
    private List<SubtaskInformation> value;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public List<SubtaskInformation> value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the CloudTaskListSubtasksResult object itself.
     */
    public CloudTaskListSubtasksResult withValue(List<SubtaskInformation> value) {
        this.value = value;
        return this;
    }

}
