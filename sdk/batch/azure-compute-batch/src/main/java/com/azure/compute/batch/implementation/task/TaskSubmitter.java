// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.implementation.task;

import com.azure.compute.batch.models.BatchTaskAddCollectionResult;
import com.azure.compute.batch.models.BatchTaskCollection;
import reactor.core.publisher.Mono;

/**
 * Interface that provides an abstraction for submitting tasks to a Batch job.
 */
public interface TaskSubmitter {

    /**
     * Submits a collection of tasks to a specified Batch job.
     *
     * @param jobId The ID of the job to which the tasks will be added.
     * @param taskCollection The collection of tasks to be submitted.
     * @return A BatchTaskAddCollectionResult representing the result of the task submission.
     * @throws Exception if there is an error during task submission.
     */
    Mono<BatchTaskAddCollectionResult> submitTasks(String jobId, BatchTaskCollection taskCollection) throws Exception;
}
