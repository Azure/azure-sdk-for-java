// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.spark;

import com.azure.analytics.synapse.spark.models.SparkBatchJob;
import com.azure.analytics.synapse.spark.models.SparkBatchJobOptions;
import com.azure.analytics.synapse.spark.models.SparkSession;
import com.azure.analytics.synapse.spark.models.SparkSessionOptions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SparkSessionClientTest extends SparkClientTestBase {

    private SparkSessionClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new SparkClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .sparkPoolName(getSparkPoolName())
            .buildSparkSessionClient());
    }

    @Test
    public void getSparkSession() {
        for (SparkSession expectedSparkSession : client.getSparkSessions().getSessions()) {
            SparkSession actualSparkSession = client.getSparkSession(expectedSparkSession.getId());
            assertSparkSessionEquals(expectedSparkSession, actualSparkSession);
        }
    }

    @Test
    public void crudSparkSession() {
        // arrange
        String sessionName = testResourceNamer.randomName("spark-session-", 10);
        String file = String.format("abfss://%s@%s.dfs.core.windows.net/wordcount.jar", getStorageContainerName(), getStorageAccountName());
        SparkSessionOptions options = new SparkSessionOptions()
            .setName(sessionName)
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
        SparkSession expected = client.createSparkSession(options, true);

        // assert
        assertNotNull(expected);
        assertEquals(sessionName, expected.getName());
        assertEquals(getSparkPoolName(), expected.getSparkPoolName());

        // act
        SparkSession actual = client.getSparkSession(expected.getId(), true);

        // assert
        assertSparkSessionEquals(expected, actual);

        // clean up
        client.cancelSparkSession(expected.getId());
    }
}
