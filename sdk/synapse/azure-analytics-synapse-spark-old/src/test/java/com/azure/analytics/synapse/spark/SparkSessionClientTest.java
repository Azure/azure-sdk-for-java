package com.azure.analytics.synapse.spark;

import com.azure.analytics.synapse.spark.implementation.models.SparkBatchJob;
import org.junit.jupiter.api.Test;

public class SparkSessionClientTest extends SparkClientTestBase {

    private SparkBatchClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new SparkBatchClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .sparkPoolName(getSparkPoolName())
            .buildClient());
    }

    /**
     * Tests that role assignments can be listed in the key vault.
     */
    @Test
    public void getSparkBatchJob() {
        for (SparkBatchJob expectedSparkJob : client.getSparkBatchJobs().getSessions())
        {
            SparkBatchJob actualSparkJob = client.getSparkBatchJob(expectedSparkJob.getId());
            validateSparkBatchJob(expectedSparkJob, actualSparkJob);
        }
    }
}
