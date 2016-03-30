/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;


/**
 * Parameters for a CloudTaskOperations.Update request.
 */
public class TaskUpdateParameter {
    /**
     * Sets constraints that apply to this task. If omitted, the task is given
     * the default constraints.
     */
    private TaskConstraints constraints;

    /**
     * Get the constraints value.
     *
     * @return the constraints value
     */
    public TaskConstraints getConstraints() {
        return this.constraints;
    }

    /**
     * Set the constraints value.
     *
     * @param constraints the constraints value to set
     */
    public void setConstraints(TaskConstraints constraints) {
        this.constraints = constraints;
    }

}
