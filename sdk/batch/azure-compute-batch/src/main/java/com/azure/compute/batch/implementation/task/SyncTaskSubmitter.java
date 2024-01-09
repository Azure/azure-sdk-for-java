// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.implementation.task;

import com.azure.compute.batch.BatchClient;
import com.azure.compute.batch.models.BatchTaskAddCollectionResult;
import com.azure.compute.batch.models.BatchTaskCollection;
import reactor.core.publisher.Mono;

/**
 * A synchronous implementation of {@link TaskSubmitter} for submitting batch tasks.
 */
public class SyncTaskSubmitter implements TaskSubmitter {
    private BatchClient client;

    /**
     * Constructs a new SyncTaskSubmitter with the provided BatchClient.
     *
     * @param client The BatchClient to be used for task submission.
     */
    public SyncTaskSubmitter(BatchClient client) {
        this.client = client;
    }

    /**
     * Submits a collection of batch tasks synchronously.
     *
     * @param jobId The ID of the job to which the tasks are added.
     * @param taskCollection The collection of tasks to be submitted.
     * @return A BatchTaskAddCollectionResult representing the result of the task submission.
     * @throws Exception if the task submission fails.
     */
    @Override
    public Mono<BatchTaskAddCollectionResult>  submitTasks(String jobId, BatchTaskCollection taskCollection) throws Exception {
        return Mono.fromCallable(() -> client.createTaskCollection(jobId, taskCollection));
    }
}
