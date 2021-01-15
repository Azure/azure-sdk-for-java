// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.spark;

import com.azure.analytics.synapse.spark.models.SparkBatchJob;
import org.junit.jupiter.api.Test;

public class SparkBatchClientTest extends SparkClientTestBase {

    private SparkBatchClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new SparkClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .sparkPoolName(getSparkPoolName())
            .buildSparkBatchClient());
    }

    /**
     * Tests that Spark jobs can be listed in the Spark pool.
     */
    @Test
    public void getSparkBatchJob() {
        for (SparkBatchJob expectedSparkJob : client.getSparkBatchJobs().getSessions()) {
            SparkBatchJob actualSparkJob = client.getSparkBatchJob(expectedSparkJob.getId());
            validateSparkBatchJob(expectedSparkJob, actualSparkJob);
        }
    }
}
