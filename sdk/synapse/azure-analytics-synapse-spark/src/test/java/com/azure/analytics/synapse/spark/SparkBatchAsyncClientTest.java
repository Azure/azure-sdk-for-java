// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.spark;

import com.azure.analytics.synapse.spark.models.SparkBatchJob;
import com.azure.analytics.synapse.spark.models.SparkBatchJobCollection;
import com.azure.analytics.synapse.spark.models.SparkBatchJobOptions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SparkBatchAsyncClientTest extends SparkClientTestBase {
    private SparkBatchAsyncClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new SparkClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .sparkPoolName(getSparkPoolName())
            .buildSparkBatchAsyncClient());
    }

    @Test
    public void getSparkBatchJob() {
        client.getSparkBatchJobs()
            .map(SparkBatchJobCollection::getSessions)
            .flatMapMany(Flux::fromIterable)
            .doOnNext(expected -> StepVerifier.create(client.getSparkBatchJob(expected.getId()))
                .assertNext(actual -> assertSparkJobEquals(expected, actual))
                .verifyComplete());
    }

    @Test
    public void crudSparkBatchJob() {
        // arrange
        String jobName = testResourceNamer.randomName("spark-job-", 20);
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

        AtomicReference<SparkBatchJob> testJob = new AtomicReference<>();

        try {
            // act
            StepVerifier.create(client.createSparkBatchJob(options, true))
                .consumeNextWith(expected -> {
                    testJob.set(expected);

                    assertEquals(jobName, expected.getName());
                    assertEquals(getSparkPoolName(), expected.getSparkPoolName());
                })
                .verifyComplete();

            // act
            StepVerifier.create(client.getSparkBatchJob(testJob.get().getId(), true))
                .assertNext(actual -> assertSparkJobEquals(testJob.get(), actual))
                .verifyComplete();

        } finally {
            // clean up
            if (testJob.get() != null) {
                client.cancelSparkBatchJob(testJob.get().getId()).block();
            }
        }
    }
}
