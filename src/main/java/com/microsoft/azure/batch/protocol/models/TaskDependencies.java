/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;

/**
 * Specifies any dependencies of a task. Any task that is explicitly specified
 * or within a dependency range must complete before the dependant task will
 * be scheduled.
 */
public class TaskDependencies {
    /**
     * The list of task ids that must complete before this task can be
     * scheduled.
     */
    private List<String> taskIds;

    /**
     * The list of task ranges that must complete before this task can be
     * scheduled.
     */
    private List<TaskIdRange> taskIdRanges;

    /**
     * Get the taskIds value.
     *
     * @return the taskIds value
     */
    public List<String> taskIds() {
        return this.taskIds;
    }

    /**
     * Set the taskIds value.
     *
     * @param taskIds the taskIds value to set
     * @return the TaskDependencies object itself.
     */
    public TaskDependencies withTaskIds(List<String> taskIds) {
        this.taskIds = taskIds;
        return this;
    }

    /**
     * Get the taskIdRanges value.
     *
     * @return the taskIdRanges value
     */
    public List<TaskIdRange> taskIdRanges() {
        return this.taskIdRanges;
    }

    /**
     * Set the taskIdRanges value.
     *
     * @param taskIdRanges the taskIdRanges value to set
     * @return the TaskDependencies object itself.
     */
    public TaskDependencies withTaskIdRanges(List<TaskIdRange> taskIdRanges) {
        this.taskIdRanges = taskIdRanges;
        return this;
    }

}
