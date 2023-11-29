// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.implementation.task;

import com.azure.compute.batch.BatchAsyncClient;
import com.azure.compute.batch.models.BatchTaskAddCollectionResult;
import com.azure.compute.batch.models.BatchTaskCollection;

/**
 * Implementation of {@link TaskSubmitter} for asynchronous task submission in Batch.
 */
public class AsyncTaskSubmitter implements TaskSubmitter {
    private BatchAsyncClient client;

    /**
     * Constructs a new AsyncTaskSubmitter with the specified BatchAsyncClient.
     *
     * @param client The {@link BatchAsyncClient} used for task submission.
     */
    public AsyncTaskSubmitter(BatchAsyncClient client) {
        this.client = client;
    }

    /**
     * Submits a collection of tasks to a specified job and waits for the submission to complete.
     * This method blocks until the asynchronous operation of task submission is complete.
     *
     * @param jobId The ID of the job to which the tasks are being added.
     * @param taskCollection The collection of tasks to be added to the job.
     * @return {@link BatchTaskAddCollectionResult} representing the result of the task submission.
     * @throws Exception if the task submission fails.
     */
    @Override
    public BatchTaskAddCollectionResult submitTasks(String jobId, BatchTaskCollection taskCollection) throws Exception {
        return client.createTaskCollectionInternal(jobId, taskCollection).block();
    }
}
