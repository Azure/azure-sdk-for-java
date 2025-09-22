// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.implementation.task;

import com.azure.compute.batch.BatchClient;
import com.azure.compute.batch.models.BatchCreateTaskCollectionResult;
import com.azure.compute.batch.models.BatchTaskGroup;

/**
 * A synchronous implementation of {@link TaskSubmitter} for submitting batch tasks.
 */
public class SyncClientTaskSubmitter implements TaskSubmitter {
    private final BatchClient client;

    /**
     * Constructs a new SyncTaskSubmitter with the provided BatchClient.
     *
     * @param client The BatchClient to be used for task submission.
     */
    public SyncClientTaskSubmitter(BatchClient client) {
        this.client = client;
    }

    /**
     * Submits a collection of batch tasks synchronously.
     *
     * @param jobId The ID of the job to which the tasks are added.
     * @param taskCollection The collection of tasks to be submitted.
     * @return A BatchCreateTaskCollectionResult representing the result of the task submission.
     * @throws Exception if the task submission fails.
     */
    @Override
    public BatchCreateTaskCollectionResult submitTasks(String jobId, BatchTaskGroup taskCollection) throws Exception {
        return client.createTaskCollection(jobId, taskCollection);
    }
}
