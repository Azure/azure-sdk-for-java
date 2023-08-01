// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

/**
 * Defines task group termination strategies to be used once a task error-ed.
 */
public enum TaskGroupTerminateOnErrorStrategy {
    /**
     * Indicate that on task error, allows for any currently executing tasks to finish and emit
     * onError terminal event with {@link RuntimeException}.
     * Acronym: IPTC
     */
    TERMINATE_ON_IN_PROGRESS_TASKS_COMPLETION,
    /**
     * Indicate that on task error, allows any other tasks those are not directly or indirectly
     * depends on the error-ed task to execute, once a LCA (lowest common ancestor) task is hit
     * (at that point any progress cannot be made) emit onError terminal event with
     * {@link RuntimeException} .
     * Acronym: HLT
     */
    TERMINATE_ON_HITTING_LCA_TASK
}
