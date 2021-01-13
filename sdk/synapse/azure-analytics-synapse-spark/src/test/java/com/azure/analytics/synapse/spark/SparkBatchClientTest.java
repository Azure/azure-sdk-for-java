// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.spark;

import com.azure.analytics.synapse.spark.models.SparkBatchJob;
import com.azure.analytics.synapse.spark.models.SparkBatchJobOptions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    @Test
    public void getSparkBatchJob() {
        for (SparkBatchJob expectedSparkJob : client.getSparkBatchJobs().getSessions()) {
            SparkBatchJob actualSparkJob = client.getSparkBatchJob(expectedSparkJob.getId());
            assertSparkJobEquals(expectedSparkJob, actualSparkJob);
        }
    }

    @Test
    public void crudSparkBatchJob() {
        // arrange
        String jobName = testResourceNamer.randomName("spark-job-", 10);
        String file = String.format("abfss://%s@%s.dfs.core.windows.net/wordcount.jar", getStorageContainerName(), getStorageAccountName());
        SparkBatchJobOptions options = new SparkBatchJobOptions()
            .setName(jobName)
            .setFile(file)
            .setClassName("WordCount")
            .setArguments(Arrays.asList(
                String.format("abfss://%s@%s.dfs.core.windows.net/shakespeare.txt", getStorageContainerName(), getStorageAccountName()),
                String.format("abfss://%s@%s.dfs.core.windows.net/java-result/", getStorageContainerName(), getStorageAccountName())
            ))
            .setDriverMemory("28g")
            .setDriverCores(4)
            .setExecutorMemory("28g")
            .setExecutorCores(4)
            .setExecutorCount(2);

        // act
        SparkBatchJob expected = client.createSparkBatchJob(options, true);

        // assert
        assertNotNull(expected);
        assertEquals(jobName, expected.getName());
        assertEquals(getSparkPoolName(), expected.getSparkPoolName());

        // act
        SparkBatchJob actual = client.getSparkBatchJob(expected.getId(), true);

        // assert
        assertSparkJobEquals(expected, actual);

        // clean up
        client.cancelSparkBatchJob(expected.getId());
    }
}
